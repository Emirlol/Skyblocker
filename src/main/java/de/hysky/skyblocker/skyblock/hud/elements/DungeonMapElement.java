package de.hysky.skyblocker.skyblock.hud.elements;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapId;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class DungeonMapElement extends UIElement {
	//This kind of singleton pattern is also a bit weird. I'm not sure if this is the way to go.
	//It can't be declared abstract from the parent class, so it's not possible to enforce the singleton pattern from the parent class.
	//Also, not sure if the constructor parameters should be here or hardcoded in the constructor (since this class is the only place using that constructor).
	private static final DungeonMapElement INSTANCE = new DungeonMapElement(
			SkyblockerConfigManager.get().locations.dungeons.mapX,
			SkyblockerConfigManager.get().locations.dungeons.mapY,
			128,
			SkyblockerConfigManager.get().locations.dungeons.mapScaling
	);
	private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");

	private DungeonMapElement(int x, int y, int size, float scale) {
		super(x, y, size, scale);
	}

	public static DungeonMapElement getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		HudRenderEvents.AFTER_MAIN_HUD.register(this::render);
	}

	//I'm also not sure about these 3 methods. They aren't really doing much and increase the amount of code to implement on each element.
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
	public void renderConfigElement(DrawContext context, float tickdelta) {
		int scaledSize = getScaledWidth(); // The map is a square, so width and height are the same and either one can be used here
		context.drawTexture(MAP_BACKGROUND, x, y, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
	}

	@Override
	public void renderNormalElement(DrawContext context, float tickdelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return;
		ItemStack item = client.player.getInventory().main.get(8);
		NbtCompound tag = item.getNbt();
		MatrixStack matrices = context.getMatrices();

		if (tag != null && tag.contains("map")) {
			String tag2 = tag.asString();
			tag2 = StringUtils.substringBetween(tag2, "map:", "}");
			MapId mapid = new MapId(Integer.parseInt(tag2));
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

	@Override
	public boolean shouldRender() {
		return Utils.isInDungeons() && DungeonScore.isDungeonStarted() && SkyblockerConfigManager.get().locations.dungeons.enableMap;
	}

	//Todo: Consider commands later.
}