package de.hysky.skyblocker.skyblock.crimson.kuudra

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra.KuudraPhase
import de.hysky.skyblocker.utils.PosUtils
import de.hysky.skyblocker.utils.Utils.isInKuudra
import de.hysky.skyblocker.utils.waypoint.Waypoint
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.GiantEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import org.slf4j.Logger
import java.io.BufferedReader
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.sin

object KuudraWaypoints {
	private val LOGGER: Logger = LogUtils.getLogger()
	private val SUPPLIES_COLOR = floatArrayOf(255f / 255f, 0f, 0f)
	private val PEARL_COLOR = floatArrayOf(57f / 255f, 117f / 255f, 125f / 255f)
	private val SAFE_SPOT_COLOR = floatArrayOf(255f / 255f, 85f / 255f, 255f / 255f)
	private val SUPPLIES_AND_FUEL_TYPE = Supplier { SkyblockerConfigManager.get().crimsonIsle.kuudra.suppliesAndFuelWaypointType }
	private val SAFE_SPOT_WAYPOINTS = ObjectArrayList<Waypoint>()
	private val PEARL_WAYPOINTS = ObjectArrayList<Waypoint>()
	private val CODEC = Function { cc: FloatArray? ->
		PosUtils.ALT_BLOCK_POS_CODEC.xmap(
			{ pos: BlockPos? -> Waypoint(pos, { Waypoint.Type.HIGHLIGHT }, cc, false) },
			{ waypoint: Waypoint -> waypoint.pos })
			.listOf()
	}

	//Use non final lists and swap them out to avoid ConcurrentModificationExceptions
	private var supplyWaypoints: ObjectArrayList<Waypoint> = ObjectArrayList.of()
	private var ballistaBuildWaypoints: ObjectArrayList<Waypoint> = ObjectArrayList.of()
	private var fuelWaypoints: ObjectArrayList<Waypoint> = ObjectArrayList.of()
	private var loaded = false

	fun load(client: MinecraftClient) {
		val safeSpots = loadWaypoints(client, Identifier(SkyblockerMod.NAMESPACE, "crimson/kuudra/safe_spot_waypoints.json"), SAFE_SPOT_WAYPOINTS, SAFE_SPOT_COLOR)
		val pearls = loadWaypoints(client, Identifier(SkyblockerMod.NAMESPACE, "crimson/kuudra/pearl_waypoints.json"), PEARL_WAYPOINTS, PEARL_COLOR)

		CompletableFuture.allOf(safeSpots, pearls).whenComplete { _result: Void?, _throwable: Throwable? -> loaded = true }
	}

	private fun loadWaypoints(client: MinecraftClient, file: Identifier, list: ObjectArrayList<Waypoint>, colorComponents: FloatArray): CompletableFuture<Void> {
		return CompletableFuture.supplyAsync {
			try {
				client.resourceManager.openAsReader(file).use { reader ->
					return@supplyAsync CODEC.apply(colorComponents).parse<JsonElement>(JsonOps.INSTANCE, getWaypoints(reader)).getOrThrow()
				}
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker Kuudra Waypoints] Failed to load kuudra waypoints from: {}", file, e)

				return@supplyAsync listOf<Waypoint>()
			}
		}.thenAccept { c: List<Waypoint>? -> list.addAll(c!!) }
	}

	private fun getWaypoints(reader: BufferedReader): JsonElement {
		return JsonParser.parseReader(reader).asJsonObject.getAsJsonArray("waypoints")
	}

	fun tick() {
		val client = MinecraftClient.getInstance()
		val config = SkyblockerConfigManager.get().crimsonIsle.kuudra

		if (isInKuudra && (config.supplyWaypoints || config.fuelWaypoints || config.ballistaBuildWaypoints) && client.player != null) {
			val searchBox = client.player!!.boundingBox.expand(500.0)
			val supplies = ObjectArrayList<Waypoint>()
			val fuelCells = ObjectArrayList<Waypoint>()

			if ((config.supplyWaypoints || config.fuelWaypoints) && client.world != null) {
				val giants = client.world!!.getEntitiesByClass(GiantEntity::class.java, searchBox) { giant: GiantEntity -> giant.y < 67 }

				for (giant in giants) {
					val yawOffset = (giant.yaw + 115).toDouble()

					val x = giant.x + 4.5 * cos((yawOffset) * MathHelper.RADIANS_PER_DEGREE)
					val y = 75.0
					val z = giant.z + 4.5 * sin((yawOffset) * MathHelper.RADIANS_PER_DEGREE)

					val waypoint = Waypoint(BlockPos.ofFloored(x, y, z), SUPPLIES_AND_FUEL_TYPE, SUPPLIES_COLOR, false)

					if (Objects.requireNonNull<KuudraPhase?>(Kuudra.phase) == KuudraPhase.DPS) {
						fuelCells.add(waypoint)
					} else {
						supplies.add(waypoint)
					}
				}
			}

			val ballistaBuildSpots = ObjectArrayList<Waypoint>()

			if (config.ballistaBuildWaypoints && client.world != null) {
				val armorStands = client.world!!.getEntitiesByClass(ArmorStandEntity::class.java, searchBox) { obj: ArmorStandEntity -> obj.hasCustomName() }

				for (armorStand in armorStands) {
					val name = armorStand.name.string

					if (config.ballistaBuildWaypoints && name.contains("SNEAK + PUNCH")) {
						ballistaBuildSpots.add(Waypoint(armorStand.blockPos, { Waypoint.Type.WAYPOINT }, SUPPLIES_COLOR, false))
					}
				}
			}

			supplyWaypoints = supplies
			ballistaBuildWaypoints = ballistaBuildSpots
			fuelWaypoints = fuelCells
		}
	}

	fun render(context: WorldRenderContext?) {
		val config = SkyblockerConfigManager.get().crimsonIsle.kuudra

		if (isInKuudra && loaded) {
			if (config.supplyWaypoints) {
				for (waypoint in supplyWaypoints) {
					waypoint.render(context)
				}
			}

			if (config.ballistaBuildWaypoints) {
				for (waypoint in ballistaBuildWaypoints) {
					waypoint.render(context)
				}
			}

			if (config.fuelWaypoints) {
				for (waypoint in fuelWaypoints) {
					waypoint.render(context)
				}
			}

			if (config.safeSpotWaypoints) {
				for (waypoint in SAFE_SPOT_WAYPOINTS) {
					waypoint.render(context)
				}
			}

			//TODO maybe have "dynamic" waypoints that draw a line to the actual spot
			if (config.pearlWaypoints) {
				for (waypoint in PEARL_WAYPOINTS) {
					waypoint.render(context)
				}
			}
		}
	}
}
