package de.hysky.skyblocker.skyblock.dungeon.secrets

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.DungeonsConfig
import de.hysky.skyblocker.events.DungeonEvents
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room.Direction.DirectionArgumentType
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint.Category.CategoryArgumentType
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Tickable
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilled
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import de.hysky.skyblocker.utils.render.Renderable
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.ints.IntRBTreeSet
import it.unimi.dsi.fastutil.ints.IntSortedSet
import it.unimi.dsi.fastutil.ints.IntSortedSets
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.mob.AmbientEntity
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.commons.lang3.tuple.MutableTriple
import org.apache.commons.lang3.tuple.Triple
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Pattern

open class Room(val type: Type, vararg physicalPositions: Vector2ic?) : Tickable, Renderable {
	val segments: Set<Vector2ic> = java.util.Set.of(*physicalPositions)

	/**
	 * The shape of the room. See [.getShape].
	 */
	private val shape: Shape

	/**
	 * The room data containing all rooms for a specific dungeon and [.shape].
	 */
	protected var roomsData: Map<String, IntArray>?

	/**
	 * Contains all possible dungeon rooms for this room. The list is gradually shrunk by checking blocks until only one room is left.
	 */
	protected var possibleRooms: List<MutableTriple<Direction, Vector2ic?, List<String>>>?

	/**
	 * Contains all blocks that have been checked to prevent checking the same block multiple times.
	 */
	private var checkedBlocks: MutableSet<BlockPos>? = HashSet()

	/**
	 * The task that is used to check blocks. This is used to ensure only one such task can run at a time.
	 */
	protected var findRoom: CompletableFuture<Void>? = null
	private var doubleCheckBlocks = 0

	/**
	 * Represents the matching state of the room with the following possible values:
	 *  * [MatchState.MATCHING] means that the room has not been checked, is being processed, or does not [need to be processed][Type.needsScanning].
	 *  * [MatchState.DOUBLE_CHECKING] means that the room has a unique match and is being double checked.
	 *  * [MatchState.MATCHED] means that the room has a unique match ans has been double checked.
	 *  * [MatchState.FAILED] means that the room has been checked and there is no match.
	 */
	protected var matchState: MatchState = MatchState.MATCHING
	private var secretWaypoints: Table<Int, BlockPos?, SecretWaypoint>? = null

	/**
	 * Not null if [.isMatched].
	 */
	var name: String? = null
		private set

	/**
	 * Not null if [.isMatched].
	 */
	var direction: Direction? = null
		private set
	private var physicalCornerPos: Vector2ic? = null

	var tickables: MutableList<Tickable?> = ArrayList()
	var renderables: MutableList<Renderable?> = ArrayList()
	private var lastChestSecret: BlockPos? = null
	private var lastChestSecretTime: Long = 0

	/**
	 * Stores the next room in the dungeon. Currently only used if the next room is the fairy room.
	 */
	var nextRoom: Room? = null
	private var doorPos: BlockPos? = null
	private var doorBox: Box? = null
	var keyFound: Boolean = false

	init {
		val segmentsX = IntSortedSets.unmodifiable(IntRBTreeSet(segments.stream().mapToInt { obj: Vector2ic -> obj.x() }.toArray()))
		val segmentsY = IntSortedSets.unmodifiable(IntRBTreeSet(segments.stream().mapToInt { obj: Vector2ic -> obj.y() }.toArray()))
		shape = getShape(segmentsX, segmentsY)
		roomsData = DungeonManager.ROOMS_DATA.getOrDefault("catacombs", emptyMap()).getOrDefault(shape.shape.lowercase(Locale.getDefault()), emptyMap())
		possibleRooms = getPossibleRooms(segmentsX, segmentsY)
	}

	val isMatched: Boolean
		get() = matchState == MatchState.DOUBLE_CHECKING || matchState == MatchState.MATCHED

	override fun toString(): String {
		return "Room{type=%s, segments=%s, shape=%s, matchState=%s, name=%s, direction=%s, physicalCornerPos=%s}".formatted(type, segments.toTypedArray().contentToString(), shape, matchState, name, direction, physicalCornerPos)
	}

	private fun getShape(segmentsX: IntSortedSet, segmentsY: IntSortedSet): Shape {
		return when (type) {
			Type.PUZZLE -> Shape.PUZZLE
			Type.TRAP -> Shape.TRAP
			else -> when (segments.size) {
				1 -> Shape.ONE_BY_ONE
				2 -> Shape.ONE_BY_TWO
				3 -> if (segmentsX.size == 2 && segmentsY.size == 2) Shape.L_SHAPE else Shape.ONE_BY_THREE
				4 -> if (segmentsX.size == 2 && segmentsY.size == 2) Shape.TWO_BY_TWO else Shape.ONE_BY_FOUR
				else -> throw IllegalArgumentException("There are no matching room shapes with this set of physical positions: " + segments.toTypedArray().contentToString())
			}
		}
	}

	private fun getPossibleRooms(segmentsX: IntSortedSet, segmentsY: IntSortedSet): List<MutableTriple<Direction, Vector2ic?, List<String>>> {
		val possibleDirectionRooms: List<String> = ArrayList(roomsData!!.keys)
		val possibleRooms: MutableList<MutableTriple<Direction, Vector2ic?, List<String>>> = ArrayList()
		for (direction in getPossibleDirections(segmentsX, segmentsY)) {
			possibleRooms.add(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), possibleDirectionRooms))
		}
		return possibleRooms
	}

	private fun getPossibleDirections(segmentsX: IntSortedSet, segmentsY: IntSortedSet): Array<Direction> {
		return when (shape) {
			Shape.ONE_BY_ONE, Shape.TWO_BY_TWO, Shape.PUZZLE, Shape.TRAP -> Direction.entries.toTypedArray()
			Shape.ONE_BY_TWO, Shape.ONE_BY_THREE, Shape.ONE_BY_FOUR -> {
				if (segmentsX.size > 1 && segmentsY.size == 1) {
					arrayOf(Direction.NW, Direction.SE)
				} else if (segmentsX.size == 1 && segmentsY.size > 1) {
					arrayOf(Direction.NE, Direction.SW)
				}
				throw IllegalArgumentException("Shape " + shape.shape + " does not match segments: " + segments.toTypedArray().contentToString())
			}

			Shape.L_SHAPE -> {
				if (!segments.contains(Vector2i(segmentsX.firstInt(), segmentsY.firstInt()))) {
					arrayOf(Direction.SW)
				} else if (!segments.contains(Vector2i(segmentsX.firstInt(), segmentsY.lastInt()))) {
					arrayOf(Direction.SE)
				} else if (!segments.contains(Vector2i(segmentsX.lastInt(), segmentsY.firstInt()))) {
					arrayOf(Direction.NW)
				} else if (!segments.contains(Vector2i(segmentsX.lastInt(), segmentsY.lastInt()))) {
					arrayOf(Direction.NE)
				}
				throw IllegalArgumentException("Shape " + shape.shape + " does not match segments: " + segments.toTypedArray().contentToString())
			}
		}
	}

	/**
	 * @see .addCustomWaypoint
	 */
	fun addCustomWaypoint(context: CommandContext<FabricClientCommandSource>, pos: BlockPos?) {
		val secretIndex = IntegerArgumentType.getInteger(context, "secretIndex")
		val category = CategoryArgumentType.getCategory(context, "category")
		val waypointName = context.getArgument("name", Text::class.java)
		addCustomWaypoint(secretIndex, category, waypointName, pos)
		context.source.sendFeedback(Constants.PREFIX.append(Text.stringifiedTranslatable("skyblocker.dungeons.secrets.customWaypointAdded", pos!!.x, pos.y, pos.z, name, secretIndex, category, waypointName)))
	}

	/**
	 * Adds a custom waypoint relative to this room to [DungeonManager.customWaypoints] and all existing instances of this room.
	 *
	 * @param secretIndex  the index of the secret waypoint
	 * @param category     the category of the secret waypoint
	 * @param waypointName the name of the secret waypoint
	 * @param pos          the position of the secret waypoint relative to this room
	 */
	private fun addCustomWaypoint(secretIndex: Int, category: SecretWaypoint.Category?, waypointName: Text, pos: BlockPos?) {
		val waypoint = SecretWaypoint(secretIndex, category, waypointName, pos)
		DungeonManager.addCustomWaypoint(name, waypoint)
		DungeonManager.getRoomsStream().filter { r: Room? -> name == r!!.name }.forEach { r: Room? -> r!!.addCustomWaypoint(waypoint) }
	}

	/**
	 * Adds a custom waypoint relative to this room to this instance of the room.
	 *
	 * @param relativeWaypoint the secret waypoint relative to this room to add
	 */
	private fun addCustomWaypoint(relativeWaypoint: SecretWaypoint) {
		val actualWaypoint = relativeWaypoint.relativeToActual(this)
		secretWaypoints!!.put(actualWaypoint.secretIndex, actualWaypoint.pos, actualWaypoint)
	}

	/**
	 * @see .removeCustomWaypoint
	 */
	fun removeCustomWaypoint(context: CommandContext<FabricClientCommandSource>, pos: BlockPos?) {
		val waypoint = removeCustomWaypoint(pos)
		if (waypoint != null) {
			context.source.sendFeedback(Constants.PREFIX.append(Text.stringifiedTranslatable("skyblocker.dungeons.secrets.customWaypointRemoved", pos!!.x, pos.y, pos.z, name, waypoint.secretIndex, waypoint.category, waypoint.name)))
		} else {
			context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.dungeons.secrets.customWaypointNotFound", pos!!.x, pos.y, pos.z, name)))
		}
	}

	/**
	 * Removes a custom waypoint relative to this room from [DungeonManager.customWaypoints] and all existing instances of this room.
	 *
	 * @param pos the position of the secret waypoint relative to this room
	 * @return the removed secret waypoint or `null` if there was no secret waypoint at the given position
	 */
	private fun removeCustomWaypoint(pos: BlockPos?): SecretWaypoint? {
		val waypoint = DungeonManager.removeCustomWaypoint(name, pos)
		if (waypoint != null) {
			DungeonManager.getRoomsStream().filter { r: Room? -> name == r!!.name }.forEach { r: Room? -> r!!.removeCustomWaypoint(waypoint.secretIndex, pos) }
		}
		return waypoint
	}

	/**
	 * Removes a custom waypoint relative to this room from this instance of the room.
	 *
	 * @param secretIndex the index of the secret waypoint
	 * @param relativePos the position of the secret waypoint relative to this room
	 */
	private fun removeCustomWaypoint(secretIndex: Int, relativePos: BlockPos?) {
		val actualPos = relativeToActual(relativePos)
		secretWaypoints!!.remove(secretIndex, actualPos)
	}

	fun <T> addSubProcess(process: T) where T : Tickable?, T : Renderable? {
		tickables.add(process)
		renderables.add(process)
	}

	/**
	 * Updates the room.
	 *
	 *
	 * First, this method tries to find a wither door and blood door.
	 * Then, this method returns immediately if any of the following conditions are met:
	 *
	 *  *  The room does not need to be scanned and matched. (When the room is not of type [Type.ROOM], [Type.PUZZLE], or [Type.TRAP]. See [Type.needsScanning])
	 *  *  The room has been matched or failed to match and is on cooldown. See [.matchState].
	 *  *  [The previous update][.findRoom] has not completed.
	 *
	 * Then this method tries to match this room through:
	 *
	 *  *  Iterate over a 11 by 11 by 11 box around the player.
	 *  *  Check it the block is part of this room and not part of a doorway. See [.segments] and [.notInDoorway].
	 *  *  Checks if the position has been checked and adds it to [.checkedBlocks].
	 *  *  Calls [.checkBlock]
	 *
	 */
	override fun tick(client: MinecraftClient?) {
		if (client!!.world == null) {
			return
		}

		for (tickable in tickables) {
			tickable!!.tick(client)
		}

		// Wither and blood door
		if (SkyblockerConfigManager.config.dungeons.doorHighlight.enableDoorHighlight && doorPos == null) {
			doorPos = DungeonMapUtils.getWitherBloodDoorPos(client.world, segments)
			if (doorPos != null) {
				doorBox = Box(doorPos!!.x.toDouble(), doorPos!!.y.toDouble(), doorPos!!.z.toDouble(), doorPos!!.x + DOOR_SIZE.getX(), doorPos!!.y + DOOR_SIZE.getY(), doorPos!!.z + DOOR_SIZE.getZ())
			}
		}

		// Room scanning and matching
		// Logical AND has higher precedence than logical OR
		if (!type.needsScanning() || matchState != MatchState.MATCHING && matchState != MatchState.DOUBLE_CHECKING || !DungeonManager.isRoomsLoaded() || findRoom != null && !findRoom!!.isDone) {
			return
		}
		val player = client.player ?: return
		findRoom = CompletableFuture.runAsync {
			for (pos in BlockPos.iterate(player.blockPos.add(-5, -5, -5), player.blockPos.add(5, 5, 5))) {
				if (segments.contains(DungeonMapUtils.getPhysicalRoomPos(pos)) && notInDoorway(pos) && checkedBlocks!!.add(pos) && checkBlock(client.world, pos)) {
					break
				}
			}
		}.exceptionally { e: Throwable? ->
			DungeonManager.LOGGER.error("[Skyblocker Dungeon Secrets] Encountered an unknown exception while matching room {}", this, e)
			null
		}
	}

	/**
	 * Filters out dungeon rooms which does not contain the block at the given position.
	 *
	 *
	 * This method:
	 *
	 *  *  Checks if the block type is included in the dungeon rooms data. See [DungeonManager.NUMERIC_ID].
	 *  *  For each possible direction:
	 *
	 *  *  Rotate and convert the position to a relative position. See [DungeonMapUtils.actualToRelative].
	 *  *  Encode the block based on the relative position and the custom numeric block id. See [.posIdToInt].
	 *  *  For each possible room in the current direction:
	 *
	 *  *  Check if [.roomsData] contains the encoded block.
	 *  *  If so, add the room to the new list of possible rooms for this direction.
	 *
	 *  *  Replace the old possible room list for the current direction with the new one.
	 *
	 *  *  If there are no matching rooms left:
	 *
	 *  *  Terminate matching by setting [.matchState] to [TriState.FALSE].
	 *  *  Schedule another matching attempt in 50 ticks (2.5 seconds).
	 *  *  Reset [.possibleRooms] and [.checkedBlocks] with [.reset].
	 *  *  Return `true`
	 *
	 *  *  If there are exactly one room matching:
	 *
	 *  *  If [.matchState] is [MatchState.MATCHING]:
	 *
	 *  *  Call [.roomMatched].
	 *  *  Return `false`.
	 *
	 *  *  If [.matchState] is [MatchState.DOUBLE_CHECKING]:
	 *
	 *  *  Set the match state to [MatchState.MATCHED].
	 *  *  Discard the no longer needed fields to save memory.
	 *  *  Return `true`.
	 *
	 *
	 *  *  Return `false`
	 *
	 *
	 * @param world the world to get the block from
	 * @param pos   the position of the block to check
	 * @return whether room matching should end. Either a match is found or there are no valid rooms left
	 */
	protected open fun checkBlock(world: ClientWorld?, pos: BlockPos): Boolean {
		val id = DungeonManager.NUMERIC_ID.getByte(Registries.BLOCK.getId(world!!.getBlockState(pos).block).toString())
		if (id.toInt() == 0) {
			return false
		}
		for (directionRooms in possibleRooms!!) {
			val block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getLeft(), directionRooms.getMiddle(), pos), id)
			val possibleDirectionRooms: MutableList<String> = ArrayList()
			for (room in directionRooms.getRight()) {
				if (Arrays.binarySearch(roomsData!![room], block) >= 0) {
					possibleDirectionRooms.add(room)
				}
			}
			directionRooms.setRight(possibleDirectionRooms)
		}

		val matchingRoomsSize = possibleRooms!!.stream().map { obj: MutableTriple<Direction, Vector2ic?, List<String>> -> obj.getRight() }.mapToInt { obj: List<String> -> obj.size }.sum()
		if (matchingRoomsSize == 0) synchronized(this) {
			// If no rooms match, reset the fields and scan again after 50 ticks.
			matchState = MatchState.FAILED
			DungeonManager.LOGGER.warn("[Skyblocker Dungeon Secrets] No dungeon room matched after checking {} block(s) including double checking {} block(s)", checkedBlocks!!.size, doubleCheckBlocks)
			Scheduler.INSTANCE.schedule({ matchState = MatchState.MATCHING }, 50)
			reset()
			return true
		} else if (matchingRoomsSize == 1) {
			if (matchState == MatchState.MATCHING) {
				// If one room matches, load the secrets for that room and set state to double-checking.
				val directionRoom: Triple<Direction, Vector2ic?, List<String>> = possibleRooms!!.stream().filter { directionRooms: MutableTriple<Direction, Vector2ic?, List<String>> -> directionRooms.getRight().size == 1 }.findAny().orElseThrow()
				name = directionRoom.right.first
				direction = directionRoom.left
				physicalCornerPos = directionRoom.middle
				DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} matched after checking {} block(s), starting double checking", name, checkedBlocks!!.size)
				roomMatched()
				return false
			} else if (matchState == MatchState.DOUBLE_CHECKING && ++doubleCheckBlocks >= 10) {
				// If double-checked, set state to matched and discard the no longer needed fields.
				matchState = MatchState.MATCHED
				DungeonEvents.ROOM_MATCHED.invoker().onRoomMatched(this)
				DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} confirmed after checking {} block(s) including double checking {} block(s)", name, checkedBlocks!!.size, doubleCheckBlocks)
				discard()
				return true
			}
			return false
		} else {
			DungeonManager.LOGGER.debug("[Skyblocker Dungeon Secrets] {} room(s) remaining after checking {} block(s)", matchingRoomsSize, checkedBlocks!!.size)
			return false
		}
	}

	/**
	 * Encodes a [BlockPos] and the custom numeric block id into an integer.
	 *
	 * @param pos the position of the block
	 * @param id  the custom numeric block id
	 * @return the encoded integer
	 */
	protected fun posIdToInt(pos: BlockPos?, id: Byte): Int {
		return pos!!.x shl 24 or (pos.y shl 16) or (pos.z shl 8) or id.toInt()
	}

	/**
	 * Loads the secret waypoints for the room from [DungeonManager.waypointsJson] once it has been matched
	 * and sets [.matchState] to [MatchState.DOUBLE_CHECKING].
	 *
	 * @param directionRooms the direction, position, and name of the room
	 */
	private fun roomMatched() {
		secretWaypoints = HashBasedTable.create()
		val secretWaypointsJson = DungeonManager.getRoomWaypoints(name)
		if (secretWaypointsJson != null) {
			for (waypointElement in secretWaypointsJson) {
				val waypoint = waypointElement.asJsonObject
				val secretName = waypoint["secretName"].asString
				val secretIndexMatcher = SECRET_INDEX.matcher(secretName)
				val secretIndex = if (secretIndexMatcher.find()) secretIndexMatcher.group(1).toInt() else 0
				val pos = DungeonMapUtils.relativeToActual(direction, physicalCornerPos, waypoint)
				secretWaypoints.put(secretIndex, pos, SecretWaypoint(secretIndex, waypoint, secretName, pos))
			}
		}
		DungeonManager.getCustomWaypoints(name).values.forEach(Consumer { relativeWaypoint: SecretWaypoint -> this.addCustomWaypoint(relativeWaypoint) })
		matchState = MatchState.DOUBLE_CHECKING
	}

	/**
	 * Resets fields for another round of matching after room matching fails.
	 */
	protected fun reset() {
		val segmentsX = IntSortedSets.unmodifiable(IntRBTreeSet(segments.stream().mapToInt { obj: Vector2ic -> obj.x() }.toArray()))
		val segmentsY = IntSortedSets.unmodifiable(IntRBTreeSet(segments.stream().mapToInt { obj: Vector2ic -> obj.y() }.toArray()))
		possibleRooms = getPossibleRooms(segmentsX, segmentsY)
		checkedBlocks = HashSet()
		doubleCheckBlocks = 0
		secretWaypoints = null
		name = null
		direction = null
		physicalCornerPos = null
	}

	/**
	 * Discards fields after room matching completes when a room is found.
	 * These fields are no longer needed and are discarded to save memory.
	 */
	private fun discard() {
		roomsData = null
		possibleRooms = null
		checkedBlocks = null
		doubleCheckBlocks = 0
	}

	/**
	 * Fails if ![.isMatched]
	 */
	fun actualToRelative(pos: BlockPos): BlockPos? {
		return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos)
	}

	/**
	 * Fails if ![.isMatched]
	 */
	fun relativeToActual(pos: BlockPos?): BlockPos? {
		return DungeonMapUtils.relativeToActual(direction, physicalCornerPos, pos)
	}

	/**
	 * Calls [SecretWaypoint.render] on [all secret waypoints][.secretWaypoints] and renders a highlight around the wither or blood door, if it exists.
	 */
	override fun render(context: WorldRenderContext?) {
		for (renderable in renderables) {
			renderable!!.render(context)
		}

		synchronized(this) {
			if (SkyblockerConfigManager.config.dungeons.secretWaypoints.enableSecretWaypoints && isMatched) {
				for (secretWaypoint in secretWaypoints!!.values()) {
					if (secretWaypoint.shouldRender()) {
						secretWaypoint.render(context)
					}
				}
			}
		}

		if (!SkyblockerConfigManager.config.dungeons.doorHighlight.enableDoorHighlight || doorPos == null) {
			return
		}
		val colorComponents = if (keyFound) GREEN_COLOR_COMPONENTS else RED_COLOR_COMPONENTS
		when (SkyblockerConfigManager.config.dungeons.doorHighlight.doorHighlightType) {
			DungeonsConfig.DoorHighlight.Type.HIGHLIGHT -> renderFilled(context!!, doorPos!!, DOOR_SIZE, colorComponents, 0.5f, true)
			DungeonsConfig.DoorHighlight.Type.OUTLINED_HIGHLIGHT -> {
				renderFilled(context!!, doorPos!!, DOOR_SIZE, colorComponents, 0.5f, true)
				renderOutline(context, doorBox, colorComponents, 5f, true)
			}

			DungeonsConfig.DoorHighlight.Type.OUTLINE -> renderOutline(context!!, doorBox, colorComponents, 5f, true)
		}
	}

	/**
	 * Sets all secrets as found if [.isAllSecretsFound] and sets [.lastChestSecret] as missing if message equals [.LOCKED_CHEST].
	 */
	fun onChatMessage(message: String) {
		if (isAllSecretsFound(message)) {
			secretWaypoints!!.values().forEach(Consumer { obj: SecretWaypoint -> obj.setFound() })
		} else if (LOCKED_CHEST == message && lastChestSecretTime + 1000 > System.currentTimeMillis() && lastChestSecret != null) {
			secretWaypoints!!.column(lastChestSecret).values.stream().filter { obj: SecretWaypoint -> obj.needsInteraction() }.findAny()
				.ifPresent { secretWaypoint: SecretWaypoint -> markSecretsAndLogInfo(secretWaypoint, false, "[Skyblocker Dungeon Secrets] Detected locked chest interaction, setting secret #{} as missing", secretWaypoint.secretIndex) }
		}
	}

	/**
	 * Marks the secret at the interaction position as found when the player interacts with a chest, player head, or lever
	 * if there is a secret at the interaction position and saves the position to [.lastChestSecret] if the block is a chest.
	 *
	 * @param world the world to get the block from
	 * @param pos   the position of the block being interacted with
	 * @see .markSecretsFoundAndLogInfo
	 */
	fun onUseBlock(world: World, pos: BlockPos?) {
		val state = world.getBlockState(pos)
		if ((state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) && lastChestSecretTime + 1000 < System.currentTimeMillis() || state.isOf(Blocks.PLAYER_HEAD) || state.isOf(Blocks.PLAYER_WALL_HEAD)) {
			secretWaypoints!!.column(pos).values.stream().filter { obj: SecretWaypoint -> obj.needsInteraction() }.findAny()
				.ifPresent { secretWaypoint: SecretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} interaction, setting secret #{} as found", secretWaypoint.category!!, secretWaypoint.secretIndex) }
			if (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) {
				lastChestSecret = pos
				lastChestSecretTime = System.currentTimeMillis()
			}
		} else if (state.isOf(Blocks.LEVER)) {
			secretWaypoints!!.column(pos).values.stream().filter { obj: SecretWaypoint -> obj.isLever }.forEach { obj: SecretWaypoint -> obj.setFound() }
		}
	}

	/**
	 * Marks the closest secret that requires item pickup no greater than 6 blocks away as found when a secret item is removed from the world.
	 *
	 * @param itemEntity the item entity being picked up
	 * @see .markSecretsFoundAndLogInfo
	 */
	fun onItemPickup(itemEntity: ItemEntity) {
		if (SecretWaypoint.Companion.SECRET_ITEMS.stream().noneMatch(Predicate<String> { s: String? -> itemEntity.stack.name.string.contains(s!!) })) {
			return
		}
		secretWaypoints!!.values().stream().filter { obj: SecretWaypoint -> obj.needsItemPickup() }.min(Comparator.comparingDouble<SecretWaypoint>(SecretWaypoint.Companion.getSquaredDistanceToFunction(itemEntity))).filter(SecretWaypoint.Companion.getRangePredicate(itemEntity))
			.ifPresent { secretWaypoint: SecretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected item {} removed from a {} secret, setting secret #{} as found", itemEntity.name.string, secretWaypoint.category!!, secretWaypoint.secretIndex) }
	}

	/**
	 * Marks the closest bat secret as found when a bat is killed.
	 *
	 * @param bat the bat being killed
	 * @see .markSecretsFoundAndLogInfo
	 */
	fun onBatRemoved(bat: AmbientEntity) {
		secretWaypoints!!.values().stream().filter { obj: SecretWaypoint -> obj.isBat }.min(Comparator.comparingDouble<SecretWaypoint>(SecretWaypoint.Companion.getSquaredDistanceToFunction(bat)))
			.ifPresent { secretWaypoint: SecretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} killed for a {} secret, setting secret #{} as found", bat.name.string, secretWaypoint.category!!, secretWaypoint.secretIndex) }
	}

	/**
	 * Marks all secret waypoints with the same index as the given [SecretWaypoint] as found and logs the given message.
	 *
	 * @param secretWaypoint the secret waypoint to read the index from.
	 * @param msg            the message to log
	 * @param args           the args for the [Logger#info(String, Object...)][org.slf4j.Logger.info] call
	 */
	private fun markSecretsFoundAndLogInfo(secretWaypoint: SecretWaypoint, msg: String, vararg args: Any) {
		markSecretsAndLogInfo(secretWaypoint, true, msg, *args)
	}

	/**
	 * Marks all secret waypoints with the same index as the given [SecretWaypoint] as found or missing and logs the given message.
	 * @param secretWaypoint the secret waypoint to read the index from.
	 * @param found          whether to mark the secret as found or missing
	 * @param msg            the message to log
	 * @param args           the args for the [Logger#info(String, Object...)][org.slf4j.Logger.info] call
	 */
	private fun markSecretsAndLogInfo(secretWaypoint: SecretWaypoint, found: Boolean, msg: String, vararg args: Any) {
		markSecrets(secretWaypoint.secretIndex, found)
		DungeonManager.LOGGER.info(msg, *args)
	}

	fun markSecrets(secretIndex: Int, found: Boolean): Boolean {
		val secret = secretWaypoints!!.row(secretIndex)
		if (secret.isEmpty()) {
			return false
		} else {
			secret.values.forEach(if (found) Consumer { obj: SecretWaypoint -> obj.setFound() } else Consumer { obj: SecretWaypoint -> obj.setMissing() })
			return true
		}
	}

	fun keyFound() {
		if (nextRoom != null && nextRoom!!.type == Type.FAIRY) {
			nextRoom!!.keyFound = true
		}
		keyFound = true
	}

	enum class Type(val color: Byte) {
		ENTRANCE(MapColor.DARK_GREEN.getRenderColorByte(MapColor.Brightness.HIGH)),
		ROOM(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.LOWEST)),
		PUZZLE(MapColor.MAGENTA.getRenderColorByte(MapColor.Brightness.HIGH)),
		TRAP(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.HIGH)),
		MINIBOSS(MapColor.YELLOW.getRenderColorByte(MapColor.Brightness.HIGH)),
		FAIRY(MapColor.PINK.getRenderColorByte(MapColor.Brightness.HIGH)),
		BLOOD(MapColor.BRIGHT_RED.getRenderColorByte(MapColor.Brightness.HIGH)),
		UNKNOWN(MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL));

		/**
		 * @return whether this room type has secrets and needs to be scanned and matched.
		 */
		fun needsScanning(): Boolean {
			return when (this) {
				ROOM, PUZZLE, TRAP -> true
				else -> false
			}
		}
	}

	enum class Shape(val shape: String) {
		ONE_BY_ONE("1x1"),
		ONE_BY_TWO("1x2"),
		ONE_BY_THREE("1x3"),
		ONE_BY_FOUR("1x4"),
		L_SHAPE("L-shape"),
		TWO_BY_TWO("2x2"),
		PUZZLE("puzzle"),
		TRAP("trap");

		override fun toString(): String {
			return shape
		}
	}

	enum class Direction(override val name: String) : StringIdentifiable {
		NW("northwest"), NE("northeast"), SW("southwest"), SE("southeast");

		override fun asString(): String {
			return name
		}

		internal object DirectionArgumentType : EnumArgumentType<Direction?>() {
			fun direction(): DirectionArgumentType {
				return DirectionArgumentType()
			}

			fun <S> getDirection(context: CommandContext<S>, name: String?): Direction {
				return context.getArgument(name, Direction::class.java)
			}
		}

		companion object {
			private val CODEC: Codec<Direction> = StringIdentifiable.createCodec { entries.toTypedArray() }
		}
	}

	protected enum class MatchState {
		MATCHING, DOUBLE_CHECKING, MATCHED, FAILED
	}

	companion object {
		private val SECRET_INDEX: Pattern = Pattern.compile("^(\\d+)")
		private val SECRETS: Pattern = Pattern.compile("§7(\\d{1,2})/(\\d{1,2}) Secrets")
		private const val LOCKED_CHEST = "That chest is locked!"
		private val DOOR_SIZE = Vec3d(3.0, 4.0, 3.0)
		protected val RED_COLOR_COMPONENTS: FloatArray = floatArrayOf(1f, 0f, 0f)
		protected val GREEN_COLOR_COMPONENTS: FloatArray = floatArrayOf(0f, 1f, 0f)
		private fun notInDoorway(pos: BlockPos): Boolean {
			if (pos.y < 66 || pos.y > 73) {
				return true
			}
			val x = Math.floorMod(pos.x - 8, 32)
			val z = Math.floorMod(pos.z - 8, 32)
			return (x < 13 || x > 17 || z > 2 && z < 28) && (z < 13 || z > 17 || x > 2 && x < 28)
		}

		/**
		 * Checks if the number of found secrets is equals or greater than the total number of secrets in the room.
		 *
		 * @param message the message to check in
		 * @return whether the number of found secrets is equals or greater than the total number of secrets in the room
		 */
		fun isAllSecretsFound(message: String?): Boolean {
			val matcher = SECRETS.matcher(message)
			if (matcher.find()) {
				return matcher.group(1).toInt() >= matcher.group(2).toInt()
			}
			return false
		}
	}
}
