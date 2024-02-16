package de.hysky.skyblocker.skyblock.hud.elements;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class DungeonMapElement extends UIElement {
	private static final DungeonMapElement INSTANCE = new DungeonMapElement();
	private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");

	private DungeonMapElement() {
		super(
				SkyblockerConfigManager.get().locations.dungeons.mapX,
				SkyblockerConfigManager.get().locations.dungeons.mapY,
				128,
				SkyblockerConfigManager.get().locations.dungeons.mapScaling
		);
	}

	public static DungeonMapElement getInstance() {
		return INSTANCE;
	}

	@Override
	public void saveToConfig() {
		SkyblockerConfigManager.get().locations.dungeons.mapX = x;
		SkyblockerConfigManager.get().locations.dungeons.mapY = y;
		SkyblockerConfigManager.get().locations.dungeons.mapScaling = scale;
	}

	@Override
	public void revertPositionChange() {
		x = SkyblockerConfigManager.get().locations.dungeons.mapX;
		y = SkyblockerConfigManager.get().locations.dungeons.mapY;
		scale = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
	}

	@Override
	public void resetPosition() {
		x = SkyblockerConfigManager.getDefaults().locations.dungeons.mapX;
		y = SkyblockerConfigManager.getDefaults().locations.dungeons.mapY;
		scale = SkyblockerConfigManager.getDefaults().locations.dungeons.mapScaling;
	}

	@Override
	public void renderConfigWidget(DrawContext context, float delta) {
		int scaledSize = getScaledWidth(); // The map is a square, so width and height are the same and either one can be used here
		context.drawTexture(MAP_BACKGROUND, x, y, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
	}

	@Override
	public void renderNormalWidget(DrawContext context, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return;
		ItemStack item = client.player.getInventory().main.get(8);
		NbtCompound tag = item.getNbt();
		MatrixStack matrices = context.getMatrices();

		if (tag != null && tag.contains("map")) {
			String tag2 = tag.asString();
			tag2 = StringUtils.substringBetween(tag2, "map:", "}");
			int mapid = Integer.parseInt(tag2);
			VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
			MapRenderer map = client.gameRenderer.getMapRenderer();
			MapState state = FilledMapItem.getMapState(mapid, client.world);

			if (state == null) return;
			matrices.push();
			matrices.translate(x, y, 0);
			matrices.scale(scale, scale, 0f);
			map.draw(matrices, vertices, mapid, state, false, 15728880);
			vertices.draw();
			matrices.pop();
		}
	}

	//Todo: Consider commands later.
}