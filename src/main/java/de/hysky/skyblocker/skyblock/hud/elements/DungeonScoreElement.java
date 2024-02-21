package de.hysky.skyblocker.skyblock.hud.elements;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonScoreElement extends UIElement {
	private static final DungeonScoreElement INSTANCE = new DungeonScoreElement(
			SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreX,
			SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreY,
			MinecraftClient.getInstance().textRenderer.getWidth(getFormattedScoreText(300)), //Create the widget based on the widest possible score text, so it doesn't go out of screen if it's on the right edge of the screen.
			MinecraftClient.getInstance().textRenderer.fontHeight,
			SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreScaling
	);
	private DungeonScoreElement(int x, int y, int width, int height, float scale) {
		super(x, y, width, height, scale);
	}

	@Override
	public void init() {
		HudRenderEvents.AFTER_MAIN_HUD.register(this::render);
	}

	@Override
	public void renderNormalElement(DrawContext context, float tickdelta) {
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		matrixStack.scale(scale, scale, 0);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getFormattedScoreText(), (int) (x / scale), (int) (y / scale), 0xFFFFFFFF);
		matrixStack.pop();
	}

	@Override
	public boolean shouldRender() {
		return Utils.isInDungeons() && DungeonScore.isDungeonStarted() && SkyblockerConfigManager.get().locations.dungeons.dungeonScore.enableScoreHUD;
	}

	public static DungeonScoreElement getInstance() {
		return INSTANCE;
	}

	@Override
	public void saveToConfig() {
		SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreX = x;
		SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreY = y;
		SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreScaling = scale;
	}

	@Override
	public void revertPositionChange() {
		x = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreX;
		y = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreY;
		scale = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreScaling;
	}

	@Override
	public void resetPosition() {
		x = SkyblockerConfigManager.getDefaults().locations.dungeons.dungeonScore.scoreX;
		y = SkyblockerConfigManager.getDefaults().locations.dungeons.dungeonScore.scoreY;
		scale = SkyblockerConfigManager.getDefaults().locations.dungeons.dungeonScore.scoreScaling;
	}

	public static Text getFormattedScoreText() {
		return getFormattedScoreText(DungeonScore.getScore());
	}

	public static Text getFormattedScoreText(int score) {
		return Text.translatable("skyblocker.dungeons.dungeonScore.scoreText", formatScore(score));
	}

	private static Text formatScore(int score) {
		if (score < 100) return Text.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Text.literal(" (D)").formatted(Formatting.GRAY));
		if (score < 160) return Text.literal(String.format("%03d", score)).withColor(0x4141FF).append(Text.literal(" (C)").formatted(Formatting.GRAY));
		if (score < 230) return Text.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Text.literal(" (B)").formatted(Formatting.GRAY));
		if (score < 270) return Text.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Text.literal(" (A)").formatted(Formatting.GRAY));
		if (score < 300) return Text.literal(String.format("%03d", score)).withColor(0xF1E252).append(Text.literal(" (S)").formatted(Formatting.GRAY));
		return Text.literal("").append(Text.literal(String.format("%03d", score)).withColor(0xF1E252).formatted(Formatting.BOLD)).append(Text.literal(" (S+)").formatted(Formatting.GRAY));
	}
}
