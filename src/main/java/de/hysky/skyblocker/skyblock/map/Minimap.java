package de.hysky.skyblocker.skyblock.map;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
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

	private Minimap() {
		throw new IllegalStateException("Utility class can't be initialized!");
	}

	public static void render(DrawContext context) {
		if (MinecraftClient.getInstance().player == null) return;
		String map = Utils.getMap();
		if (map.isEmpty()) return; //Wait until locraw is parsed so there's no flickering of the map when joining a world
		if (map.equals("Private Island") //Nothing to draw here
				|| map.equals("Garden") //Could be added later on
				|| map.equals("Dungeon Hub") //Not drawing in dungeons is handled in the mixin
		) return;

		MatrixStack matrixStack = context.getMatrices();

		//Deep caverns isn't connected to the main islands' location scheme, it has its own (0,0) so we have to handle its location separately
		if (map.equals("Deep Caverns")) drawTexturedQuad(matrixStack, skyblockMap, 783.5f, 519.5f, 1196, 1308);
		else if (map.equals("The Rift")) drawTexturedQuad(matrixStack, riftMap, 358.5f, 391.5f, 663, 715);
		else drawTexturedQuad(matrixStack, skyblockMap, 791.5f, 1094.5f, 1196, 1308);

	}

	private static void drawTexturedQuad(MatrixStack matrices, Identifier texture, float centerX, float centerY, float textureWidth, float textureHeight) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		assert MinecraftClient.getInstance().player != null;

		float playerX = (float) MinecraftClient.getInstance().player.getX();
		float playerZ = (float) MinecraftClient.getInstance().player.getZ();
		if (rotationEnabled) {
			float yaw = (MinecraftClient.getInstance().player.getHeadYaw());

			bufferBuilder.vertex(matrix4f, x, y, 0)
						 .texture(calculateTextureX(playerX, centerX, yaw + 45, textureWidth),
								 calculateTextureY(playerZ, centerY, yaw + 45, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x, y + mapSize, 0)
						 .texture(calculateTextureX(playerX, centerX, yaw - 45, textureWidth),
								 calculateTextureY(playerZ, centerY, yaw - 45, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x + mapSize, y + mapSize, 0)
						 .texture(calculateTextureX(playerX, centerX, yaw - 135, textureWidth),
								 calculateTextureY(playerZ, centerY, yaw - 135, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x + mapSize, y, 0)
						 .texture(calculateTextureX(playerX, centerX, yaw - 225, textureWidth),
								 calculateTextureY(playerZ, centerY, yaw - 225, textureHeight)).next();
		} else {
			bufferBuilder.vertex(matrix4f, x, y, 0)
						 .texture(calculateTextureX(playerX, centerX, 225, textureWidth),
								 calculateTextureY(playerZ, centerY, 225, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x, y + mapSize, 0)
						 .texture(calculateTextureX(playerX, centerX, 135, textureWidth),
								 calculateTextureY(playerZ, centerY, 135, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x + mapSize, y + mapSize, 0)
						 .texture(calculateTextureX(playerX, centerX, 45, textureWidth),
								 calculateTextureY(playerZ, centerY, 45, textureHeight)).next();
			bufferBuilder.vertex(matrix4f, x + mapSize, y, 0)
						 .texture(calculateTextureX(playerX, centerX, -45, textureWidth),
								 calculateTextureY(playerZ, centerY, -45, textureHeight)).next();
		}
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	private static float calculateTextureX(float playerX, float centerX, float angle, float textureWidth) {
		return (playerX + centerX + mapSize / zoomFactor / MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.cos((angle) * MathConstants.RADIANS_PER_DEGREE)) / textureWidth;
	}

	private static float calculateTextureY(float playerZ, float centerY, float angle, float textureHeight) {
		return (playerZ + centerY + mapSize / zoomFactor / MathHelper.SQUARE_ROOT_OF_TWO * MathHelper.sin((angle) * MathConstants.RADIANS_PER_DEGREE)) / textureHeight;
	}
}
