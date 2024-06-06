package de.hysky.skyblocker.skyblock.rift

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.waypoint.Waypoint
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

object MirrorverseWaypoints {
	private val LOGGER: Logger = LoggerFactory.getLogger("skyblocker")
	private val WAYPOINT_TYPE = Supplier { Waypoint.Type.HIGHLIGHT }
	private val WAYPOINTS_JSON = Identifier(SkyblockerMod.NAMESPACE, "rift/mirrorverse_waypoints.json")
	private var LAVA_PATH_WAYPOINTS: Array<Waypoint?>
	private var UPSIDE_DOWN_WAYPOINTS: Array<Waypoint?>
	private var TURBULATOR_WAYPOINTS: Array<Waypoint?>
	private val COLOR_COMPONENTS: FloatArray = DyeColor.RED.colorComponents

	private var waypointsLoaded: CompletableFuture<Void>? = null

	/**
	 * Loads the waypoint locations into memory
	 */
	fun load(client: MinecraftClient) {
		waypointsLoaded = CompletableFuture.runAsync {
			try {
				client.resourceManager.openAsReader(WAYPOINTS_JSON).use { reader ->
					val sections = JsonParser.parseReader(reader).asJsonObject["sections"].asJsonArray
					/// Lava Path
					LAVA_PATH_WAYPOINTS = loadWaypoints(sections[0].asJsonObject["waypoints"].asJsonArray)

					/// Upside Down Parkour
					UPSIDE_DOWN_WAYPOINTS = loadWaypoints(sections[1].asJsonObject["waypoints"].asJsonArray)

					/// Turbulator Parkour
					TURBULATOR_WAYPOINTS = loadWaypoints(sections[2].asJsonObject["waypoints"].asJsonArray)
				}
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Mirrorverse Waypoints failed to load ;(", e)
			}
		}
	}

	private fun loadWaypoints(waypointsJson: JsonArray): Array<Waypoint?> {
		val waypoints = arrayOfNulls<Waypoint>(waypointsJson.size())
		for (i in 0 until waypointsJson.size()) {
			val point = waypointsJson[i].asJsonObject
			waypoints[i] = Waypoint(BlockPos(point["x"].asInt, point["y"].asInt, point["z"].asInt), WAYPOINT_TYPE, COLOR_COMPONENTS, false)
		}
		return waypoints
	}

	fun render(wrc: WorldRenderContext?) {
		//I would also check for the mirrorverse location but the scoreboard stuff is not performant at all...
		if (isInTheRift && SkyblockerConfigManager.config.otherLocations.rift.mirrorverseWaypoints && waypointsLoaded!!.isDone) {
			for (waypoint in LAVA_PATH_WAYPOINTS) {
				waypoint!!.render(wrc!!)
			}

			for (waypoint in UPSIDE_DOWN_WAYPOINTS) {
				waypoint!!.render(wrc!!)
			}

			for (waypoint in TURBULATOR_WAYPOINTS) {
				waypoint!!.render(wrc!!)
			}
		}
	}
}
