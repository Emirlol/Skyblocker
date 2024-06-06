package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Utils.isInCrystalHollows
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.render.RenderHelper.renderLineFromCursor
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.util.regex.Pattern
import kotlin.math.abs

object MetalDetector {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private val LIGHT_GRAY = floatArrayOf(192 / 255f, 192 / 255f, 192 / 255f)
	private val TREASURE_PATTERN: Pattern = Pattern.compile("(§3§lTREASURE: §b)(\\d+\\.?\\d?)m")
	private val KEEPER_PATTERN: Pattern = Pattern.compile("Keeper of (\\w+)")
	private val keeperOffsets: HashMap<String?, Vec3i?> = Util.make(HashMap()) { map: HashMap<String?, Vec3i?> ->
		map["Diamond"] = Vec3i(33, 0, 3)
		map["Lapis"] = Vec3i(-33, 0, -3)
		map["Emerald"] = Vec3i(-3, 0, 33)
		map["Gold"] = Vec3i(3, 0, -33)
	}
	private val knownChestOffsets: HashSet<Vec3i?> = Util.make(HashSet()) { set: HashSet<Vec3i?> ->
		set.add(Vec3i(-38, -22, 26)) // -38, -22, 26
		set.add(Vec3i(38, -22, -26)) // 38, -22, -26
		set.add(Vec3i(-40, -22, 18)) // -40, -22, 18
		set.add(Vec3i(-41, -20, 22)) // -41, -20, 22
		set.add(Vec3i(-5, -21, 16)) // -5, -21, 16
		set.add(Vec3i(40, -22, -30)) // 40, -22, -30
		set.add(Vec3i(-42, -20, -28)) // -42, -20, -28
		set.add(Vec3i(-43, -22, -40)) // -43, -22, -40
		set.add(Vec3i(42, -19, -41)) // 42, -19, -41
		set.add(Vec3i(43, -21, -16)) // 43, -21, -16
		set.add(Vec3i(-1, -22, -20)) // -1, -22, -20
		set.add(Vec3i(6, -21, 28)) // 6, -21, 28
		set.add(Vec3i(7, -21, 11)) // 7, -21, 11
		set.add(Vec3i(7, -21, 22)) // 7, -21, 22
		set.add(Vec3i(-12, -21, -44)) // -12, -21, -44
		set.add(Vec3i(12, -22, 31)) // 12, -22, 31
		set.add(Vec3i(12, -22, -22)) // 12, -22, -22
		set.add(Vec3i(12, -21, 7)) // 12, -21, 7
		set.add(Vec3i(12, -21, -43)) // 12, -21, -43
		set.add(Vec3i(-14, -21, 43)) // -14, -21, 43
		set.add(Vec3i(-14, -21, 22)) // -14, -21, 22
		set.add(Vec3i(-17, -21, 20)) // -17, -21, 20
		set.add(Vec3i(-20, -22, 0)) // -20, -22, 0
		set.add(Vec3i(1, -21, 20)) // 1, -21, 20
		set.add(Vec3i(19, -22, 29)) // 19, -22, 29
		set.add(Vec3i(20, -22, 0)) // 20, -22, 0
		set.add(Vec3i(20, -21, -26)) // 20, -21, -26
		set.add(Vec3i(-23, -22, 40)) // -23, -22, 40
		set.add(Vec3i(22, -21, -14)) // 22, -21, -14
		set.add(Vec3i(-24, -22, 12)) // -24, -22, 12
		set.add(Vec3i(23, -22, 26)) // 23, -22, 26
		set.add(Vec3i(23, -22, -39)) // 23, -22, -39
		set.add(Vec3i(24, -22, 27)) // 24, -22, 27
		set.add(Vec3i(25, -22, 17)) // 25, -22, 17
		set.add(Vec3i(29, -21, -44)) // 29, -21, -44
		set.add(Vec3i(-31, -21, -12)) // -31, -21, -12
		set.add(Vec3i(-31, -21, -40)) // -31, -21, -40
		set.add(Vec3i(30, -21, -25)) // 30, -21, -25
		set.add(Vec3i(-32, -21, -40)) // -32, -21, -40
		set.add(Vec3i(-36, -20, 42)) // -36, -20, 42
		set.add(Vec3i(-37, -21, -14)) // -37, -21, -14
		set.add(Vec3i(-37, -21, -22)) // -37, -21, -22
	}

	var minesCenter: Vec3i? = null
	private var previousDistance = 0.0
	private var previousPlayerPos: Vec3d? = null
	var newTreasure: Boolean = true
	private var startedLooking = false
	var possibleBlocks: MutableList<Vec3i?> = ArrayList()

	fun init() {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> getDistanceMessage(text) })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _handler: ClientPlayNetworkHandler?, _sender: PacketSender?, _client: MinecraftClient? -> reset() })
	}

	/**
	 * Processes the message with the distance to the treasure, updates the helper, and works out possible locations using that message.
	 *
	 * @param text    the message sent to the player
	 * @param overlay if the message is an overlay message
	 */
	private fun getDistanceMessage(text: Text, overlay: Boolean) {
		if (!overlay || !SkyblockerConfigManager.config.mining.crystalHollows.metalDetectorHelper || !isInCrystalHollows || islandArea.substring(2) != "Mines of Divan" || CLIENT.player == null) {
			checkChestFound(text)
			return
		}
		//in the mines of divan
		val treasureDistanceMature = TREASURE_PATTERN.matcher(text.string)
		if (!treasureDistanceMature.matches()) {
			return
		}
		//find new values
		val distance = treasureDistanceMature.group(2).toDouble()
		val playerPos = CLIENT.player!!.pos
		val previousPossibleBlockCount = possibleBlocks.size

		//send message when starting looking about how to use mod
		if (!startedLooking) {
			startedLooking = true
			CLIENT.player!!.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.startTip")), false)
		}

		//find the center of the mines if possible to speed up search
		if (minesCenter == null) {
			findCenterOfMines()
		}

		//find the possible locations the treasure could be
		if (distance == previousDistance && playerPos == previousPlayerPos) {
			updatePossibleBlocks(distance, playerPos)
		}

		//if the amount of possible blocks has changed output that to the user
		if (possibleBlocks.size != previousPossibleBlockCount) {
			if (possibleBlocks.size == 1) {
				CLIENT.player!!.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.foundTreasureMessage").formatted(Formatting.GREEN)), false)
			} else {
				CLIENT.player!!.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.possibleTreasureLocationsMessage").append(Text.of(possibleBlocks.size.toString()))), false)
			}
		}

		//update previous positions
		previousDistance = distance
		previousPlayerPos = playerPos
	}

	/**
	 * Processes the found treasure message and resets the helper
	 *
	 * @param text the message sent to the player
	 */
	private fun checkChestFound(text: Text) {
		if (!isInCrystalHollows || islandArea.substring(2) != "Mines of Divan" || CLIENT.player == null) {
			return
		}
		if (text.string.startsWith("You found")) {
			newTreasure = true
			possibleBlocks = ArrayList()
		}
	}

	/**
	 * Works out the possible locations the treasure could be using the distance the treasure is from the player and
	 * narrows down possible locations until there is one left.
	 *
	 * @param distance the distance the treasure is from the player squared
	 * @param playerPos the position of the player
	 */
	fun updatePossibleBlocks(distance: Double, playerPos: Vec3d) {
		if (newTreasure) {
			possibleBlocks = ArrayList()
			newTreasure = false
			if (minesCenter != null) { //if center of the mines is known use the predefined offsets to filter the locations
				for (knownOffset in knownChestOffsets) {
					val checkPos = minesCenter!!.add(knownOffset).add(0, 1, 0)
					if (abs(playerPos.distanceTo(Vec3d.of(checkPos)) - distance) < 0.25) {
						possibleBlocks.add(checkPos)
					}
				}
			} else {
				var x = -distance.toInt()
				while (x <= distance) {
					var z = -distance.toInt()
					while (z <= distance) {
						val checkPos = Vec3i(playerPos.x.toInt() + x, playerPos.y.toInt(), playerPos.z.toInt() + z)
						if (abs(playerPos.distanceTo(Vec3d.of(checkPos)) - distance) < 0.25) {
							possibleBlocks.add(checkPos)
						}
						z++
					}
					x++
				}
			}
		} else {
			possibleBlocks.removeIf { location: Vec3i? -> abs(playerPos.distanceTo(Vec3d.of(location)) - distance) >= 0.25 }
		}

		//if possible blocks is of length 0 something has failed reset and try again
		if (possibleBlocks.isEmpty()) {
			newTreasure = true
			if (CLIENT.player != null) {
				CLIENT.player!!.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.somethingWentWrongMessage").formatted(Formatting.RED)), false)
			}
		}
	}

	/**
	 * Uses the labels for the keepers names to find the central point of the mines of divan so the known offsets can be used.
	 */
	private fun findCenterOfMines() {
		if (CLIENT.player == null || CLIENT.world == null) {
			return
		}
		val searchBox = CLIENT.player!!.boundingBox.expand(500.0)
		val armorStands = CLIENT.world!!.getEntitiesByClass(ArmorStandEntity::class.java, searchBox) { obj: ArmorStandEntity -> obj.hasCustomName() }

		for (armorStand in armorStands) {
			val name = armorStand.name.string
			val nameMatcher = KEEPER_PATTERN.matcher(name)

			if (nameMatcher.matches()) {
				val offset = keeperOffsets[nameMatcher.group(1)]
				minesCenter = armorStand.blockPos.add(offset)
				CLIENT.player!!.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.foundCenter").formatted(Formatting.GREEN)), false)
				return
			}
		}
	}

	private fun reset() {
		minesCenter = null
		possibleBlocks = ArrayList()
	}

	/**
	 * Renders waypoints for the location of treasure or possible treasure.
	 * @param context world render context
	 */
	private fun render(context: WorldRenderContext) {
		//only render enabled and if there is a few location options and in the mines of divan
		if (!SkyblockerConfigManager.config.mining.crystalHollows.metalDetectorHelper || !isInCrystalHollows || possibleBlocks.isEmpty() || possibleBlocks.size > 8 || islandArea.substring(2) != "Mines of Divan") {
			return
		}
		//only one location render just that and guiding line to it
		if (possibleBlocks.size == 1) {
			val block = possibleBlocks.first!!.add(0, -1, 0) //the block you are taken to is one block above the chest
			val waypoint = CrystalsWaypoint(CrystalsWaypoint.Category.CORLEONE, Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.treasure"), BlockPos(block.x, block.y, block.z))
			waypoint.render(context)
			renderLineFromCursor(context, Vec3d.ofCenter(block), LIGHT_GRAY, 1f, 5f)
			return
		}

		for (block in possibleBlocks) {
			val waypoint = CrystalsWaypoint(CrystalsWaypoint.Category.CORLEONE, Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.possible"), BlockPos(block!!.x, block.y, block.z))
			waypoint.render(context)
		}
	}
}
