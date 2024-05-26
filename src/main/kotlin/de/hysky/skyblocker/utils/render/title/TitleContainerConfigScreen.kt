package de.hysky.skyblocker.utils.render.title

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig
import de.hysky.skyblocker.utils.render.RenderHelper.pointIsInArea
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.gui.YACLScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.Vector2f
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Pair
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.util.Set

class TitleContainerConfigScreen @JvmOverloads constructor(private val parent: Screen? = null) : Screen(Text.of("Title Container HUD Config")) {
	private val example1 = Title(Text.literal("Test1").formatted(Formatting.RED))
	private val example2 = Title(Text.literal("Test23").formatted(Formatting.AQUA))
	private val example3 = Title(Text.literal("Testing1234").formatted(Formatting.DARK_GREEN))
	private var hudX = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x.toFloat()
	private var hudY = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y.toFloat()
	private var changedScale = false

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		renderBackground(context, mouseX, mouseY, delta)
		TitleContainer.render(context, Set.of(example1, example2, example3), hudX.toInt(), hudY.toInt(), delta)
		val direction = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction
		val alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment
		context.drawCenteredTextWithShadow(textRenderer, "Press Q/E to change Alignment: $alignment", width / 2, textRenderer.fontHeight * 2, Color.WHITE.rgb)
		context.drawCenteredTextWithShadow(textRenderer, "Press R to change Direction: $direction", width / 2, textRenderer.fontHeight * 3 + 5, Color.WHITE.rgb)
		context.drawCenteredTextWithShadow(textRenderer, "Press +/- to change Scale", width / 2, textRenderer.fontHeight * 4 + 10, Color.WHITE.rgb)
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, textRenderer.fontHeight * 5 + 15, Color.GRAY.rgb)

		val boundingBox = selectionBoundingBox
		val x1 = boundingBox.left.x.toInt()
		val y1 = boundingBox.left.y.toInt()
		val x2 = boundingBox.right.x.toInt()
		val y2 = boundingBox.right.y.toInt()

		context.drawHorizontalLine(x1, x2, y1, Color.RED.rgb)
		context.drawHorizontalLine(x1, x2, y2, Color.RED.rgb)
		context.drawVerticalLine(x1, y1, y2, Color.RED.rgb)
		context.drawVerticalLine(x2, y1, y2, Color.RED.rgb)
	}

	private val selectionBoundingBox: Pair<Vector2f, Vector2f>
		get() {
			val alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment

			val midWidth = selectionWidth / 2f
			var x1 = 0f
			var x2 = 0f
			val y1 = hudY
			val y2 = hudY + selectionHeight
			when (alignment) {
				UIAndVisualsConfig.Alignment.RIGHT -> {
					x1 = hudX - midWidth * 2
					x2 = hudX
				}

				UIAndVisualsConfig.Alignment.MIDDLE -> {
					x1 = hudX - midWidth
					x2 = hudX + midWidth
				}

				UIAndVisualsConfig.Alignment.LEFT -> {
					x1 = hudX
					x2 = hudX + midWidth * 2
				}
			}
			return Pair(Vector2f(x1, y1), Vector2f(x2, y2))
		}

	private val selectionHeight: Float
		get() {
			val scale = (3f * (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale / 100f))
			return if (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction == UIAndVisualsConfig.Direction.HORIZONTAL) (textRenderer.fontHeight * scale) else (textRenderer.fontHeight + 10f) * 3f * scale
		}

	private val selectionWidth: Float
		get() {
			val scale = (3f * (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale / 100f))
			return if (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction == UIAndVisualsConfig.Direction.HORIZONTAL) (textRenderer.getWidth("Test1") + 10 + textRenderer.getWidth("Test23") + 10 + textRenderer.getWidth("Testing1234")) * scale else textRenderer.getWidth("Testing1234") * scale
		}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		val midWidth = selectionWidth / 2
		val midHeight = selectionHeight / 2
		val alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment

		val boundingBox = selectionBoundingBox
		val x1 = boundingBox.left.x
		val y1 = boundingBox.left.y
		val x2 = boundingBox.right.x
		val y2 = boundingBox.right.y

		if (pointIsInArea(mouseX, mouseY, x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble()) && button == 0) {
			hudX = when (alignment) {
				UIAndVisualsConfig.Alignment.LEFT -> mouseX.toInt() - midWidth
				UIAndVisualsConfig.Alignment.MIDDLE -> mouseX.toInt().toFloat()
				UIAndVisualsConfig.Alignment.RIGHT -> mouseX.toInt() + midWidth
			}
			hudY = (mouseY - midHeight).toInt().toFloat()
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == 1) {
			hudX = width.toFloat() / 2
			hudY = this.height * 0.6f
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_Q) {
			val current = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment
			SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment = when (current) {
				UIAndVisualsConfig.Alignment.LEFT -> UIAndVisualsConfig.Alignment.MIDDLE
				UIAndVisualsConfig.Alignment.MIDDLE -> UIAndVisualsConfig.Alignment.RIGHT
				UIAndVisualsConfig.Alignment.RIGHT -> UIAndVisualsConfig.Alignment.LEFT
			}
		}
		if (keyCode == GLFW.GLFW_KEY_E) {
			val current = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment
			SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment = when (current) {
				UIAndVisualsConfig.Alignment.LEFT -> UIAndVisualsConfig.Alignment.RIGHT
				UIAndVisualsConfig.Alignment.MIDDLE -> UIAndVisualsConfig.Alignment.LEFT
				UIAndVisualsConfig.Alignment.RIGHT -> UIAndVisualsConfig.Alignment.MIDDLE
			}
		}
		if (keyCode == GLFW.GLFW_KEY_R) {
			val current = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction
			SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction = when (current) {
				UIAndVisualsConfig.Direction.HORIZONTAL -> UIAndVisualsConfig.Direction.VERTICAL
				UIAndVisualsConfig.Direction.VERTICAL -> UIAndVisualsConfig.Direction.HORIZONTAL
			}
		}
		if (keyCode == GLFW.GLFW_KEY_EQUAL) {
			SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale += 10f
			changedScale = true
		}
		if (keyCode == GLFW.GLFW_KEY_MINUS) {
			SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale -= 10f
			changedScale = true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}


	override fun close() {
		SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x = hudX.toInt()
		SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y = hudY.toInt()

		//TODO Come up with a better, less hacky solution for this in the future (:
		if (parent is YACLScreen) {
			val category: ConfigCategory = parent.config.categories().stream().filter { cat: ConfigCategory -> cat.name().string == I18n.translate("skyblocker.config.uiAndVisuals") }.findFirst().orElseThrow()
			val group = category.groups().stream().filter { grp: OptionGroup -> grp.name().string == I18n.translate("skyblocker.config.uiAndVisuals.titleContainer") }.findFirst().orElseThrow()

			val scaleOpt = group.options().first

			// Refresh the value in the config with the bound value
			if (changedScale) scaleOpt.forgetPendingValue()
		}

		SkyblockerConfigManager.save()
		client!!.setScreen(parent)
	}
}
