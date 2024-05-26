package de.hysky.skyblocker.skyblock.rift

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.PosUtils.getPosString
import de.hysky.skyblocker.utils.PosUtils.parsePosString
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier

object EnigmaSouls {
	private val LOGGER: Logger = LoggerFactory.getLogger(EnigmaSouls::class.java)
	private val TYPE_SUPPLIER = Supplier { SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType }
	private val WAYPOINTS_JSON = Identifier(SkyblockerMod.NAMESPACE, "rift/enigma_soul_waypoints.json")
	private val SOUL_WAYPOINTS: MutableMap<BlockPos, ProfileAwareWaypoint> = HashMap(42)
	private val FOUND_SOULS_FILE: Path = SkyblockerMod.CONFIG_DIR.resolve("found_enigma_souls.json")
	private val GREEN: FloatArray = DyeColor.GREEN.colorComponents
	private val RED: FloatArray = DyeColor.RED.colorComponents

	private var soulsLoaded: CompletableFuture<Void>? = null

	fun load(client: MinecraftClient) {
		//Load waypoints
		soulsLoaded = CompletableFuture.runAsync {
			try {
				client.resourceManager.openAsReader(WAYPOINTS_JSON).use { reader ->
					val file = JsonParser.parseReader(reader).asJsonObject
					val waypoints = file["waypoints"].asJsonArray
					for (i in 0 until waypoints.size()) {
						val waypoint = waypoints[i].asJsonObject
						val pos = BlockPos(waypoint["x"].asInt, waypoint["y"].asInt, waypoint["z"].asInt)
						SOUL_WAYPOINTS[pos] = ProfileAwareWaypoint(pos, TYPE_SUPPLIER, GREEN, RED)
					}
				}
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] There was an error while loading enigma soul waypoints!", e)
			}
			//Load found souls
			try {
				Files.newBufferedReader(FOUND_SOULS_FILE).use { reader ->
					for ((key, value) in JsonParser.parseReader(reader).asJsonObject.asMap()) {
						for (foundSoul in value.asJsonArray.asList()) {
							SOUL_WAYPOINTS[parsePosString(foundSoul.asString)]!!.setFound(key!!)
						}
					}
				}
			} catch (ignored: NoSuchFileException) {
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] There was an error while loading found enigma souls!", e)
			}
		}
	}

	fun save(client: MinecraftClient?) {
		val foundSouls: MutableMap<String, MutableSet<BlockPos?>> = HashMap()
		for (soul in SOUL_WAYPOINTS.values) {
			for (profile in soul.foundProfiles) {
				foundSouls.computeIfAbsent(profile) { profile_: String? -> HashSet() }
				foundSouls[profile]!!.add(soul.pos)
			}
		}

		val json = JsonObject()
		for ((key, value) in foundSouls) {
			val foundSoulsJson = JsonArray()

			for (foundSoul in value) {
				foundSoulsJson.add(getPosString(foundSoul!!))
			}

			json.add(key, foundSoulsJson)
		}

		try {
			Files.newBufferedWriter(FOUND_SOULS_FILE).use { writer ->
				SkyblockerMod.GSON.toJson(json, writer)
			}
		} catch (e: IOException) {
			LOGGER.error("[Skyblocker] There was an error while saving found enigma souls!", e)
		}
	}

	fun render(context: WorldRenderContext?) {
		val config = SkyblockerConfigManager.get().otherLocations.rift

		if (isInTheRift && config.enigmaSoulWaypoints && soulsLoaded!!.isDone) {
			for (soul in SOUL_WAYPOINTS.values) {
				if (soul.shouldRender()) {
					soul.render(context)
				} else if (config.highlightFoundEnigmaSouls) {
					soul.render(context)
				}
			}
		}
	}

	fun onMessage(text: Text, overlay: Boolean) {
		if (isInTheRift && !overlay) {
			val message = text.string

			if (message == "You have already found that Enigma Soul!" || Formatting.strip(message) == "SOUL! You unlocked an Enigma Soul!") markClosestSoulAsFound()
		}
	}

	fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess?) {
		dispatcher.register(
			ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
				.then(
					ClientCommandManager.literal("rift")
						.then(ClientCommandManager.literal("enigmaSouls")
							.then(ClientCommandManager.literal("markAllFound").executes { context: CommandContext<FabricClientCommandSource> ->
								SOUL_WAYPOINTS.values.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setFound() })
								context.source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllFound")))
								Command.SINGLE_SUCCESS
							})
							.then(ClientCommandManager.literal("markAllMissing").executes { context: CommandContext<FabricClientCommandSource> ->
								SOUL_WAYPOINTS.values.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setMissing() })
								context.source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllMissing")))
								Command.SINGLE_SUCCESS
							})
						)
				)
		)
	}

	private fun markClosestSoulAsFound() {
		val player = MinecraftClient.getInstance().player

		if (!soulsLoaded!!.isDone || player == null) return

		SOUL_WAYPOINTS.values.stream()
			.filter { obj: ProfileAwareWaypoint -> obj.shouldRender() }
			.min(Comparator.comparingDouble { soul: ProfileAwareWaypoint -> soul.pos!!.getSquaredDistance(player.pos) })
			.filter { soul: ProfileAwareWaypoint -> soul.pos!!.getSquaredDistance(player.pos) <= 16 }
			.ifPresent { obj: ProfileAwareWaypoint -> obj.setFound() }
	}
}
