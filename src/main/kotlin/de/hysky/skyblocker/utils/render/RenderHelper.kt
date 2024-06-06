package de.hysky.skyblocker.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.mixins.accessors.BeaconBlockEntityRendererInvoker
import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling
import de.hysky.skyblocker.utils.render.title.Title
import de.hysky.skyblocker.utils.render.title.TitleContainer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.Event
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.texture.Scaling.NineSlice
import net.minecraft.client.texture.Sprite
import net.minecraft.sound.SoundEvents
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.math.min

object RenderHelper {
	private val TRANSLUCENT_DRAW = Identifier(SkyblockerMod.NAMESPACE, "translucent_draw")
	private val SCHEDULE_DEFERRED_RENDER_TASK = deferredRenderTaskHandle
	private val ONE = Vec3d(1.0, 1.0, 1.0)
	private const val MAX_OVERWORLD_BUILD_HEIGHT = 319
	private val client: MinecraftClient = MinecraftClient.getInstance()

	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.addPhaseOrdering(Event.DEFAULT_PHASE, TRANSLUCENT_DRAW)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(TRANSLUCENT_DRAW, ::drawTranslucents)
	}

	fun renderFilledWithBeaconBeam(context: WorldRenderContext, pos: BlockPos, colorComponents: FloatArray, alpha: Float, throughWalls: Boolean) {
		renderFilled(context, pos, colorComponents, alpha, throughWalls)
		renderBeaconBeam(context, pos, colorComponents)
	}

	fun renderFilled(context: WorldRenderContext, pos: BlockPos?, colorComponents: FloatArray, alpha: Float, throughWalls: Boolean) {
		renderFilled(context, Vec3d.of(pos), ONE, colorComponents, alpha, throughWalls)
	}

	fun renderFilled(context: WorldRenderContext, pos: BlockPos, dimensions: Vec3d, colorComponents: FloatArray, alpha: Float, throughWalls: Boolean) {
		if (throughWalls) {
			if (FrustumUtils.isVisible(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z)) {
				renderFilled(context, Vec3d.of(pos), dimensions, colorComponents, alpha, true)
			}
		} else {
			if (OcclusionCulling.regularCuller.isVisible(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z)) {
				renderFilled(context, Vec3d.of(pos), dimensions, colorComponents, alpha, false)
			}
		}
	}

	private fun renderFilled(context: WorldRenderContext, pos: Vec3d, dimensions: Vec3d, colorComponents: FloatArray, alpha: Float, throughWalls: Boolean) {
		val matrices = context.matrixStack()
		val camera = context.camera().pos

		matrices!!.push()
		matrices.translate(-camera.x, -camera.y, -camera.z)

		val consumers = context.consumers()
		val buffer = consumers!!.getBuffer(if (throughWalls) SkyblockerRenderLayers.FILLED_THROUGH_WALLS else SkyblockerRenderLayers.FILLED)

		WorldRenderer.renderFilledBox(matrices, buffer, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colorComponents[0], colorComponents[1], colorComponents[2], alpha)

		matrices.pop()
	}

	private fun renderBeaconBeam(context: WorldRenderContext, pos: BlockPos, colorComponents: FloatArray) {
		if (FrustumUtils.isVisible(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), (pos.x + 1).toDouble(), MAX_OVERWORLD_BUILD_HEIGHT.toDouble(), (pos.z + 1).toDouble())) {
			val matrices = context.matrixStack()
			val camera = context.camera().pos

			matrices!!.push()
			matrices.translate(pos.x - camera.getX(), pos.y - camera.getY(), pos.z - camera.getZ())

			BeaconBlockEntityRendererInvoker.renderBeam(matrices, context.consumers(), context.tickDelta(), context.world().time, 0, MAX_OVERWORLD_BUILD_HEIGHT, colorComponents)

			matrices.pop()
		}
	}

	/**
	 * Renders the outline of a box with the specified color components and line width.
	 * This does not use renderer since renderer draws outline using debug lines with a fixed width.
	 */
	fun renderOutline(context: WorldRenderContext, box: Box, colorComponents: FloatArray, lineWidth: Float, throughWalls: Boolean) {
		if (FrustumUtils.isVisible(box)) {
			val matrices = context.matrixStack()
			val camera = context.camera().pos
			val tessellator = RenderSystem.renderThreadTesselator()
			val buffer = tessellator.buffer

			RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
			RenderSystem.lineWidth(lineWidth)
			RenderSystem.disableCull()
			RenderSystem.enableDepthTest()
			RenderSystem.depthFunc(if (throughWalls) GL11.GL_ALWAYS else GL11.GL_LEQUAL)

			matrices!!.push()
			matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ())

			buffer.begin(DrawMode.LINES, VertexFormats.LINES)
			WorldRenderer.drawBox(matrices, buffer, box, colorComponents[0], colorComponents[1], colorComponents[2], 1f)
			tessellator.draw()

			matrices.pop()
			RenderSystem.lineWidth(1f)
			RenderSystem.enableCull()
			RenderSystem.disableDepthTest()
			RenderSystem.depthFunc(GL11.GL_LEQUAL)
		}
	}

	/**
	 * Draws lines from point to point.<br></br><br></br>
	 *
	 *
	 * Tip: To draw lines from the center of a block, offset the X, Y and Z each by 0.5
	 *
	 *
	 * Note: This is super messed up when drawing long lines. Tried different normals and [DrawMode.LINES] but nothing worked.
	 *
	 * @param context         The WorldRenderContext which supplies the matrices and tick delta
	 * @param points          The points from which to draw lines between
	 * @param colorComponents An array of R, G and B color components
	 * @param alpha           The alpha of the lines
	 * @param lineWidth       The width of the lines
	 * @param throughWalls    Whether to render through walls or not
	 */
	fun renderLinesFromPoints(context: WorldRenderContext, points: Array<Vec3d>, colorComponents: FloatArray, alpha: Float, lineWidth: Float, throughWalls: Boolean) {
		val camera = context.camera().pos
		val matrices = context.matrixStack()

		matrices!!.push()
		matrices.translate(-camera.x, -camera.y, -camera.z)

		val tessellator = RenderSystem.renderThreadTesselator()
		val buffer = tessellator.buffer
		val positionMatrix = matrices.peek().positionMatrix
		val normalMatrix = matrices.peek().normalMatrix

		GL11.glEnable(GL11.GL_LINE_SMOOTH)
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

		RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
		RenderSystem.lineWidth(lineWidth)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.disableCull()
		RenderSystem.enableDepthTest()
		RenderSystem.depthFunc(if (throughWalls) GL11.GL_ALWAYS else GL11.GL_LEQUAL)

		buffer.begin(DrawMode.LINE_STRIP, VertexFormats.LINES)

		for (i in points.indices) {
			val nextPoint = points[if (i + 1 == points.size) i - 1 else i + 1]
			val normalVec = Vector3f(nextPoint.getX().toFloat(), nextPoint.getY().toFloat(), nextPoint.getZ().toFloat()).sub(points[i].getX().toFloat(), points[i].getY().toFloat(), points[i].getZ().toFloat()).normalize().mul(normalMatrix)
			buffer
				.vertex(positionMatrix, points[i].getX().toFloat(), points[i].getY().toFloat(), points[i].getZ().toFloat())
				.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
				.normal(normalVec.x, normalVec.y, normalVec.z)
				.next()
		}

		tessellator.draw()

		matrices.pop()
		GL11.glDisable(GL11.GL_LINE_SMOOTH)
		RenderSystem.lineWidth(1f)
		RenderSystem.enableCull()
		RenderSystem.depthFunc(GL11.GL_LEQUAL)
	}

	fun renderLineFromCursor(context: WorldRenderContext, point: Vec3d, colorComponents: FloatArray, alpha: Float, lineWidth: Float) {
		val camera = context.camera().pos
		val matrices = context.matrixStack()

		matrices!!.push()
		matrices.translate(-camera.x, -camera.y, -camera.z)

		val tessellator = RenderSystem.renderThreadTesselator()
		val buffer = tessellator.buffer
		val positionMatrix = matrices.peek().positionMatrix

		GL11.glEnable(GL11.GL_LINE_SMOOTH)
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

		RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
		RenderSystem.lineWidth(lineWidth)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.disableCull()
		RenderSystem.enableDepthTest()
		RenderSystem.depthFunc(GL11.GL_ALWAYS)

		val offset = Vec3d.fromPolar(context.camera().pitch, context.camera().yaw)
		val cameraPoint = camera.add(offset)

		buffer.begin(DrawMode.LINES, VertexFormats.LINES)
		val normal = Vector3f(offset.x.toFloat(), offset.y.toFloat(), offset.z.toFloat())
		buffer
			.vertex(positionMatrix, cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat())
			.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
			.normal(normal.x, normal.y, normal.z)
			.next()

		buffer
			.vertex(positionMatrix, point.getX().toFloat(), point.getY().toFloat(), point.getZ().toFloat())
			.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
			.normal(normal.x, normal.y, normal.z)
			.next()


		tessellator.draw()

		matrices.pop()
		GL11.glDisable(GL11.GL_LINE_SMOOTH)
		RenderSystem.lineWidth(1f)
		RenderSystem.enableCull()
		RenderSystem.depthFunc(GL11.GL_LEQUAL)
	}

	fun renderQuad(context: WorldRenderContext, points: Array<Vec3d>, colorComponents: FloatArray, alpha: Float, throughWalls: Boolean) {
		val positionMatrix = Matrix4f()
		val camera = context.camera().pos

		positionMatrix.translate(-camera.x.toFloat(), -camera.y.toFloat(), -camera.z.toFloat())

		val tessellator = RenderSystem.renderThreadTesselator()
		val buffer = tessellator.buffer

		RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.disableCull()
		RenderSystem.depthFunc(if (throughWalls) GL11.GL_ALWAYS else GL11.GL_LEQUAL)

		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
		for (i in 0..3) {
			buffer.vertex(positionMatrix, points[i].getX().toFloat(), points[i].getY().toFloat(), points[i].getZ().toFloat()).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha).next()
		}
		tessellator.draw()

		RenderSystem.enableCull()
		RenderSystem.depthFunc(GL11.GL_LEQUAL)
	}

	fun renderText(context: WorldRenderContext, text: Text, pos: Vec3d, throughWalls: Boolean) {
		renderText(context, text, pos, 1f, throughWalls)
	}

	fun renderText(context: WorldRenderContext, text: Text, pos: Vec3d, scale: Float, throughWalls: Boolean) {
		renderText(context, text, pos, scale, 0f, throughWalls)
	}

	fun renderText(context: WorldRenderContext, text: Text, pos: Vec3d, scale: Float, yOffset: Float, throughWalls: Boolean) {
		renderText(context, text.asOrderedText(), pos, scale, yOffset, throughWalls)
	}

	/**
	 * Renders text in the world space.
	 *
	 * @param throughWalls whether the text should be able to be seen through walls or not.
	 */
	fun renderText(context: WorldRenderContext, text: OrderedText, pos: Vec3d, scale: Float, yOffset: Float, throughWalls: Boolean) {
		var scale = scale
		val positionMatrix = Matrix4f()
		val camera = context.camera()
		val cameraPos = camera.pos
		val textRenderer = client.textRenderer

		scale *= 0.025f

		positionMatrix
			.translate((pos.getX() - cameraPos.getX()).toFloat(), (pos.getY() - cameraPos.getY()).toFloat(), (pos.getZ() - cameraPos.getZ()).toFloat())
			.rotate(camera.rotation)
			.scale(-scale, -scale, scale)

		val xOffset = -textRenderer.getWidth(text) / 2f

		val tessellator = RenderSystem.renderThreadTesselator()
		val buffer = tessellator.buffer
		val consumers = VertexConsumerProvider.immediate(buffer)

		RenderSystem.depthFunc(if (throughWalls) GL11.GL_ALWAYS else GL11.GL_LEQUAL)

		textRenderer.draw(text, xOffset, yOffset, -0x1, false, positionMatrix, consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE)
		consumers.draw()

		RenderSystem.depthFunc(GL11.GL_LEQUAL)
	}

	/**
	 * This is called after all [WorldRenderEvents.AFTER_TRANSLUCENT] listeners have been called so that we can draw all remaining render layers.
	 */
	private fun drawTranslucents(context: WorldRenderContext) {
		//Draw all render layers that haven't been drawn yet - drawing a specific layer does nothing and idk why
		(context.consumers() as VertexConsumerProvider.Immediate).draw()
	}

	fun runOnRenderThread(runnable: () -> Unit) {
		if (RenderSystem.isOnRenderThread()) {
			runnable.invoke()
		} else if (SCHEDULE_DEFERRED_RENDER_TASK != null) { //Sodium
			try {
				SCHEDULE_DEFERRED_RENDER_TASK.invokeExact(runnable)
			} catch (t: Throwable) {
				TextHandler.error("Failed to schedule a render task!", t)
			}
		} else { //Vanilla
			RenderSystem.recordRenderCall { runnable.invoke() }
		}
	}

	/**
	 * Adds the title to [TitleContainer] and [plays the notification sound][.playNotificationSound] if the title is not in the [TitleContainer] already.
	 * No checking needs to be done on whether the title is in the [TitleContainer] already by the caller.
	 *
	 * @param title the title
	 */
	fun displayInTitleContainerAndPlaySound(title: Title) {
		if (TitleContainer.addTitle(title)) {
			playNotificationSound()
		}
	}

	/**
	 * Adds the title to [TitleContainer] for a set number of ticks and [plays the notification sound][.playNotificationSound] if the title is not in the [TitleContainer] already.
	 * No checking needs to be done on whether the title is in the [TitleContainer] already by the caller.
	 *
	 * @param title the title
	 * @param ticks the number of ticks the title will remain
	 */
	fun displayInTitleContainerAndPlaySound(title: Title, ticks: Int) {
		if (TitleContainer.addTitle(title, ticks)) {
			playNotificationSound()
		}
	}

	private fun playNotificationSound() = client.player?.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f)

	fun pointIsInArea(x: Double, y: Double, x1: Double, y1: Double, x2: Double, y2: Double) = x in x1..x2 && y in y1..y2

	private fun drawSprite(context: DrawContext, sprite: Sprite, i: Int, j: Int, k: Int, l: Int, x: Int, y: Int, z: Int, width: Int, height: Int, red: Float, green: Float, blue: Float, alpha: Float) {
		if (width == 0 || height == 0) {
			return
		}
		(context as DrawContextInvoker).invokeDrawTexturedQuad(sprite.atlasId, x, x + width, y, y + height, z, sprite.getFrameU(k.toFloat() / i.toFloat()), sprite.getFrameU((k + width).toFloat() / i.toFloat()), sprite.getFrameV(l.toFloat() / j.toFloat()), sprite.getFrameV((l + height).toFloat() / j.toFloat()), red, green, blue, alpha)
	}

	private fun drawSpriteTiled(context: DrawContext, sprite: Sprite, x: Int, y: Int, z: Int, width: Int, height: Int, i: Int, j: Int, tileWidth: Int, tileHeight: Int, k: Int, l: Int, red: Float, green: Float, blue: Float, alpha: Float) {
		if (width <= 0 || height <= 0) {
			return
		}
		require(tileWidth > 0 && tileHeight > 0) { "Tiled sprite texture size must be positive, got " + tileWidth + "x" + tileHeight }
		var m = 0
		while (m < width) {
			val n = min(tileWidth.toDouble(), (width - m).toDouble()).toInt()
			var o = 0
			while (o < height) {
				val p = min(tileHeight.toDouble(), (height - o).toDouble()).toInt()
				drawSprite(context, sprite, k, l, i, j, x + m, y + o, z, n, p, red, green, blue, alpha)
				o += tileHeight
			}
			m += tileWidth
		}
	}

	fun renderNineSliceColored(context: DrawContext, texture: Identifier?, x: Int, y: Int, width: Int, height: Int, red: Float, green: Float, blue: Float, alpha: Float) {
		val sprite = MinecraftClient.getInstance().guiAtlasManager.getSprite(texture)
		val scaling = MinecraftClient.getInstance().guiAtlasManager.getScaling(sprite) as? NineSlice ?: return
		val border = scaling.border()
		val z = 0

		val i = min(border.left().toDouble(), (width / 2).toDouble()).toInt()
		val j = min(border.right().toDouble(), (width / 2).toDouble()).toInt()
		val k = min(border.top().toDouble(), (height / 2).toDouble()).toInt()
		val l = min(border.bottom().toDouble(), (height / 2).toDouble()).toInt()
		if (width == scaling.width() && height == scaling.height()) {
			drawSprite(context, sprite, scaling.width(), scaling.height(), 0, 0, x, y, z, width, height, red, green, blue, alpha)
			return
		}
		if (height == scaling.height()) {
			drawSprite(context, sprite, scaling.width(), scaling.height(), 0, 0, x, y, z, i, height, red, green, blue, alpha)
			drawSpriteTiled(context, sprite, x + i, y, z, width - j - i, height, i, 0, scaling.width() - j - i, scaling.height(), scaling.width(), scaling.height(), red, green, blue, alpha)
			drawSprite(context, sprite, scaling.width(), scaling.height(), scaling.width() - j, 0, x + width - j, y, z, j, height, red, green, blue, alpha)
			return
		}
		if (width == scaling.width()) {
			drawSprite(context, sprite, scaling.width(), scaling.height(), 0, 0, x, y, z, width, k, red, green, blue, alpha)
			drawSpriteTiled(context, sprite, x, y + k, z, width, height - l - k, 0, k, scaling.width(), scaling.height() - l - k, scaling.width(), scaling.height(), red, green, blue, alpha)
			drawSprite(context, sprite, scaling.width(), scaling.height(), 0, scaling.height() - l, x, y + height - l, z, width, l, red, green, blue, alpha)
			return
		}
		drawSprite(context, sprite, scaling.width(), scaling.height(), 0, 0, x, y, z, i, k, red, green, blue, alpha)
		drawSpriteTiled(context, sprite, x + i, y, z, width - j - i, k, i, 0, scaling.width() - j - i, k, scaling.width(), scaling.height(), red, green, blue, alpha)
		drawSprite(context, sprite, scaling.width(), scaling.height(), scaling.width() - j, 0, x + width - j, y, z, j, k, red, green, blue, alpha)
		drawSprite(context, sprite, scaling.width(), scaling.height(), 0, scaling.height() - l, x, y + height - l, z, i, l, red, green, blue, alpha)
		drawSpriteTiled(context, sprite, x + i, y + height - l, z, width - j - i, l, i, scaling.height() - l, scaling.width() - j - i, l, scaling.width(), scaling.height(), red, green, blue, alpha)
		drawSprite(context, sprite, scaling.width(), scaling.height(), scaling.width() - j, scaling.height() - l, x + width - j, y + height - l, z, j, l, red, green, blue, alpha)
		drawSpriteTiled(context, sprite, x, y + k, z, i, height - l - k, 0, k, i, scaling.height() - l - k, scaling.width(), scaling.height(), red, green, blue, alpha)
		drawSpriteTiled(context, sprite, x + i, y + k, z, width - j - i, height - l - k, i, k, scaling.width() - j - i, scaling.height() - l - k, scaling.width(), scaling.height(), red, green, blue, alpha)
		drawSpriteTiled(context, sprite, x + width - j, y + k, z, i, height - l - k, scaling.width() - j, k, j, scaling.height() - l - k, scaling.width(), scaling.height(), red, green, blue, alpha)
	}

	private val colorBuffer = FloatArray(4)
	@JvmStatic
	fun renderNineSliceColored(context: DrawContext, texture: Identifier?, x: Int, y: Int, width: Int, height: Int, color: Color) {
		color.getComponents(colorBuffer)
		renderNineSliceColored(context, texture, x, y, width, height, colorBuffer[0], colorBuffer[1], colorBuffer[2], colorBuffer[3])
	}

	private val deferredRenderTaskHandle: MethodHandle?
		// TODO Get rid of reflection once the new Sodium is released
		get() {
			try {
				val deferredTaskClass = Class.forName("me.jellysquid.mods.sodium.client.render.util.DeferredRenderTask")

				val lookup = MethodHandles.publicLookup()
				val mt = MethodType.methodType(Void.TYPE, Runnable::class.java)

				return lookup.findStatic(deferredTaskClass, "schedule", mt)
			} catch (ignored: Throwable) {
			}

			return null
		}
}
