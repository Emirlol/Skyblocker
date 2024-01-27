package de.hysky.skyblocker.skyblock.map;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class Minimap {
	private static final int mapSize = 128; //TODO: Get these from config and add a way to configure them
	private static final float zoomFactor = 1.0f;
	private static final int x = 5;
	private static final int y = 5;
	private static final boolean rotationEnabled = true;

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
		matrixStack.push();
		if (rotationEnabled) {
			matrixStack.multiply(new Quaternionf().rotateZ((float) (Math.PI + MinecraftClient.getInstance().player.getHeadYaw()*Math.PI/-180)), x+(mapSize/2.0f), y+(mapSize/2.0f), 0);
		}
		if (map.equals("Deep Caverns")) { //This place isn't connected to the main islands' location scheme, so we have to handle its location separately
			render(context, "textures/gui/map/skyblock.png", 783.5f, 519.5f, 1196, 1308);
		} else if (map.equals("The Rift")) {
			render(context, "textures/gui/map/rift.png", 358.5f, 391.5f, 663, 715);
		} else {
			render(context, "textures/gui/map/skyblock.png", 791.5f, 1094.5f, 1196, 1308);
		}

		matrixStack.pop();
	}

	private static void render(DrawContext context, String path, float offsetX, float offsetZ, int textureWidth, int textureHeight) {
		float playerX = (float) MinecraftClient.getInstance().player.getX();
		float playerZ = (float) MinecraftClient.getInstance().player.getZ();
		float centerOffset = (mapSize / 2.0f) / zoomFactor;
		int regionSize = (int) (mapSize / zoomFactor);
		context.drawTexture(Identifier.of(SkyblockerMod.NAMESPACE, path),
				x, y,
				mapSize, mapSize,
				offsetX + playerX - centerOffset, offsetZ + playerZ - centerOffset,
				regionSize, regionSize,
				textureWidth, textureHeight);
	}

}
