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

class CrystalsWaypoint internal constructor(val category: Category, val name: Text, pos: BlockPos) : Waypoint(pos, SkyblockerConfigManager.config.uiAndVisuals.waypoints.waypointType, category.colorComponents) {
	private val centerPos: Vec3d = pos.toCenterPos()

	override fun equals(other: Any?) = super.equals(other) || other is CrystalsWaypoint && category == other.category && name == other.name && pos == other.pos

	override fun hashCode(): Int {
		var result = category.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + centerPos.hashCode()
		return result
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
	 * Enum for the different waypoints used in the crystals HUD, each with a [categoryName] and associated [color]
	 */
	enum class Category(private val categoryName: String, val color: Color) {
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

		override fun toString()= categoryName

	}

	companion object {
		fun getSquaredDistanceToFunction(entity: Entity): (CrystalsWaypoint) -> Double = { crystalsWaypoint: CrystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos) }
		fun getRangePredicate(entity: Entity): (CrystalsWaypoint) -> Boolean = { crystalsWaypoint: CrystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos) <= 36.0 }
	}
}
