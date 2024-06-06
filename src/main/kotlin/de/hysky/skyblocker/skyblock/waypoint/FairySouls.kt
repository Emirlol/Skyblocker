package de.hysky.skyblocker.skyblock.waypoint

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.NEURepoManager
import de.hysky.skyblocker.utils.NEURepoManager.runAsyncAfterLoad
import de.hysky.skyblocker.utils.PosUtils.getPosString
import de.hysky.skyblocker.utils.PosUtils.parsePosString
import de.hysky.skyblocker.utils.Utils.locationRaw
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint
import io.github.moulberry.repo.data.Coordinate
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
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
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

object FairySouls {
	private val LOGGER: Logger = LoggerFactory.getLogger(FairySouls::class.java)
	private val TYPE_SUPPLIER = Supplier { SkyblockerConfigManager.config.uiAndVisuals.waypoints.waypointType }
	private var fairySoulsLoaded: CompletableFuture<Void>? = null
	private var maxSouls = 0
	private val fairySouls: MutableMap<String, Map<BlockPos, ProfileAwareWaypoint>> = HashMap()

	@JvmStatic
    fun runAsyncAfterFairySoulsLoad(runnable: Runnable?): CompletableFuture<Void?> {
		if (fairySoulsLoaded == null) {
			LOGGER.error("[Skyblocker] Fairy Souls have not being initialized yet! Please ensure the Fairy Souls configs is initialized before modules calling this method in SkyblockerMod#onInitializeClient. This error can be safely ignore in a test environment.")
			return CompletableFuture.completedFuture(null)
		}
		return fairySoulsLoaded!!.thenRunAsync(runnable)
	}

	@JvmStatic
    fun getFairySoulsSize(location: String?): Int {
		return if (location == null) maxSouls else fairySouls[location]!!.size
	}

	fun init() {
		loadFairySouls()
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { obj: MinecraftClient? -> saveFoundFairySouls() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onChatMessage(text) })
	}

	private fun loadFairySouls() {
		fairySoulsLoaded = runAsyncAfterLoad {
			maxSouls = NEURepoManager.NEU_REPO.constants.fairySouls.maxSouls
			NEURepoManager.NEU_REPO.constants.fairySouls.soulLocations.forEach { (location: String, fairiesForLocation: List<Coordinate>) -> fairySouls[location] = fairiesForLocation.stream().map { coordinate: Coordinate -> BlockPos(coordinate.x, coordinate.y, coordinate.z) }.collect(Collectors.toUnmodifiableMap(Function { pos: BlockPos -> pos }, Function { pos: BlockPos? -> ProfileAwareWaypoint(pos, TYPE_SUPPLIER, DyeColor.GREEN.colorComponents, DyeColor.RED.colorComponents) })) }
			LOGGER.debug("[Skyblocker] Loaded {} fairy souls across {} locations", fairySouls.values.stream().mapToInt { obj: Map<BlockPos, ProfileAwareWaypoint> -> obj.size }.sum(), fairySouls.size)

			try {
				Files.newBufferedReader(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json")).use { reader ->
					for ((key, value) in JsonParser.parseReader(reader).asJsonObject.asMap()) {
						for ((key1, value1) in value.asJsonObject.asMap()) {
							val fairiesForLocation = fairySouls[key1]!!
							for (foundFairy in value1.asJsonArray.asList()) {
								fairiesForLocation[parsePosString(foundFairy.asString)]!!.setFound(key!!)
							}
						}
					}
					LOGGER.debug("[Skyblocker] Loaded found fairy souls")
				}
			} catch (ignored: NoSuchFileException) {
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to load found fairy souls", e)
			}
			LOGGER.info("[Skyblocker] Loaded {} fairy souls across {} locations", fairySouls.values.stream().mapToInt { obj: Map<BlockPos, ProfileAwareWaypoint> -> obj.size }.sum(), fairySouls.size)
		}
	}

	private fun saveFoundFairySouls(client: MinecraftClient) {
		val foundFairies: MutableMap<String, MutableMap<String, MutableSet<BlockPos?>>> = HashMap()
		for ((key, value) in fairySouls) {
			for (fairySoul in value.values) {
				for (profile in fairySoul.foundProfiles) {
					foundFairies.computeIfAbsent(profile, Function<String, MutableMap<String, MutableSet<BlockPos?>>> { profile_: String? -> HashMap<String, Set<BlockPos?>>() })
					foundFairies[profile]!!.computeIfAbsent(key) { location_: String? -> HashSet() }
					foundFairies[profile]!![key]!!.add(fairySoul.pos)
				}
			}
		}

		try {
			Files.newBufferedWriter(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json")).use { writer ->
				val foundFairiesJson = JsonObject()
				for ((key, value) in foundFairies) {
					val foundFairiesForProfileJson = JsonObject()
					for ((key1, value1) in value) {
						val foundFairiesForLocationJson = JsonArray()
						for (foundFairy in value1) {
							foundFairiesForLocationJson.add(getPosString(foundFairy!!))
						}
						foundFairiesForProfileJson.add(key1, foundFairiesForLocationJson)
					}
					foundFairiesJson.add(key, foundFairiesForProfileJson)
				}
				SkyblockerMod.GSON.toJson(foundFairiesJson, writer)
				LOGGER.info("[Skyblocker] Saved found fairy souls")
			}
		} catch (e: IOException) {
			LOGGER.error("[Skyblocker] Failed to write found fairy souls to file", e)
		}
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(
			ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
				.then(ClientCommandManager.literal("fairySouls")
					.then(ClientCommandManager.literal("markAllInCurrentIslandFound").executes { context: CommandContext<FabricClientCommandSource> ->
						markAllFairiesOnCurrentIslandFound()
						context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.fairySouls.markAllFound")))
						1
					})
					.then(ClientCommandManager.literal("markAllInCurrentIslandMissing").executes { context: CommandContext<FabricClientCommandSource> ->
						markAllFairiesOnCurrentIslandMissing()
						context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.fairySouls.markAllMissing")))
						1
					})
				)
		)
	}

	private fun render(context: WorldRenderContext) {
		val fairySoulsConfig = SkyblockerConfigManager.config.helpers.fairySouls

		if (fairySoulsConfig.enableFairySoulsHelper && fairySoulsLoaded!!.isDone && fairySouls.containsKey(locationRaw)) {
			for (fairySoul in fairySouls[locationRaw]!!.values) {
				val fairySoulNotFound = fairySoul.shouldRender()
				if (!fairySoulsConfig.highlightFoundSouls && !fairySoulNotFound || fairySoulsConfig.highlightOnlyNearbySouls && fairySoul.pos!!.getSquaredDistance(context.camera().pos) > 2500) {
					continue
				}
				fairySoul.render(context)
			}
		}
	}

	private fun onChatMessage(text: Text, overlay: Boolean) {
		val message = text.string
		if (message == "You have already found that Fairy Soul!" || message == "§d§lSOUL! §fYou found a §dFairy Soul§f!") {
			markClosestFairyFound()
		}
	}

	private fun markClosestFairyFound() {
		if (!fairySoulsLoaded!!.isDone) return

		val player: PlayerEntity? = MinecraftClient.getInstance().player
		if (player == null) {
			LOGGER.warn("[Skyblocker] Failed to mark closest fairy soul as found because player is null")
			return
		}

		val fairiesOnCurrentIsland = fairySouls[locationRaw]
		if (fairiesOnCurrentIsland == null) {
			LOGGER.warn("[Skyblocker] Failed to mark closest fairy soul as found because there are no fairy souls loaded on the current island. NEU repo probably failed to load.")
			return
		}

		fairiesOnCurrentIsland.values.stream()
			.filter { obj: ProfileAwareWaypoint -> obj.shouldRender() }
			.min(Comparator.comparingDouble { fairySoul: ProfileAwareWaypoint -> fairySoul.pos!!.getSquaredDistance(player.pos) })
			.filter { fairySoul: ProfileAwareWaypoint -> fairySoul.pos!!.getSquaredDistance(player.pos) <= 16 }
			.ifPresent { obj: ProfileAwareWaypoint -> obj.setFound() }
	}

	fun markAllFairiesOnCurrentIslandFound() {
		val fairiesForLocation = fairySouls[locationRaw]
		fairiesForLocation?.values?.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setFound() })
	}

	fun markAllFairiesOnCurrentIslandMissing() {
		val fairiesForLocation = fairySouls[locationRaw]
		fairiesForLocation?.values?.forEach(Consumer { obj: ProfileAwareWaypoint -> obj.setMissing() })
	}
}
