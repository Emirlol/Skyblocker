package de.hysky.skyblocker.skyblock.map;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class Minimap {
	private static final int dim = 128;
	private static final float zoomFactor = 1.0f;
	private static final int x = 5;
	private static final int y = 5;

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

		float playerX = (float) MinecraftClient.getInstance().player.getX();
		float playerZ = (float) MinecraftClient.getInstance().player.getZ();
		float offset = (dim / 2.0f) / zoomFactor;
		int regionDim = (int) (dim / zoomFactor);
		if (map.equals("Deep Caverns")) { //This place isn't connected to the main islands' location scheme, so we have to handle its location separately
			context.drawTexture(
					Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/map/skyblock.png"),
					x, y,
					dim, dim,
					783.5f + playerX - offset, 519.5f + playerZ - offset,
					regionDim, regionDim,
					1196, 1308);
		} else if (map.equals("The Rift")) {
			context.drawTexture(Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/map/rift.png"),
					x, y,
					dim, dim,
					358.5f + playerX - offset, 391.5f + playerZ - offset,
					regionDim, regionDim,
					663, 715);

			//385, 391
		} else {
			context.drawTexture(Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/map/skyblock.png"),
					x, y,
					dim, dim,
					791.5f + playerX - offset, 1094.5f + playerZ - offset,
					regionDim, regionDim,
					1196, 1308);
		}
	}

}
