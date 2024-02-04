package de.hysky.skyblocker.skyblock.map;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class Minimap {
	private static final float mapSize = 128.0f; //TODO: Get these from config and add a way to configure them
	private static final float zoomFactor = 1.0f;
	private static final float x = 5.0f;
	private static final float y = 5.0f;
	private static final boolean rotationEnabled = true;
	private static final Identifier skyblockMap = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/map/skyblock.png");
	private static final Identifier riftMap = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/map/rift.png");
	private static final Identifier mapFrame = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/mapframe.png");

	private Minimap() {
		throw new IllegalStateException("Utility class can't be initialized!");
	}

	public static void render(DrawContext context, float tickDelta) {
		if (MinecraftClient.getInstance().player == null) return;
		String map = Utils.getMap();
		if (map.isEmpty()) return; //Wait until locraw is parsed so there's no flickering of the map when joining a world
		if (map.equals("Private Island") //Nothing to draw here
				|| map.equals("Garden") //Could be added later on
				|| map.equals("Dungeon Hub") || Utils.isInDungeons() //Will add this later on
		) return;

		MatrixStack matrixStack = context.getMatrices();

		//Deep caverns isn't connected to the main islands' location scheme, it has its own (0,0) so we have to handle its location separately
		if (map.equals("Deep Caverns")) drawTexturedQuad(matrixStack, skyblockMap, 783.5f, 519.5f, 1196, 1308, tickDelta);
		else if (map.equals("The Rift")) drawTexturedQuad(matrixStack, riftMap, 358.5f, 391.5f, 663, 715, tickDelta);
		else drawTexturedQuad(matrixStack, skyblockMap, 791.5f, 1094.5f, 1196, 1308, tickDelta);

		renderMapFrame(context);
		renderCardinalDirections(context, tickDelta);
	}

	private static void drawTexturedQuad(MatrixStack matrices, Identifier texture, float centerX, float centerY, float textureWidth, float textureHeight, float tickDelta) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		assert MinecraftClient.getInstance().player != null;

		float playerX = (float) MathHelper.lerp(tickDelta, MinecraftClient.getInstance().player.prevX, MinecraftClient.getInstance().player.getX());
		float playerZ = (float) MathHelper.lerp(tickDelta, MinecraftClient.getInstance().player.prevZ, MinecraftClient.getInstance().player.getZ());
		float topLeft = 225.0f;
		if (rotationEnabled) {
			topLeft += MathHelper.lerp(tickDelta, MinecraftClient.getInstance().player.prevYaw, MinecraftClient.getInstance().player.getYaw()) - 180.0f;
		}
		bufferBuilder.vertex(matrix4f, x, y, 0)
					 .texture(calculateTextureX(playerX, centerX, topLeft, textureWidth),
							 calculateTextureY(playerZ, centerY, topLeft, textureHeight)).next();
		bufferBuilder.vertex(matrix4f, x, y + mapSize, 0)
					 .texture(calculateTextureX(playerX, centerX, topLeft - 90.0f, textureWidth),
							 calculateTextureY(playerZ, centerY, topLeft - 90.0f, textureHeight)).next();
		bufferBuilder.vertex(matrix4f, x + mapSize, y + mapSize, 0)
					 .texture(calculateTextureX(playerX, centerX, topLeft - 180.0f, textureWidth),
							 calculateTextureY(playerZ, centerY, topLeft - 180.0f, textureHeight)).next();
		bufferBuilder.vertex(matrix4f, x + mapSize, y, 0)
					 .texture(calculateTextureX(playerX, centerX, topLeft - 270.0f, textureWidth),
							 calculateTextureY(playerZ, centerY, topLeft - 270.f, textureHeight)).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	private static void renderMapFrame(DrawContext context) {
		context.drawTexture(mapFrame, (int) x, (int) y, 0, 0, 0, (int) mapSize, (int) mapSize, (int) mapSize, (int) mapSize);
	}

	private static void renderCardinalDirections(DrawContext context, float tickDelta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		float east = -180.0f;
		if (rotationEnabled) {
			assert MinecraftClient.getInstance().player != null;
			east = -MathHelper.lerp(tickDelta, MinecraftClient.getInstance().player.prevYaw, MinecraftClient.getInstance().player.getYaw()) - 180.0f;
		}
		context.drawCenteredTextWithShadow(textRenderer, "N", (int) calculateTextX(east + 270.0f), (int) calculateTextY(east + 270.0f), 0xFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, "E", (int) calculateTextX(east), (int) calculateTextY(east), 0xFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, "W", (int) calculateTextX(east + 180.0f), (int) calculateTextY(east + 180.0f), 0xFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, "S", (int) calculateTextX(east + 90.0f), (int) calculateTextY(east + 90.0f), 0xFFFFFF);
	}

	private static float calculateTextureX(float playerX, float centerX, float angle, float textureWidth) {
		return (playerX + centerX + mapSize / zoomFactor / MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.cos((angle) * MathConstants.RADIANS_PER_DEGREE)) / textureWidth;
	}

	private static float calculateTextureY(float playerZ, float centerY, float angle, float textureHeight) {
		return (playerZ + centerY + mapSize / zoomFactor / MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.sin((angle) * MathConstants.RADIANS_PER_DEGREE)) / textureHeight;
	}

	private static float calculateTextX(float angle) {
		float centerX = x + mapSize / 2;
		angle = MathHelper.wrapDegrees(angle);
		if (angle >= -45.0f && angle <= 45.0f) return centerX + mapSize / 2; //Right edge
		if (angle >= 135.0f || angle <= -135.0f) return centerX - mapSize / 2; //Left edge
		return (centerX + mapSize / 2 * MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.cos(angle * MathConstants.RADIANS_PER_DEGREE)); //Top or bottom edge, depending on Y.
	}

	private static float calculateTextY(float angle) {
		float centerY = y + mapSize / 2;
		angle = MathHelper.wrapDegrees(angle);
		if (angle >= -135.0f && angle <= -45.0f) return centerY - mapSize / 2 - 4; //Top edge
		if (angle >= 45.0f && angle <= 135.0f) return centerY + mapSize / 2 - 4; //Bottom edge
		return centerY + mapSize / 2 * MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.sin(angle * MathConstants.RADIANS_PER_DEGREE) - 4; //Left or right edge, depending on X.
	}
}
