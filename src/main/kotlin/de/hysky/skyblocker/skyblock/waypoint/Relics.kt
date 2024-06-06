package de.hysky.skyblocker.skyblock.waypoint

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.PosUtils.getPosString
import de.hysky.skyblocker.utils.PosUtils.parsePosString
import de.hysky.skyblocker.utils.Utils.locationRaw
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier

object Relics {
	private val LOGGER: Logger = LoggerFactory.getLogger(Relics::class.java)
	private val TYPE_SUPPLIER = Supplier { SkyblockerConfigManager.config.uiAndVisuals.waypoints.waypointType }
	private var relicsLoaded: CompletableFuture<Void>? = null

	@Suppress("unused")
	private var totalRelics = 0
	private val relics: MutableMap<BlockPos, ProfileAwareWaypoint> = HashMap()

	fun init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { obj: MinecraftClient? -> loadRelics() })
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { obj: MinecraftClient? -> saveFoundRelics() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onChatMessage(text) })
	}

	private fun loadRelics(client: MinecraftClient) {
		relicsLoaded = CompletableFuture.runAsync {
			try {
				client.resourceManager.openAsReader(Identifier(SkyblockerMod.NAMESPACE, "spidersden/relics.json")).use { reader ->
					for ((key, value) in JsonParser.parseReader(reader).asJsonObject.asMap()) {
						if (key == "total") {
							totalRelics = value.asInt
						} else if (key == "locations") {
							for (locationJson in value.asJsonArray.asList()) {
								val posData = locationJson.asJsonObject
								val pos = BlockPos(posData["x"].asInt, posData["y"].asInt, posData["z"].asInt)
								relics[pos] = ProfileAwareWaypoint(pos, TYPE_SUPPLIER, DyeColor.YELLOW.colorComponents, DyeColor.BROWN.colorComponents)
							}
						}
					}
					LOGGER.info("[Skyblocker] Loaded relics locations")
				}
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to load relics locations", e)
			}
			try {
				Files.newBufferedReader(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json")).use { reader ->
					for ((key, value) in JsonParser.parseReader(reader).asJsonObject.asMap()) {
						for (foundRelicsJson in value.asJsonArray.asList()) {
							relics[parsePosString(foundRelicsJson.asString)]!!.setFound(key!!)
						}
					}
					LOGGER.debug("[Skyblocker] Loaded found relics")
				}
			} catch (ignored: NoSuchFileException) {
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to load found relics", e)
			}
		}
	}

	private fun saveFoundRelics(client: MinecraftClient) {
		val foundRelics: MutableMap<String, MutableSet<BlockPos?>> = HashMap()
		for (relic in relics.values) {
			for (profile in relic.foundProfiles) {
				foundRelics.computeIfAbsent(profile) { profile_: String? -> HashSet() }
				foundRelics[profile]!!.add(relic.pos)
			}
		}

		try {
			Files.newBufferedWriter(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json")).use { writer ->
				val json = JsonObject()
				for ((key, value) in foundRelics) {
					val foundRelicsJson = JsonArray()
					for (foundRelic in value) {
						foundRelicsJson.add(getPosString(foundRelic!!))
					}
					json.add(key, foundRelicsJson)
				}
				SkyblockerMod.GSON.toJson(json, writer)
				LOGGER.debug("[Skyblocker] Saved found relics")
			}
		} catch (e: IOException) {
			LOGGER.error("[Skyblocker] Failed to write found relics to file", e)
		}
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(
			ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
				.then(ClientCommandManager.literal("relics")
					.then(ClientCommandManager.literal("markAllFound").executes { context: CommandContext<FabricClientCommandSource> ->
						relics.values.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setFound() })
						context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.relics.markAllFound")))
						1
					})
					.then(ClientCommandManager.literal("markAllMissing").executes { context: CommandContext<FabricClientCommandSource> ->
						relics.values.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setMissing() })
						context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.relics.markAllMissing")))
						1
					})
				)
		)
	}

	private fun render(context: WorldRenderContext) {
		val config = SkyblockerConfigManager.config.otherLocations.spidersDen.relics

		if (config.enableRelicsHelper && relicsLoaded!!.isDone && locationRaw == "combat_1") {
			for (relic in relics.values) {
				val isRelicMissing = relic.shouldRender()
				if (!isRelicMissing && !config.highlightFoundRelics) continue
				relic.render(context)
			}
		}
	}

	private fun onChatMessage(text: Text, overlay: Boolean) {
		val message = text.string
		if (message == "You've already found this relic!" || message.startsWith("+10,000 Coins! (") && message.endsWith("/28 Relics)")) {
			markClosestRelicFound()
		}
	}

	private fun markClosestRelicFound() {
		if (!relicsLoaded!!.isDone) return
		val player: PlayerEntity? = MinecraftClient.getInstance().player
		if (player == null) {
			LOGGER.warn("[Skyblocker] Failed to mark closest relic as found because player is null")
			return
		}
		relics.values.stream()
			.filter { obj: ProfileAwareWaypoint -> obj.shouldRender() }
			.min(Comparator.comparingDouble { relic: ProfileAwareWaypoint -> relic.pos!!.getSquaredDistance(player.pos) })
			.filter { relic: ProfileAwareWaypoint -> relic.pos!!.getSquaredDistance(player.pos) <= 16 }
			.ifPresent { obj: ProfileAwareWaypoint -> obj.setFound() }
	}
}
