package de.hysky.skyblocker.debug

import de.hysky.skyblocker.utils.render.RenderHelper.renderFilledWithBeaconBeam
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import de.hysky.skyblocker.utils.render.RenderHelper.renderQuad
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.minecraft.SharedConstants
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object SnapshotDebug {
	private val RED = floatArrayOf(1.0f, 0.0f, 0.0f)
	private const val ALPHA = 0.5f
	private const val LINE_WIDTH = 8f

	private val isInSnapshot: Boolean
		get() = !SharedConstants.getGameVersion().isStable

	@JvmStatic
	fun init() {
		if (isInSnapshot) {
			WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> renderTest() })
		}
	}

	private fun renderTest(wrc: WorldRenderContext) {
		renderFilledWithBeaconBeam(wrc, BlockPos(175, 63, -14), RED, ALPHA, true)
		renderLinesFromPoints(wrc, arrayOf(Vec3d(173.0, 66.0, -7.5), Vec3d(178.0, 66.0, -7.5)), RED, ALPHA, LINE_WIDTH, false)
		renderQuad(wrc, arrayOf(Vec3d(183.0, 66.0, -16.0), Vec3d(183.0, 63.0, -16.0), Vec3d(183.0, 63.0, -14.0), Vec3d(183.0, 66.0, -14.0)), RED, ALPHA, false)
		renderText(wrc, Text.of("Skyblocker on " + SharedConstants.getGameVersion().name + "!"), Vec3d(175.5, 67.5, -7.5), false)
	}
}
