package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import de.hysky.skyblocker.utils.waypoint.Waypoint
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction

class CrystalsWaypoint internal constructor(val category: Category?, val name: Text, pos: BlockPos) : Waypoint(pos, TYPE_SUPPLIER, category!!.colorComponents) {
	private val centerPos: Vec3d = pos.toCenterPos()

	override fun shouldRender(): Boolean {
		return super.shouldRender()
	}

	override fun equals(obj: Any?): Boolean {
		return super.equals(obj) || obj is CrystalsWaypoint && category == obj.category && name == obj.name && pos == obj.pos
	}

	/**
	 * Renders the secret waypoint, including a waypoint through [Waypoint.render], the name, and the distance from the player.
	 */
	override fun render(context: WorldRenderContext) {
		super.render(context)

		val posUp = centerPos.add(0.0, 1.0, 0.0)
		renderText(context, name, posUp, true)
		val distance = context.camera().pos.distanceTo(centerPos)
		renderText(context, Text.literal(Math.round(distance).toString() + "m").formatted(Formatting.YELLOW), posUp, 1f, (MinecraftClient.getInstance().textRenderer.fontHeight + 1).toFloat(), true)
	}

	/**
	 * enum for the different waypoints used int the crystals hud each with a [name] and associated [color]
	 */
	enum class Category(override val name: String, val color: Color) {
		JUNGLE_TEMPLE("Jungle Temple", Color(DyeColor.PURPLE.signColor)),
		MINES_OF_DIVAN("Mines of Divan", Color.GREEN),
		GOBLIN_QUEENS_DEN("Goblin Queen's Den", Color(DyeColor.ORANGE.signColor)),
		LOST_PRECURSOR_CITY("Lost Precursor City", Color.CYAN),
		KHAZAD_DUM("Khazad-dûm", Color.YELLOW),
		FAIRY_GROTTO("Fairy Grotto", Color.PINK),
		DRAGONS_LAIR("Dragon's Lair", Color.BLACK),
		CORLEONE("Corleone", Color.WHITE),
		KING_YOLKAR("King Yolkar", Color.RED),
		ODAWA("Odawa", Color.MAGENTA),
		KEY_GUARDIAN("Key Guardian", Color.LIGHT_GRAY);

		val colorComponents: FloatArray = color.getColorComponents(null)

		override fun toString(): String {
			return name
		}
	}

	companion object {
		private val CONFIG = Supplier { SkyblockerConfigManager.get().uiAndVisuals.waypoints }
		private val TYPE_SUPPLIER = Supplier { CONFIG.get().waypointType }
		fun getSquaredDistanceToFunction(entity: Entity): ToDoubleFunction<CrystalsWaypoint> {
			return ToDoubleFunction { crystalsWaypoint: CrystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos) }
		}

		fun getRangePredicate(entity: Entity): Predicate<CrystalsWaypoint> {
			return Predicate { crystalsWaypoint: CrystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos) <= 36.0 }
		}
	}
}
