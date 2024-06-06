package de.hysky.skyblocker.utils.waypoint

import de.hysky.skyblocker.utils.render.RenderHelper
import de.hysky.skyblocker.utils.render.Renderable
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

open class Waypoint(val pos: BlockPos, val typeSupplier: () -> Type, open val colorComponents: FloatArray, val alpha: Float = DEFAULT_HIGHLIGHT_ALPHA, val lineWidth: Float = DEFAULT_LINE_WIDTH, val throughWalls: Boolean = true, private var shouldRender: Boolean = true) : Renderable {
	val box: Box = Box(pos)

	constructor(pos: BlockPos, type: Type, colorComponents: FloatArray, alpha: Float = DEFAULT_HIGHLIGHT_ALPHA, lineWidth: Float = DEFAULT_LINE_WIDTH, throughWalls: Boolean = true, shouldRender: Boolean = true) : this(pos, { type }, colorComponents, alpha, lineWidth, throughWalls, shouldRender)

	open fun shouldRender() = shouldRender

	open fun setFound() {
		this.shouldRender = false
	}

	open fun setMissing() {
		this.shouldRender = true
	}

	fun toggle() {
		this.shouldRender = !this.shouldRender
	}

	override fun render(context: WorldRenderContext) {
		when (typeSupplier.invoke()) {
			Type.WAYPOINT -> RenderHelper.renderFilledWithBeaconBeam(context, pos, colorComponents, alpha, throughWalls)
			Type.OUTLINED_WAYPOINT -> {
				val colorComponents = colorComponents
				RenderHelper.renderFilledWithBeaconBeam(context, pos, colorComponents, alpha, throughWalls)
				RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls)
			}

			Type.HIGHLIGHT -> RenderHelper.renderFilled(context, pos, colorComponents, alpha, throughWalls)
			Type.OUTLINED_HIGHLIGHT -> {
				val colorComponents = colorComponents
				RenderHelper.renderFilled(context, pos, colorComponents, alpha, throughWalls)
				RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls)
			}

			Type.OUTLINE -> RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls)
		}
	}

	enum class Type {
		WAYPOINT,
		OUTLINED_WAYPOINT,
		HIGHLIGHT,
		OUTLINED_HIGHLIGHT,
		OUTLINE;

		override fun toString() = when (this) {
			WAYPOINT -> "Waypoint"
			OUTLINED_WAYPOINT -> "Outlined Waypoint"
			HIGHLIGHT -> "Highlight"
			OUTLINED_HIGHLIGHT -> "Outlined Highlight"
			OUTLINE -> "Outline"
		}
	}

	companion object {
		const val DEFAULT_HIGHLIGHT_ALPHA: Float = 0.5f
		const val DEFAULT_LINE_WIDTH: Float = 5f
	}
}
