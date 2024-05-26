package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.RenderHelper.pointIsInArea
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class DungeonMapConfigScreen @JvmOverloads constructor(private val parent: Screen? = null) : Screen(Text.literal("Dungeon Map Config")) {
	private var mapX = SkyblockerConfigManager.get().dungeons.dungeonMap.mapX
	private var mapY = SkyblockerConfigManager.get().dungeons.dungeonMap.mapY
	private var scoreX = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX
	private var scoreY = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		renderBackground(context, mouseX, mouseY, delta)
		renderHUDMap(context, mapX, mapY)
		renderHUDScore(context, scoreX, scoreY)
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width shr 1, height shr 1, Color.GRAY.rgb)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		val mapSize = (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling).toInt()
		val scoreScaling = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling
		val scoreWidth = (textRenderer.getWidth(DungeonScoreHUD.getFormattedScoreText()) * scoreScaling).toInt()
		val scoreHeight = (textRenderer.fontHeight * scoreScaling).toInt()
		if (pointIsInArea(mouseX, mouseY, mapX.toDouble(), mapY.toDouble(), (mapX + mapSize).toDouble(), (mapY + mapSize).toDouble()) && button == 0) {
			mapX = max(min(mouseX - (mapSize shr 1), (this.width - mapSize).toDouble()), 0.0).toInt()
			mapY = max(min(mouseY - (mapSize shr 1), (this.height - mapSize).toDouble()), 0.0).toInt()
		} else if (pointIsInArea(mouseX, mouseY, scoreX.toDouble(), scoreY.toDouble(), (scoreX + scoreWidth).toDouble(), (scoreY + scoreHeight).toDouble()) && button == 0) {
			scoreX = max(min(mouseX - (scoreWidth shr 1), (this.width - scoreWidth).toDouble()), 0.0).toInt()
			scoreY = max(min(mouseY - (scoreHeight shr 1), (this.height - scoreHeight).toDouble()), 0.0).toInt()
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == 1) {
			mapX = 2
			mapY = 2
			scoreX = max(((mapX + (64 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling)) - textRenderer.getWidth(DungeonScoreHUD.getFormattedScoreText()) * SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling / 2).toInt().toDouble(), 0.0).toInt()
			scoreY = (mapY + (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling) + 4).toInt()
		}

		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun close() {
		SkyblockerConfigManager.get().dungeons.dungeonMap.mapX = mapX
		SkyblockerConfigManager.get().dungeons.dungeonMap.mapY = mapY
		SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX = scoreX
		SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY = scoreY
		SkyblockerConfigManager.save()

		client!!.setScreen(parent)
	}

	fun renderHUDMap(context: DrawContext, x: Int, y: Int) {
		val scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling
		val size = (128 * scaling).toInt()
		context.drawTexture(MAP_BACKGROUND, x, y, 0f, 0f, size, size, size, size)
	}

	fun renderHUDScore(context: DrawContext, x: Int, y: Int) {
		DungeonScoreHUD.render(context, x, y)
	}

	companion object {
		private val MAP_BACKGROUND = Identifier("textures/map/map_background.png")
	}
}
