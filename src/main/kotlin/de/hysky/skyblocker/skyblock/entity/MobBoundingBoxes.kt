package de.hysky.skyblocker.skyblock.entity

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.render.FrustumUtils.isVisible
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import de.hysky.skyblocker.utils.render.Renderable
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.DebugRender
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box

object MobBoundingBoxes {
	/**
	 * These boxes will be rendered before the debug render phase which happens after entities are rendered;
	 */
	private val BOXES_2_RENDER = ObjectOpenHashSet<RenderableBox>()

	fun init() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(DebugRender { obj: WorldRenderContext? -> render() })
	}

	@JvmStatic
	fun shouldDrawMobBoundingBox(entity: Entity): Boolean {
		val box = entity.boundingBox

		if (isInDungeons && isVisible(box) && !entity.isInvisible) {
			val name = entity.name.string

			return when (entity) {
				-> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes
				-> false
				else -> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes && MobGlow.isStarred(entity)
			}
		}

		return false
	}

	@JvmStatic
	fun getBoxColor(entity: Entity?): FloatArray {
		val color = MobGlow.getGlowColor(entity)

		return floatArrayOf(((color shr 16) and 0xFF) / 255f, ((color shr 8) and 0xFF) / 255f, (color and 0xFF) / 255f)
	}

	@JvmStatic
	fun submitBox2BeRendered(box: Box?, colorComponents: FloatArray?) {
		BOXES_2_RENDER.add(RenderableBox(box, colorComponents))
	}

	private fun render(context: WorldRenderContext) {
		for (box in BOXES_2_RENDER) {
			box.render(context)
		}

		BOXES_2_RENDER.clear()
	}

	@JvmRecord
	private data class RenderableBox(val box: Box?, val colorComponents: FloatArray?) : Renderable {
		override fun render(context: WorldRenderContext?) {
			renderOutline(context!!, box, colorComponents!!, 6f, false)
		}
	}
}
