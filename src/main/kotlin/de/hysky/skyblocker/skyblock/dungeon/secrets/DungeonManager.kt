package de.hysky.skyblocker.skyblock.dungeon.secrets

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.DungeonsConfig.SecretWaypoints
import de.hysky.skyblocker.debug.Debug
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss.Companion.fromMessage
import de.hysky.skyblocker.skyblock.dungeon.DungeonMap.getMapIdComponent
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room.Direction.DirectionArgumentType
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint.Category.CategoryArgumentType
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Tickable
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.objects.Object2ByteMap
import it.unimi.dsi.fastutil.objects.Object2ByteMaps
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.GameCanceled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.PosArgument
import net.minecraft.command.argument.TextArgumentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.mob.AmbientEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FilledMapItem
import net.minecraft.item.Items
import net.minecraft.item.map.MapState
import net.minecraft.resource.Resource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import org.jetbrains.annotations.Contract
import org.joml.Vector2ic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.ObjectInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.regex.Pattern
import java.util.stream.Stream
import java.util.zip.InflaterInputStream

object DungeonManager {
	val LOGGER: Logger = LoggerFactory.getLogger(DungeonManager::class.java)
	private const val DUNGEONS_PATH = "dungeons"
	private var CUSTOM_WAYPOINTS_DIR: Path? = null
	private val KEY_FOUND: Pattern = Pattern.compile("^(?:\\[.+] )?(?<name>\\w+) has obtained (?<type>Wither|Blood) Key!$")

	/**
	 * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
	 *
	 * @implNote Not using [Registry#getId(Block)][Registry.getId] and [Blocks] since this is also used by [DungeonRoomsDFU][de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU], which runs outside of Minecraft.
	 */
	val NUMERIC_ID: Object2ByteMap<String?> = Object2ByteMaps.unmodifiable(
		Object2ByteOpenHashMap(
			java.util.Map.ofEntries(
				java.util.Map.entry("minecraft:stone", 1.toByte()),
				java.util.Map.entry("minecraft:diorite", 2.toByte()),
				java.util.Map.entry("minecraft:polished_diorite", 3.toByte()),
				java.util.Map.entry("minecraft:andesite", 4.toByte()),
				java.util.Map.entry("minecraft:polished_andesite", 5.toByte()),
				java.util.Map.entry("minecraft:grass_block", 6.toByte()),
				java.util.Map.entry("minecraft:dirt", 7.toByte()),
				java.util.Map.entry("minecraft:coarse_dirt", 8.toByte()),
				java.util.Map.entry("minecraft:cobblestone", 9.toByte()),
				java.util.Map.entry("minecraft:bedrock", 10.toByte()),
				java.util.Map.entry("minecraft:oak_leaves", 11.toByte()),
				java.util.Map.entry("minecraft:gray_wool", 12.toByte()),
				java.util.Map.entry("minecraft:double_stone_slab", 13.toByte()),
				java.util.Map.entry("minecraft:mossy_cobblestone", 14.toByte()),
				java.util.Map.entry("minecraft:clay", 15.toByte()),
				java.util.Map.entry("minecraft:stone_bricks", 16.toByte()),
				java.util.Map.entry("minecraft:mossy_stone_bricks", 17.toByte()),
				java.util.Map.entry("minecraft:chiseled_stone_bricks", 18.toByte()),
				java.util.Map.entry("minecraft:gray_terracotta", 19.toByte()),
				java.util.Map.entry("minecraft:cyan_terracotta", 20.toByte()),
				java.util.Map.entry("minecraft:black_terracotta", 21.toByte())
			)
		)
	)

	/**
	 * Block data for dungeon rooms. See [DungeonRoomsDFU][de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU] for format details and how it's generated.
	 * All access to this map must check [.isRoomsLoaded] to prevent concurrent modification.
	 */
	val ROOMS_DATA: HashMap<String, MutableMap<String?, MutableMap<String, IntArray>>> = HashMap()
	private val rooms: MutableMap<Vector2ic, Room?> = HashMap()
	private val roomsJson: MutableMap<String?, JsonElement> = HashMap()
	private val waypointsJson: MutableMap<String?, JsonElement> = HashMap()

	/**
	 * The map of dungeon room names to custom waypoints relative to the room.
	 */
	private val customWaypoints: Table<String?, BlockPos?, SecretWaypoint> = HashBasedTable.create()
	private var roomsLoaded: CompletableFuture<Void>? = null

	/**
	 * The map position of the top left corner of the entrance room.
	 */
	private var mapEntrancePos: Vector2ic? = null

	/**
	 * The size of a room on the map.
	 */
	private var mapRoomSize = 0

	/**
	 * The physical position of the northwest corner of the entrance room.
	 */
	private var physicalEntrancePos: Vector2ic? = null

	/**
	 * not null if [.isCurrentRoomMatched]
	 */
    @JvmStatic
    var currentRoom: Room? = null
		private set
	var boss: DungeonBoss = DungeonBoss.NONE
		private set

	fun isRoomsLoaded(): Boolean {
		return roomsLoaded != null && roomsLoaded!!.isDone
	}

	val roomsStream: Stream<Room?>
		get() = rooms.values.stream()

	@Suppress("unused")
	fun getRoomMetadata(room: String?): JsonObject? {
		val value = roomsJson[room]
		return value?.asJsonObject
	}

	fun getRoomWaypoints(room: String?): JsonArray? {
		val value = waypointsJson[room]
		return value?.asJsonArray
	}

	/**
	 * @see .customWaypoints
	 */
	fun getCustomWaypoints(room: String?): Map<BlockPos?, SecretWaypoint> {
		return customWaypoints.row(room)
	}

	/**
	 * @see .customWaypoints
	 */
	fun addCustomWaypoint(room: String?, waypoint: SecretWaypoint): SecretWaypoint? {
		return customWaypoints.put(room, waypoint.pos, waypoint)
	}

	/**
	 * @see .customWaypoints
	 */
	fun addCustomWaypoints(room: String?, waypoints: Collection<SecretWaypoint>) {
		for (waypoint in waypoints) {
			addCustomWaypoint(room, waypoint)
		}
	}

	/**
	 * @see .customWaypoints
	 */
	fun removeCustomWaypoint(room: String?, pos: BlockPos?): SecretWaypoint? {
		return customWaypoints.remove(room, pos)
	}

	val isInBoss: Boolean
		get() = boss.isInBoss

	/**
	 * Loads the dungeon secrets asynchronously from `/assets/skyblocker/dungeons`.
	 * Use [.isRoomsLoaded] to check for completion of loading.
	 */
	fun init() {
		CUSTOM_WAYPOINTS_DIR = SkyblockerMod.CONFIG_DIR.resolve("custom_secret_waypoints.json")
		if (!SkyblockerConfigManager.get().dungeons.secretWaypoints.enableRoomMatching) {
			return
		}
		// Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
		CompletableFuture.runAsync(Runnable { obj: DungeonManager? -> load() }, MinecraftClient.getInstance()).exceptionally { e: Throwable? ->
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e)
			null
		}
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { obj: MinecraftClient? -> saveCustomWaypoints() })
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: DungeonManager? -> update() }, 5)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onChatMessage(text) })
		ClientReceiveMessageEvents.GAME_CANCELED.register(GameCanceled { obj: Text?, text: Boolean -> onChatMessage(text) })
		UseBlockCallback.EVENT.register(UseBlockCallback { player: PlayerEntity?, world: World, hand: Hand?, hitResult: BlockHitResult -> onUseBlock(world, hitResult) })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess ->
			dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(
					ClientCommandManager.literal("dungeons").then(
						ClientCommandManager.literal("secrets")
							.then(ClientCommandManager.literal("markAsFound").then(markSecretsCommand(true)))
							.then(ClientCommandManager.literal("markAsMissing").then(markSecretsCommand(false)))
							.then(ClientCommandManager.literal("getRelativePos").executes(Command { obj: CommandContext<FabricClientCommandSource?>? -> getRelativePos() }))
							.then(ClientCommandManager.literal("getRelativeTargetPos").executes { obj: CommandContext<FabricClientCommandSource?>? -> getRelativeTargetPos() })
							.then(ClientCommandManager.literal("addWaypoint").then(addCustomWaypointCommand(false, registryAccess)))
							.then(ClientCommandManager.literal("addWaypointRelatively").then(addCustomWaypointCommand(true, registryAccess)))
							.then(ClientCommandManager.literal("removeWaypoint").then(removeCustomWaypointCommand(false)))
							.then(ClientCommandManager.literal("removeWaypointRelatively").then(removeCustomWaypointCommand(true)))
					)
				)
			)
		})
		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
				dispatcher.register(
					ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(
						ClientCommandManager.literal("dungeons").then(
							ClientCommandManager.literal("secrets")
								.then(ClientCommandManager.literal("matchAgainst").then(matchAgainstCommand()))
								.then(ClientCommandManager.literal("clearSubProcesses").executes { context: CommandContext<FabricClientCommandSource> ->
									if (currentRoom != null) {
										currentRoom!!.tickables.clear()
										currentRoom!!.renderables.clear()
										context.source.sendFeedback(Constants.PREFIX.get().append("§rCleared sub processes in the current room."))
									} else {
										context.source.sendError(Constants.PREFIX.get().append("§cCurrent room is null."))
									}
									Command.SINGLE_SUCCESS
								})
						)
					)
				)
			})
		}
		ClientPlayConnectionEvents.JOIN.register((ClientPlayConnectionEvents.Join { handler: ClientPlayNetworkHandler?, sender: PacketSender?, client: MinecraftClient? -> reset() }))
	}

	private fun load() {
		val startTime = System.currentTimeMillis()
		val dungeonFutures: MutableList<CompletableFuture<Void>> = java.util.ArrayList()
		for ((key, value) in MinecraftClient.getInstance().resourceManager.findResources(DUNGEONS_PATH) { id: Identifier -> id.path.endsWith(".skeleton") }) {
			val path = key.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			if (path.size != 4) {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets, invalid resource identifier {}", key)
				break
			}
			val dungeon = path[1]
			val roomShape = path[2]
			val room = path[3].substring(0, path[3].length - ".skeleton".length)
			ROOMS_DATA.computeIfAbsent(dungeon, Function<String, MutableMap<String?, MutableMap<String, IntArray>>> { dungeonKey: String? -> HashMap<String?, Map<String, IntArray>>() })
			ROOMS_DATA[dungeon]!!.computeIfAbsent(roomShape) { roomShapeKey: String? -> HashMap() }
			dungeonFutures.add(CompletableFuture.supplyAsync { readRoom(value) }.thenAcceptAsync { rooms: IntArray ->
				val roomsMap = ROOMS_DATA[dungeon]!![roomShape]!!
				synchronized(roomsMap) {
					roomsMap.put(room, rooms)
				}
				LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room)
			}.exceptionally { e: Throwable? ->
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room, e)
				null
			})
		}
		dungeonFutures.add(CompletableFuture.runAsync {
			try {
				MinecraftClient.getInstance().resourceManager.openAsReader(Identifier(SkyblockerMod.NAMESPACE, "dungeons/dungeonrooms.json")).use { roomsReader ->
					MinecraftClient.getInstance().resourceManager.openAsReader(Identifier(SkyblockerMod.NAMESPACE, "dungeons/secretlocations.json")).use { waypointsReader ->
						loadJson(roomsReader, roomsJson)
						loadJson(waypointsReader, waypointsJson)
						LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon secret waypoints json")
					}
				}
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secret waypoints json", e)
			}
		})
		dungeonFutures.add(CompletableFuture.runAsync {
			try {
				Files.newBufferedReader(CUSTOM_WAYPOINTS_DIR).use { customWaypointsReader ->
					SkyblockerMod.GSON.fromJson<JsonObject>(customWaypointsReader, JsonObject::class.java).asMap().forEach { (room: String?, waypointsJson: JsonElement) -> addCustomWaypoints(room, SecretWaypoint.Companion.LIST_CODEC.parse<JsonElement>(JsonOps.INSTANCE, waypointsJson).resultOrPartial(Consumer<String> { msg: String? -> LOGGER.error(msg) }).orElseGet(Supplier<List<SecretWaypoint>> { ArrayList() })) }
					LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded custom dungeon secret waypoints")
				}
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load custom dungeon secret waypoints", e)
			}
		})
		roomsLoaded = CompletableFuture.allOf(*dungeonFutures.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).thenRun {
			LOGGER.info("[Skyblocker Dungeon Secrets] Loaded dungeon secrets for {} dungeon(s), {} room shapes, {} rooms, and {} custom secret waypoints total in {} ms", ROOMS_DATA.size, ROOMS_DATA.values.stream().mapToInt { obj: Map<String?, Map<String, IntArray>> -> obj.size }.sum(), ROOMS_DATA.values.stream().map { obj: Map<String?, Map<String, IntArray>> -> obj.values }.flatMap { obj: Collection<Map<String, IntArray>> -> obj.stream() }.mapToInt { obj: Map<String, IntArray> -> obj.size }
				.sum(), customWaypoints.size(), System.currentTimeMillis() - startTime)
		}.exceptionally { e: Throwable? ->
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e)
			null
		}
		LOGGER.info("[Skyblocker Dungeon Secrets] Started loading dungeon secrets in (blocked main thread for) {} ms", System.currentTimeMillis() - startTime)
	}

	private fun saveCustomWaypoints(client: MinecraftClient) {
		try {
			Files.newBufferedWriter(CUSTOM_WAYPOINTS_DIR).use { writer ->
				val customWaypointsJson = JsonObject()
				customWaypoints.rowMap().forEach { (room: String?, waypoints: Map<BlockPos?, SecretWaypoint>) -> customWaypointsJson.add(room, SecretWaypoint.Companion.LIST_CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, java.util.ArrayList<SecretWaypoint>(waypoints.values)).resultOrPartial(Consumer<String> { msg: String? -> LOGGER.error(msg) }).orElseGet(Supplier<JsonElement> { JsonArray() })) }
				SkyblockerMod.GSON.toJson(customWaypointsJson, writer)
				LOGGER.info("[Skyblocker Dungeon Secrets] Saved custom dungeon secret waypoints")
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to save custom dungeon secret waypoints", e)
		}
	}

	@Throws(RuntimeException::class)
	private fun readRoom(resource: Resource): IntArray {
		try {
			ObjectInputStream(InflaterInputStream(resource.inputStream)).use { `in` ->
				return `in`.readObject() as IntArray
			}
		} catch (e: IOException) {
			throw RuntimeException(e)
		} catch (e: ClassNotFoundException) {
			throw RuntimeException(e)
		}
	}

	/**
	 * Loads the json from the given [BufferedReader] into the given [Map].
	 *
	 * @param reader the reader to read the json from
	 * @param map    the map to load into
	 */
	private fun loadJson(reader: BufferedReader, map: MutableMap<String?, JsonElement>) {
		SkyblockerMod.GSON.fromJson(reader, JsonObject::class.java).asMap().forEach { (room: String, jsonElement: JsonElement) -> map[room.lowercase(Locale.getDefault()).replace(" ".toRegex(), "-")] = jsonElement }
	}

	private fun markSecretsCommand(found: Boolean): RequiredArgumentBuilder<FabricClientCommandSource, Int> {
		return ClientCommandManager.argument("secretIndex", IntegerArgumentType.integer()).executes { context: CommandContext<FabricClientCommandSource> ->
			val secretIndex = IntegerArgumentType.getInteger(context, "secretIndex")
			if (markSecrets(secretIndex, found)) {
				context.source.sendFeedback(Constants.PREFIX.get().append(Text.translatable(if (found) "skyblocker.dungeons.secrets.markSecretFound" else "skyblocker.dungeons.secrets.markSecretMissing", secretIndex)))
			} else {
				context.source.sendError(Constants.PREFIX.get().append(Text.translatable(if (found) "skyblocker.dungeons.secrets.markSecretFoundUnable" else "skyblocker.dungeons.secrets.markSecretMissingUnable", secretIndex)))
			}
			Command.SINGLE_SUCCESS
		}
	}

	private fun getRelativePos(context: CommandContext<FabricClientCommandSource>): Int {
		return getRelativePos(context.source, context.source.player.blockPos)
	}

	private fun getRelativeTargetPos(context: CommandContext<FabricClientCommandSource>): Int {
		if (MinecraftClient.getInstance().crosshairTarget is BlockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
			return getRelativePos(context.source, blockHitResult.getBlockPos())
		} else {
			context.source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.noTarget")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun getRelativePos(source: FabricClientCommandSource, pos: BlockPos): Int {
		val room = getRoomAtPhysical(pos)
		if (isRoomMatched(room)) {
			val relativePos = currentRoom!!.actualToRelative(pos)
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.posMessage", currentRoom.getName(), currentRoom.getDirection().asString(), relativePos!!.x, relativePos.y, relativePos.z)))
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun addCustomWaypointCommand(relative: Boolean, registryAccess: CommandRegistryAccess): RequiredArgumentBuilder<FabricClientCommandSource, PosArgument> {
		return ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
			.then(
				ClientCommandManager.argument("secretIndex", IntegerArgumentType.integer())
					.then(
						ClientCommandManager.argument("category", CategoryArgumentType.category())
							.then(ClientCommandManager.argument("name", TextArgumentType.text(registryAccess)).executes { context: CommandContext<FabricClientCommandSource> ->
								// TODO Less hacky way with custom ClientBlockPosArgumentType
								val pos = context.getArgument("pos", PosArgument::class.java).toAbsoluteBlockPos(ServerCommandSource(null, context.source.position, context.source.rotation, null, 0, null, null, null, null))
								if (relative) addCustomWaypointRelative(context, pos) else addCustomWaypoint(context, pos)
							})
					)
			)
	}

	private fun addCustomWaypoint(context: CommandContext<FabricClientCommandSource>, pos: BlockPos): Int {
		val room = getRoomAtPhysical(pos)
		if (isRoomMatched(room)) {
			room!!.addCustomWaypoint(context, room.actualToRelative(pos))
		} else {
			context.source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun addCustomWaypointRelative(context: CommandContext<FabricClientCommandSource>, pos: BlockPos): Int {
		if (isCurrentRoomMatched) {
			currentRoom!!.addCustomWaypoint(context, pos)
		} else {
			context.source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun removeCustomWaypointCommand(relative: Boolean): RequiredArgumentBuilder<FabricClientCommandSource, PosArgument> {
		return ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
			.executes { context: CommandContext<FabricClientCommandSource> ->
				// TODO Less hacky way with custom ClientBlockPosArgumentType
				val pos = context.getArgument("pos", PosArgument::class.java).toAbsoluteBlockPos(ServerCommandSource(null, context.source.position, context.source.rotation, null, 0, null, null, null, null))
				if (relative) removeCustomWaypointRelative(context, pos) else removeCustomWaypoint(context, pos)
			}
	}

	private fun removeCustomWaypoint(context: CommandContext<FabricClientCommandSource>, pos: BlockPos): Int {
		val room = getRoomAtPhysical(pos)
		if (isRoomMatched(room)) {
			room!!.removeCustomWaypoint(context, room.actualToRelative(pos))
		} else {
			context.source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun removeCustomWaypointRelative(context: CommandContext<FabricClientCommandSource>, pos: BlockPos): Int {
		if (isCurrentRoomMatched) {
			currentRoom!!.removeCustomWaypoint(context, pos)
		} else {
			context.source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")))
		}
		return Command.SINGLE_SUCCESS
	}

	private fun matchAgainstCommand(): RequiredArgumentBuilder<FabricClientCommandSource, String> {
		return ClientCommandManager.argument("room", StringArgumentType.string()).suggests { context: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(ROOMS_DATA.values.stream().map { obj: Map<String?, Map<String, IntArray>> -> obj.values }.flatMap { obj: Collection<Map<String, IntArray>> -> obj.stream() }.map { obj: Map<String, IntArray> -> obj.keys }.flatMap { obj: Set<String> -> obj.stream() }, builder) }
			.then(ClientCommandManager.argument("direction", DirectionArgumentType.direction()).executes { context: CommandContext<FabricClientCommandSource> ->
				if (physicalEntrancePos == null || mapEntrancePos == null || mapRoomSize == 0) {
					context.source.sendError(Constants.PREFIX.get().append("§cYou are not in a dungeon."))
					return@executes Command.SINGLE_SUCCESS
				}
				val client = MinecraftClient.getInstance()
				if (client.player == null || client.world == null) {
					context.source.sendError(Constants.PREFIX.get().append("§cFailed to get player or world."))
					return@executes Command.SINGLE_SUCCESS
				}
				val stack = client.player!!.inventory.main[8]
				if (!stack.isOf(Items.FILLED_MAP)) {
					context.source.sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map."))
					return@executes Command.SINGLE_SUCCESS
				}
				val map = FilledMapItem.getMapState(stack.get(DataComponentTypes.MAP_ID), client.world)
				if (map == null) {
					context.source.sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map state."))
					return@executes Command.SINGLE_SUCCESS
				}

				val roomName = StringArgumentType.getString(context, "room")
				val direction = DirectionArgumentType.getDirection(context, "direction")

				val room = newDebugRoom(roomName, direction, client.player, map)
				if (room == null) {
					context.source.sendError(Constants.PREFIX.get().append("§cFailed to find room with name $roomName."))
					return@executes Command.SINGLE_SUCCESS
				}
				if (currentRoom != null) {
					currentRoom!!.addSubProcess(room)
					context.source.sendFeedback(Constants.PREFIX.get().append("§rMatching room $roomName with direction $direction against current room."))
				} else {
					context.source.sendError(Constants.PREFIX.get().append("§cCurrent room is null."))
				}
				Command.SINGLE_SUCCESS
			})
	}

	private fun newDebugRoom(roomName: String, direction: Room.Direction?, player: PlayerEntity?, map: MapState): Room? {
		var room: Room? = null
		var roomData: IntArray?
		if ((ROOMS_DATA["catacombs"]!![Room.Shape.PUZZLE.shape]!![roomName].also { roomData = it }) != null) {
			room = DebugRoom.Companion.ofSinglePossibleRoom(Room.Type.PUZZLE, DungeonMapUtils.getPhysicalRoomPos(player!!.pos), roomName, roomData, direction)
		} else if ((ROOMS_DATA["catacombs"]!![Room.Shape.TRAP.shape]!![roomName].also { roomData = it }) != null) {
			room = DebugRoom.Companion.ofSinglePossibleRoom(Room.Type.TRAP, DungeonMapUtils.getPhysicalRoomPos(player!!.pos), roomName, roomData, direction)
		} else if ((ROOMS_DATA["catacombs"]!!.values.stream().map<Set<Map.Entry<String, IntArray>>> { obj: Map<String, IntArray> -> obj.entries }.flatMap<Map.Entry<String, IntArray>> { obj: Set<Map.Entry<String, IntArray>> -> obj.stream() }.filter { entry: Map.Entry<String, IntArray> -> entry.key == roomName }.findAny().map<IntArray?>(
				Function<Map.Entry<String, IntArray>, IntArray?> { java.util.Map.Entry.value }).orElse(null).also { roomData = it }) != null
		) {
			room = DebugRoom.Companion.ofSinglePossibleRoom(Room.Type.ROOM, DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, *DungeonMapUtils.getRoomSegments(map, DungeonMapUtils.getMapRoomPos(map, mapEntrancePos, mapRoomSize), mapRoomSize, Room.Type.ROOM.color)), roomName, roomData, direction)
		}
		return room
	}

	/**
	 * Updates the dungeon. The general idea is similar to the Dungeon Rooms Mod.
	 *
	 *
	 * When entering a new dungeon, this method:
	 *
	 *  *  Gets the physical northwest corner position of the entrance room and saves it in [.physicalEntrancePos].
	 *  *  Do nothing until the dungeon map exists.
	 *  *  Gets the upper left corner of entrance room on the map and saves it in [.mapEntrancePos].
	 *  *  Gets the size of a room on the map in pixels and saves it in [.mapRoomSize].
	 *  *  Creates a new [Room] with [Room.Type] [ENTRANCE][Room.Type.ENTRANCE] and sets [.currentRoom].
	 *
	 * When processing an existing dungeon, this method:
	 *
	 *  *  Calculates the physical northwest corner and upper left corner on the map of the room the player is currently in.
	 *  *  Gets the room type based on the map color.
	 *  *  If the room has not been created (when the physical northwest corner is not in [.rooms]):
	 *
	 *  *  If the room type is [Room.Type.ROOM], gets the northwest corner of all connected room segments with [DungeonMapUtils.getRoomSegments].  (For example, a 1x2 room has two room segments.)
	 *  *  Create a new room.
	 *
	 *  *  Sets [.currentRoom] to the current room, either created from the previous step or from [.rooms].
	 *  *  Calls [Tickable.tick] on [.currentRoom].
	 *
	 */
	private fun update() {
		if (!isInDungeons || isInBoss) {
			return
		}
		val client = MinecraftClient.getInstance()
		if (client.player == null || client.world == null) {
			return
		}
		if (physicalEntrancePos == null) {
			val playerPos = client.player!!.pos
			physicalEntrancePos = DungeonMapUtils.getPhysicalRoomPos(playerPos)
			currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos!!)
		}
		val map = FilledMapItem.getMapState(getMapIdComponent(client.player!!.inventory.main[8]), client.world) ?: return
		if (mapEntrancePos == null || mapRoomSize == 0) {
			val mapEntrancePosAndSize = DungeonMapUtils.getMapEntrancePosAndRoomSize(map) ?: return
			mapEntrancePos = mapEntrancePosAndSize.left()
			mapRoomSize = mapEntrancePosAndSize.rightInt()
			LOGGER.info("[Skyblocker Dungeon Secrets] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, client.player!!.pos, physicalEntrancePos)
		}

		val physicalPos = DungeonMapUtils.getPhysicalRoomPos(client.player!!.pos)
		val mapPos = DungeonMapUtils.getMapPosFromPhysical(physicalEntrancePos, mapEntrancePos, mapRoomSize, physicalPos)
		var room = rooms[physicalPos]
		if (room == null) {
			val type = DungeonMapUtils.getRoomType(map, mapPos)
			if (type == null || type == Room.Type.UNKNOWN) {
				return
			}
			when (type) {
				Room.Type.ENTRANCE, Room.Type.PUZZLE, Room.Type.TRAP, Room.Type.MINIBOSS, Room.Type.FAIRY, Room.Type.BLOOD -> room = newRoom(type, physicalPos)
				Room.Type.ROOM -> room = newRoom(type, *DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, *DungeonMapUtils.getRoomSegments(map, mapPos, mapRoomSize, type.color)))
			}
		}
		if (room != null && currentRoom !== room) {
			if (currentRoom != null && room.type == Room.Type.FAIRY) {
				currentRoom!!.nextRoom = room
				if (currentRoom!!.keyFound) {
					room.keyFound = true
				}
			}
			currentRoom = room
		}
		currentRoom!!.tick(client)
	}

	/**
	 * Creates a new room with the given type and physical positions,
	 * adds the room to [.rooms], and sets [.currentRoom] to the new room.
	 *
	 * @param type              the type of room to create
	 * @param physicalPositions the physical positions of the room
	 */
	private fun newRoom(type: Room.Type, vararg physicalPositions: Vector2ic): Room? {
		try {
			val newRoom = Room(type, *physicalPositions)
			for (physicalPos in physicalPositions) {
				rooms[physicalPos] = newRoom
			}
			return newRoom
		} catch (e: IllegalArgumentException) {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to create room", e)
		}
		return null
	}

	/**
	 * Renders the secret waypoints in [.currentRoom] if [.shouldProcess] and [.currentRoom] is not null.
	 */
	private fun render(context: WorldRenderContext) {
		if (shouldProcess() && currentRoom != null) {
			currentRoom!!.render(context)
		}
	}

	/**
	 * Calls [Room.onChatMessage] on [.currentRoom] if the message is an overlay message and [.isCurrentRoomMatched] and processes key obtained messages.
	 *
	 * Used to detect when all secrets in a room are found and detect when a wither or blood door is unlocked.
	 * To process key obtained messages, this method checks if door highlight is enabled and if the message matches a key obtained message.
	 * Then, it calls [Room.keyFound] on [.currentRoom] if the client's player is the one who obtained the key.
	 * Otherwise, it calls [Room.keyFound] on the room the player who obtained the key is in.
	 */
	private fun onChatMessage(text: Text, overlay: Boolean) {
		if (!shouldProcess()) {
			return
		}

		val message = text.string

		if (isCurrentRoomMatched) {
			currentRoom!!.onChatMessage(message)
		}

		// Process key found messages for door highlight
		if (SkyblockerConfigManager.get().dungeons.doorHighlight.enableDoorHighlight) {
			val matcher = KEY_FOUND.matcher(message)
			if (matcher.matches()) {
				val name = matcher.group("name")
				val client = MinecraftClient.getInstance()
				if (client.player != null && client.player!!.gameProfile.name == name) {
					if (currentRoom != null) {
						currentRoom!!.keyFound()
					} else {
						LOGGER.warn("[Skyblocker Dungeon Door] The current room at the current player {} does not exist", name)
					}
				} else if (client.world != null) {
					val posOptional = client.world!!.players.stream().filter { player: AbstractClientPlayerEntity -> player.gameProfile.name == name }.findAny().map { obj: AbstractClientPlayerEntity -> obj.pos }
					if (posOptional.isPresent) {
						val room = getRoomAtPhysical(posOptional.get())
						if (room != null) {
							room.keyFound()
						} else {
							LOGGER.warn("[Skyblocker Dungeon Door] Failed to find room at player {} with position {}", name, posOptional.get())
						}
					} else {
						LOGGER.warn("[Skyblocker Dungeon Door] Failed to find player {}", name)
					}
				}
			}
		}

		val newBoss = fromMessage(message)
		if (!isInBoss && newBoss.isInBoss) {
			reset()
			boss = newBoss
		}
	}

	/**
	 * Calls [Room.onUseBlock] on [.currentRoom] if [.isCurrentRoomMatched].
	 * Used to detect finding [SecretWaypoint.Category.CHEST] and [SecretWaypoint.Category.WITHER] secrets.
	 *
	 * @return [ActionResult.PASS]
	 */
	private fun onUseBlock(world: World, hitResult: BlockHitResult): ActionResult {
		if (isCurrentRoomMatched) {
			currentRoom!!.onUseBlock(world, hitResult.blockPos)
		}
		return ActionResult.PASS
	}

	/**
	 * Calls [Room.onItemPickup] on the room the `collector` is in if that room [.isRoomMatched].
	 * Used to detect finding [SecretWaypoint.Category.ITEM] secrets.
	 * If the collector is the player, [.currentRoom] is used as an optimization.
	 */
    @JvmStatic
    fun onItemPickup(itemEntity: ItemEntity) {
		val room = getRoomAtPhysical(itemEntity.pos)
		if (isRoomMatched(room)) {
			room!!.onItemPickup(itemEntity)
		}
	}

	/**
	 * Calls [Room.onBatRemoved] on the room the `bat` is in if that room [.isRoomMatched].
	 * Used to detect finding [SecretWaypoint.Category.BAT] secrets.
	 */
    @JvmStatic
    fun onBatRemoved(bat: AmbientEntity) {
		val room = getRoomAtPhysical(bat.pos)
		if (isRoomMatched(room)) {
			room!!.onBatRemoved(bat)
		}
	}

	fun markSecrets(secretIndex: Int, found: Boolean): Boolean {
		if (isCurrentRoomMatched) {
			return currentRoom!!.markSecrets(secretIndex, found)
		}
		return false
	}

	/**
	 * Gets the room at the given physical position.
	 *
	 * @param pos the physical position
	 * @return the room at the given physical position, or null if there is no room at the given physical position
	 * @see .rooms
	 *
	 * @see DungeonMapUtils.getPhysicalRoomPos
	 */
	private fun getRoomAtPhysical(pos: Vec3d): Room? {
		return rooms[DungeonMapUtils.getPhysicalRoomPos(pos)]
	}

	/**
	 * Gets the room at the given physical position.
	 *
	 * @param pos the physical position
	 * @return the room at the given physical position, or null if there is no room at the given physical position
	 * @see .rooms
	 *
	 * @see DungeonMapUtils.getPhysicalRoomPos
	 */
	private fun getRoomAtPhysical(pos: Vec3i): Room? {
		return rooms[DungeonMapUtils.getPhysicalRoomPos(pos)]
	}

	@JvmStatic
    val isCurrentRoomMatched: Boolean
		/**
		 * Calls [.isRoomMatched] on [.currentRoom].
		 *
		 * @return `true` if [.currentRoom] is not null and [.isRoomMatched]
		 */
		get() = isRoomMatched(currentRoom)

	/**
	 * Calls [.shouldProcess] and [Room.isMatched] on the given room.
	 *
	 * @param room the room to check
	 * @return `true` if [.shouldProcess], the given room is not null, and [Room.isMatched] on the given room
	 */
	@Contract("null -> false")
	private fun isRoomMatched(room: Room?): Boolean {
		return shouldProcess() && room != null && room.isMatched
	}

	/**
	 * Checks if [room matching][DungeonsConfig.SecretWaypoints.enableRoomMatching] is enabled and the player is in a dungeon.
	 *
	 * @return whether room matching and dungeon secrets should be processed
	 */
	private fun shouldProcess(): Boolean {
		return SkyblockerConfigManager.get().dungeons.secretWaypoints.enableRoomMatching && isInDungeons
	}

	/**
	 * Resets fields when leaving a dungeon or entering boss.
	 */
	private fun reset() {
		mapEntrancePos = null
		mapRoomSize = 0
		physicalEntrancePos = null
		rooms.clear()
		currentRoom = null
		boss = DungeonBoss.NONE
	}
}
