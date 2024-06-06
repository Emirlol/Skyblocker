package de.hysky.skyblocker.skyblock.dwarven

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils.isInCrystalHollows
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.PosArgument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.util.regex.Pattern

object CrystalsLocationsManager {
	private val CLIENT = MinecraftClient.getInstance()

	/**
	 * A look-up table to convert between location names and waypoint in the [CrystalsWaypoint.Category] values.
	 */
	private val WAYPOINT_LOCATIONS = CrystalsWaypoint.Category.entries.associateBy { it.toString() }
	private val TEXT_CWORDS_PATTERN = Pattern.compile("([0-9][0-9][0-9]) ([0-9][0-9][0-9]?) ([0-9][0-9][0-9])")

	var activeWaypoints: MutableMap<String, CrystalsWaypoint> = HashMap()

	fun init() {
		Scheduler.scheduleCyclic(40, task = ::update)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(::render)
		ClientReceiveMessageEvents.GAME.register(::extractLocationFromMessage)
		ClientCommandRegistrationCallback.EVENT.register(::registerWaypointLocationCommands)
		ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
	}

	private fun extractLocationFromMessage(message: Text, overlay: Boolean) {
		if (overlay || !SkyblockerConfigManager.config.mining.crystalsWaypoints.findInChat || !isInCrystalHollows) {
			return
		}

		try {
			//get the message text
			val value = message.string
			val matcher = TEXT_CWORDS_PATTERN.matcher(value)
			//if there are coordinates in the message try to get them and what they are talking about
			if (matcher.find()) {
				val location = matcher.group()
				val coordinates = location.split(' ', limit = 3).map(String::toInt)
				val blockPos = BlockPos(coordinates[0], coordinates[1], coordinates[2])

				//if position is not in the hollows do not add it
				if (!checkInCrystals(blockPos)) return

				//see if there is a name of a location to add to this
				for (waypointLocation in WAYPOINT_LOCATIONS.keys) {
					if (value.contains(waypointLocation, true)) { //todo be more lenient
						//all data found to create waypoint
						addCustomWaypoint(waypointLocation, blockPos)
						return
					}
				}

				//if the location is not found ask the user for the location (could have been in a previous chat message)
				CLIENT.player?.sendMessage(getLocationInputText(location), false)
			}
		} catch (e: Exception) {
			TextHandler.error("[Crystals Locations Manager] Encountered an exception while extracing a location from a chat message!", e)
		}
	}

	fun checkInCrystals(pos: BlockPos): Boolean {
		//checks if a location is inside crystal hollows bounds
		return pos.x >= 202 && pos.x <= 823 && pos.z >= 202 && pos.z <= 823 && pos.y >= 31 && pos.y <= 188
	}

	private fun registerWaypointLocationCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
			.then(ClientCommandManager.literal("crystalWaypoints")
				.then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
					.then(ClientCommandManager.argument("place", StringArgumentType.greedyString())
						.suggests { context, builder -> CommandSource.suggestMatching(WAYPOINT_LOCATIONS.keys, builder) }
						.executes { context -> addWaypointFromCommand(context.source, StringArgumentType.getString(context, "place"), context.getArgument("pos", PosArgument::class.java)) }
					)
				)
				.then(ClientCommandManager.literal("share")
					.then(ClientCommandManager.argument("place", StringArgumentType.greedyString())
						.suggests { context, builder -> CommandSource.suggestMatching(WAYPOINT_LOCATIONS.keys, builder) }
						.executes { context -> shareWaypoint(StringArgumentType.getString(context, "place")) }
					)
				)
			)
		)
	}

	fun getSetLocationMessage(location: String, blockPos: BlockPos): Text {
		val text = Constants.PREFIX
		text.append(Text.literal("Added waypoint for "))
		val locationColor = WAYPOINT_LOCATIONS[location]!!.color
		text.append(Text.literal(location).withColor(locationColor.rgb))
		text.append(Text.literal(" at : " + blockPos.toShortString()))

		return text
	}

	private fun getLocationInputText(location: String): Text {
		val text = Constants.PREFIX

		for (waypointLocation in WAYPOINT_LOCATIONS.keys) {
			val locationColor = WAYPOINT_LOCATIONS[waypointLocation]!!.color
			text.append(Text.literal("[$waypointLocation]").withColor(locationColor.rgb).styled { style: Style -> style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker crystalWaypoints $location $waypointLocation")) })
		}

		return text
	}

	fun addWaypointFromCommand(source: FabricClientCommandSource, place: String, location: PosArgument): Int {
		// TODO Less hacky way with custom ClientBlockPosArgumentType
		val blockPos = location.toAbsoluteBlockPos(ServerCommandSource(null, source.position, source.rotation, null, 0, null, null, null, null))

		if (WAYPOINT_LOCATIONS.containsKey(place)) {
			addCustomWaypoint(place, blockPos)

			//tell the client it has done this
			CLIENT.player?.sendMessage(getSetLocationMessage(place, blockPos), false)
		}

		return Command.SINGLE_SUCCESS
	}

	fun shareWaypoint(place: String): Int {
		if (activeWaypoints.containsKey(place)) {
			val pos = activeWaypoints[place]!!.pos
			MessageScheduler.sendMessageAfterCooldown(Constants.PREFIX.string + " " + place + ": " + pos.toShortString())
		} else {
			//send fail message
			CLIENT.player?.sendMessage(Constants.PREFIX.append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFail").formatted(Formatting.RED)), false)
		}

		return Command.SINGLE_SUCCESS
	}

	private fun addCustomWaypoint(waypointName: String, pos: BlockPos) {
		val category = WAYPOINT_LOCATIONS[waypointName]
		val waypoint = CrystalsWaypoint(category, Text.literal(waypointName), pos)
		activeWaypoints[waypointName] = waypoint
	}

	fun render(context: WorldRenderContext) {
		if (SkyblockerConfigManager.config.mining.crystalsWaypoints.enabled) {
			for (crystalsWaypoint in activeWaypoints.values) {
				if (crystalsWaypoint.shouldRender()) {
					crystalsWaypoint.render(context)
				}
			}
		}
	}

	private fun reset() {
		activeWaypoints.clear()
	}

	fun update() {
		if (CLIENT.player == null || CLIENT.networkHandler == null || !SkyblockerConfigManager.config.mining.crystalsWaypoints.enabled || !isInCrystalHollows) {
			return
		}

		//get if the player is in the crystals
		val location = islandArea.substring(2)
		//if new location and needs waypoint add waypoint
		if (location != "Unknown" && WAYPOINT_LOCATIONS.containsKey(location) && !activeWaypoints.containsKey(location)) {
			//add waypoint at player location
			val playerLocation = CLIENT.player!!.blockPos
			addCustomWaypoint(location, playerLocation)
		}
	}
}
