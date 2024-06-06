package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import de.hysky.skyblocker.utils.Utils.isInDungeons
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object DungeonScoreHUD {
	fun init() {
		HudRenderEvents.AFTER_MAIN_HUD.register(HudRenderStage { context: DrawContext, tickDelta: Float -> render(context) })
	}

	//This is 4+5 wide, needed to offset the extra width from bold numbers (3×1 wide) in S+ and the "+" (6 wide) so that it doesn't go off the screen if the score is S+ and the hud element is at the right edge of the screen
	private val extraSpace: Text = Text.literal(" ").append(Text.literal(" ").formatted(Formatting.BOLD))

	private fun render(context: DrawContext) {
		if (isInDungeons && DungeonScore.isDungeonStarted() && SkyblockerConfigManager.config.dungeons.dungeonScore.enableScoreHUD) {
			val x = SkyblockerConfigManager.config.dungeons.dungeonScore.scoreX
			val y = SkyblockerConfigManager.config.dungeons.dungeonScore.scoreY
			render(context, x, y)
		}
	}

	fun render(context: DrawContext, x: Int, y: Int) {
		val scale = SkyblockerConfigManager.config.dungeons.dungeonScore.scoreScaling
		val matrixStack = context.matrices
		matrixStack.push()
		matrixStack.scale(scale, scale, 0f)
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, formattedScoreText, (x / scale).toInt(), (y / scale).toInt(), -0x1)
		matrixStack.pop()
	}

	val formattedScoreText: Text
		get() = Text.translatable("skyblocker.dungeons.dungeonScore.scoreText", formatScore(DungeonScore.score))

	private fun formatScore(score: Int): Text {
		if (score < 100) return Text.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Text.literal(" (D)").formatted(Formatting.GRAY)).append(extraSpace)
		if (score < 160) return Text.literal(String.format("%03d", score)).withColor(0x4141FF).append(Text.literal(" (C)").formatted(Formatting.GRAY)).append(extraSpace)
		if (score < 230) return Text.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Text.literal(" (B)").formatted(Formatting.GRAY)).append(extraSpace)
		if (score < 270) return Text.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Text.literal(" (A)").formatted(Formatting.GRAY)).append(extraSpace)
		if (score < 300) return Text.literal(String.format("%03d", score)).withColor(0xF1E252).append(Text.literal(" (S)").formatted(Formatting.GRAY)).append(extraSpace)
		return Text.empty().append(Text.literal(String.format("%03d", score)).withColor(0xF1E252).formatted(Formatting.BOLD)).append(Text.literal(" (S+)").formatted(Formatting.GRAY))
	}
}
