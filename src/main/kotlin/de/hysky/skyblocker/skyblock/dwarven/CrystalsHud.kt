package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.utils.Utils.isInCrystalHollows
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.joml.Vector2i
import org.joml.Vector2ic

object CrystalsHud {
	val MAP_TEXTURE = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/crystals_map.png")
	private val MAP_ICON = Identifier("textures/map/decorations/player.png")
	private val SMALL_LOCATIONS = listOf("Fairy Grotto", "King Yolkar", "Corleone", "Odawa", "Key Guardian")
	private val CLIENT = MinecraftClient.getInstance()
	var visible: Boolean = false

	init {
		ClientCommandRegistrationCallback.EVENT.register{ dispatcher, _ ->
			dispatcher.register(
				ClientCommandManager.literal("skyblocker")
					.then(
						ClientCommandManager.literal("hud")
							.then(ClientCommandManager.literal("crystals")
								.executes(Scheduler.queueOpenScreenCommand { CrystalsHudConfigScreen() })
							)
					)
			)
		}

		HudRenderEvents.AFTER_MAIN_HUD.register{ context, tickDelta ->
			if (!SkyblockerConfigManager.config.mining.crystalsHud.enabled || CLIENT.player == null || !visible) {
				return@register
			}
			render(context, tickDelta, SkyblockerConfigManager.config.mining.crystalsHud.x, SkyblockerConfigManager.config.mining.crystalsHud.y)
		}
	}

	val dimensionsForConfig: Int
		get() = (62 * SkyblockerConfigManager.config.mining.crystalsHud.mapScaling).toInt()

	/**
	 * Renders the map to the players UI. renders the background image ([CrystalsHud.MAP_TEXTURE]) of the map then if enabled special locations on the map. then finally the player to the map.
	 *
	 * @param context DrawContext to draw map to
	 * @param tickDelta For interpolating the player's yaw for map marker
	 * @param hudX Top left X coordinate of the map
	 * @param hudY Top left Y coordinate of the map
	 */
	private fun render(context: DrawContext, tickDelta: Float, hudX: Int, hudY: Int) {
		val scale = SkyblockerConfigManager.config.mining.crystalsHud.mapScaling

		//make sure the map renders infront of some stuff - improve this in the future with better layering (1.20.5?)
		//and set position and scale
		val matrices = context.matrices
		matrices.push()
		matrices.translate(hudX.toFloat(), hudY.toFloat(), 0f)
		matrices.scale(scale, scale, 0f)

		//draw map texture
		context.drawTexture(MAP_TEXTURE, 0, 0, 0f, 0f, 62, 62, 62, 62)

		//if enabled add waypoint locations to map
		if (SkyblockerConfigManager.config.mining.crystalsHud.showLocations) {
			val ActiveWaypoints = CrystalsLocationsManager.activeWaypoints

			for (waypoint in ActiveWaypoints.values) {
				val waypointColor = waypoint.category!!.color
				val renderPos = transformLocation(waypoint.pos.x.toDouble(), waypoint.pos.z.toDouble())
				var locationSize = SkyblockerConfigManager.config.mining.crystalsHud.locationSize

				if (SMALL_LOCATIONS.contains(waypoint.name.string)) { //if small location half the location size
					locationSize /= 2
				}

				//fill square of size locationSize around the coordinates of the location
				context.fill(renderPos.x() - locationSize / 2, renderPos.y() - locationSize / 2, renderPos.x() + locationSize / 2, renderPos.y() + locationSize / 2, waypointColor!!.rgb)
			}
		}

		//draw player on map
		if (CLIENT.player == null || CLIENT.networkHandler == null) return

		//get player location
		val playerX = CLIENT.player!!.x
		val playerZ = CLIENT.player!!.z
		val playerRotation = CLIENT.player!!.yaw //TODO make the transitions more rough?
		val renderPos = transformLocation(playerX, playerZ)

		val renderX = renderPos.x() - 2
		val renderY = renderPos.y() - 3

		//position, scale and rotate the player marker
		matrices.translate(renderX.toFloat(), renderY.toFloat(), 0f)
		matrices.scale(0.75f, 0.75f, 0f)
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw2Cardinal(playerRotation)), 2.5f, 3.5f, 0f)

		//draw marker on map
		context.drawTexture(MAP_ICON, 0, 0, 2f, 0f, 5, 7, 8, 8)

		//todo add direction (can not work out how to rotate)
		matrices.pop()
	}

	/**
	 * Converts an X and Z coordinate in the crystal hollow to an X and Y coordinate on the map.
	 *
	 * @param x the world X coordinate
	 * @param z the world Z coordinate
	 * @return a vector representing the x and y values
	 */
	fun transformLocation(x: Double, z: Double): Vector2ic {
		//converts an x and z to a location on the map
		var transformedX = ((x - 202) / 621 * 62).toInt()
		var transformedY = ((z - 202) / 621 * 62).toInt()
		transformedX = Math.clamp(transformedX.toLong(), 0, 62)
		transformedY = Math.clamp(transformedY.toLong(), 0, 62)

		return Vector2i(transformedX, transformedY)
	}

	/**
	 * Converts yaw to the cardinal directions that a player marker can be rotated towards on a map.
	 * The rotations of a marker follow this order: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW.
	 * <br></br><br></br>
	 * Based off code from [net.minecraft.client.render.MapRenderer]
	 */
	private fun yaw2Cardinal(yaw: Float): Float {
							//flip direction
		val clipped = (yaw + 180f).let { flippedYaw -> ((flippedYaw + (if (flippedYaw < 0.0) -8.0 else 8.0)) * 16.0 / 360.0).toInt().toByte() }
		return (clipped * 360f) / 16f
	}

	/**
	 * Works out if the crystals map should be rendered and sets [CrystalsHud.visible] accordingly.
	 *
	 */
	fun update() {
		if (CLIENT.player == null || CLIENT.networkHandler == null || !SkyblockerConfigManager.config.mining.crystalsHud.enabled) {
			visible = false
			return
		}

		//get if the player is in the crystals
		visible = isInCrystalHollows
	}
}
