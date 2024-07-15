package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.EggTypeArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EggFinder {
	private static final Pattern newEggPattern = Pattern.compile("^HOPPITY'S HUNT A Chocolate (Breakfast|Lunch|Dinner) Egg has appeared!$");
	private static final Logger logger = LoggerFactory.getLogger("Skyblocker Egg Finder");
	private static final Location[] possibleLocations = {Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.HUB, Location.THE_END, Location.THE_PARK, Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.SPIDERS_DEN, Location.THE_FARMING_ISLAND};
	private static boolean isLocationCorrect = false;

	private EggFinder() {
	}

	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> invalidateState());
		SkyblockEvents.LOCATION_CHANGE.register(EggFinder::handleLocationChange);
		ClientReceiveMessageEvents.GAME.register(EggFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggFinder::renderWaypoints);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("eggFinder")
						.then(literal("shareLocation")
								.then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
										.then(argument("eggType", EggTypeArgumentType.eggType())
												.executes(context -> {
													MessageScheduler.INSTANCE.sendMessageAfterCooldown("[Skyblocker] Chocolate " + context.getArgument("eggType", EggType.class) + " Egg found at " + context.getArgument("blockPos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource()).toShortString() + "!");
													return Command.SINGLE_SUCCESS;
												})))))));
	}

	public static void onParticle(ParticleS2CPacket packet) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder
				|| MinecraftClient.getInstance().world == null
				|| SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING
				|| !isLocationCorrect) return;
		BlockPos blockPos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());

		if (packet.getCount() == 0 && packet.getParameters() instanceof DustParticleEffect dustParticleEffect && dustParticleEffect.getScale() == 1) {
			searchForEntities(blockPos, dustParticleEffect.getColor());
		} else if (packet.getCount() == 3 && packet.getParameters().getType() == ParticleTypes.CRIT) {
			searchForEntities(blockPos, null);
		}
	}

	private static void handleLocationChange(Location location) {
		for (Location possibleLocation : possibleLocations) {
			if (location == possibleLocation) {
				isLocationCorrect = true;
				break;
			}
		}
	}

	private static void searchForEntities(@NotNull BlockPos blockPos, @Nullable Vector3f color) {
		assert MinecraftClient.getInstance().world != null;
		List<ArmorStandEntity> entities = MinecraftClient.getInstance().world.getEntitiesByClass(ArmorStandEntity.class, new Box(getEggLocFromParticle(blockPos)), entity -> true);
		for (ArmorStandEntity entity : entities) {
			handleArmorStand(entity, color);
		}
	}

	private static void handleArmorStand(@NotNull ArmorStandEntity armorStand, @Nullable Vector3f color) {
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || !armorStand.shouldHideBasePlate()) return;
		for (ItemStack itemStack : armorStand.getArmorItems()) {
			ItemUtils.getHeadTextureOptional(itemStack).ifPresent(texture -> {
				for (EggType type : EggType.entries) { //Compare blockPos rather than entity to avoid incorrect matches when the entity just moves rather than a new one being spawned elsewhere
					if (texture.equals(type.texture) ) {
						if (color == null) {
							if (type.waypoint == null) type.waypoint = new Waypoint(getEggLocFromEntity(armorStand), SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(type.color), false);
							else type.waypoint.setFound();
							return;
						} else if (type.particleColors.equals(color) && type.waypoint == null) {
							type.waypoint = new Waypoint(getEggLocFromEntity(armorStand), SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(type.color));

							MinecraftClient.getInstance().player.sendMessage(
									Constants.PREFIX.get()
									                .append("Found a ")
									                .append(Text.literal("Chocolate " + type + " Egg")
									                            .withColor(type.color))
									                .append(" at " + armorStand.getBlockPos().up(2).toShortString() + "!")
									                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker eggFinder shareLocation " + PosUtils.toSpaceSeparatedString(getEggLocFromEntity(armorStand)) + " " + type))
									                                      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to share the location in chat!").formatted(Formatting.GREEN)))));
							return;
						}
						logger.error("[Skyblocker Egg Finder] Found an egg with the correct texture but incorrect color. Expected: {} for type {}, Found: {}", type.particleColors, type, color);
					}
				}
			});
		}
	}

	private static @NotNull BlockPos getEggLocFromEntity(@NotNull ArmorStandEntity armorStandEntity) {
		return armorStandEntity.getBlockPos().up(2);
	}

	private static @NotNull BlockPos getEggLocFromParticle(@NotNull BlockPos blockPos) {
		return blockPos.down(2);
	}

	private static void invalidateState() {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		isLocationCorrect = false;
		for (EggType type : EggType.entries) {
			type.waypoint = null;
		}
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			Waypoint waypoint = type.waypoint;
			if (waypoint != null && waypoint.shouldRender()) waypoint.render(context);
		}
	}

	private static void onChatMessage(Text text, boolean overlay) {
		if (overlay || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		Matcher matcher = newEggPattern.matcher(text.getString());
		if (matcher.find()) {
			try {
				EggType eggType = EggType.from(matcher.group(1).toUpperCase());
				if (eggType != null) eggType.waypoint = null;
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg spawn message. Tried to match against: " + matcher.group(0), e);
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	//Removes that pesky "unboxing of Integer might cause NPE" warning when we already know it's not null
	public enum EggType {
		LUNCH(Formatting.BLUE.getColorValue(), new Vector3f(0.0f, 0.0f, 1.0f), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9"),
		DINNER(Formatting.GREEN.getColorValue(), new Vector3f(0.0f, 0.5019608f, 0.0f), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0"),
		BREAKFAST(Formatting.GOLD.getColorValue(), new Vector3f(1.0f, 0.64705884f, 0.0f), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0");

		private @Nullable Waypoint waypoint;
		public final int color;
		public final Vector3f particleColors;
		public final String texture;

		//This is to not create an array each time we iterate over the values
		public static final ObjectImmutableList<EggType> entries = ObjectImmutableList.of(BREAKFAST, LUNCH, DINNER);

		EggType(int color, Vector3f particleColor, String texture) {
			this.color = color;
			this.particleColors = particleColor;
			this.texture = texture;
		}

		@Nullable
		public static EggType from(String name) {
			try {
				return valueOf(name);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		@Override
		public String toString() {
			return WordUtils.capitalizeFully(name());
		}
	}
}
