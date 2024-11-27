package de.hysky.skyblocker.skyblock.chocolatefactory.eggfinder;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.EggTypeArgumentType;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EggFinder {
	private static final Pattern eggFoundPattern = Pattern.compile("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper)", Pattern.CANON_EQ);
	private static final Logger logger = LoggerFactory.getLogger("Skyblocker Egg Finder");
	//This is most likely unnecessary with the addition of the location change packet, but it works fine and might be doing something so might as well keep it
	private static final LinkedList<ArmorStandEntity> armorStandQueue = new LinkedList<>();
	/**
	 * Squared distance from the player to the egg for the egg to be considered in range.
	 */
	private static final int MAX_DISTANCE = 64;
	/**
	 * The locations that the egg finder should work while the player is in.
	 */
	private static final List<Location> possibleLocations = List.of(Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.HUB, Location.THE_END, Location.THE_PARK, Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.SPIDERS_DEN, Location.THE_FARMING_ISLAND);
	/**
	 * Whether the player is in a location where the egg finder should work.
	 * This is set to false upon world change and will be checked with the location change event afterward.
	 */
	private static boolean isLocationCorrect = false;

	private static final @NotNull EnumMap<@NotNull Location, @NotNull Eggs> ALL_EGGS = new EnumMap<>(Location.class);

	private static @Nullable Eggs currentLocationEggs = null;

	public static final @NotNull List<@NotNull EggConsumer> consumers = new LinkedList<>();

	private EggFinder() {}

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register(EggFinder::invalidateState);
		SkyblockEvents.LOCATION_CHANGE.register(EggFinder::handleLocationChange);
		ClientReceiveMessageEvents.GAME.register(EggFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggFinder::renderWaypoints);
//		ClientTickEvents.END_CLIENT_TICK.register(EggFinder::tick);
		SkyblockTime.HOUR_CHANGE.register(EggFinder::onHourChange);
//		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
//				.then(literal("eggFinder")
//						.then(literal("shareLocation")
//								.then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
//										.then(argument("eggType", EggTypeArgumentType.eggType())
//												.executes(context -> {
//													MessageScheduler.INSTANCE.sendMessageAfterCooldown("[Skyblocker] Chocolate " + context.getArgument("eggType", EggType.class) + " Egg found at " + context.getArgument("blockPos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource()).toShortString() + "!");
//													return Command.SINGLE_SUCCESS;
//												})))))));

		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
				dispatcher.register(
						literal(SkyblockerMod.NAMESPACE).then(
								literal("eggFinder").then(
										literal("spawn").then(
												argument("eggType", EggTypeArgumentType.eggType()).then(
														argument("hourChanged", BoolArgumentType.bool()).then(
																argument("actuallySpawn", BoolArgumentType.bool()).executes(EggFinder::spawnEggCommand)
														)
												)
										)
								).then(
										literal("kill").then(
												argument("eggType", EggTypeArgumentType.eggType()).executes(context -> {
													String type = StringArgumentType.getString(context, "eggType");
													EggType eggType = EggType.entries.stream().filter(it -> it.namePattern.matcher(type).matches()).findFirst().orElse(null);
													if (eggType == null || currentLocationEggs == null) return Command.SINGLE_SUCCESS;
													Vec3d position = context.getSource().getPosition();
													List<ArmorStandEntity> result = new ArrayList<>();
													context.getSource().getWorld().collectEntitiesByType(TypeFilter.equals(ArmorStandEntity.class), Box.from(position.add(0, 2, 0)), e -> true, result, 1);
													if (result.size() != 1) return Command.SINGLE_SUCCESS;

													if (currentLocationEggs.brunch != null && currentLocationEggs.brunch.entity == result.getFirst()) currentLocationEggs.brunch = null;
													else if (currentLocationEggs.breakfast != null && currentLocationEggs.breakfast.entity == result.getFirst()) currentLocationEggs.breakfast = null;
													else if (currentLocationEggs.dejeuner != null && currentLocationEggs.dejeuner.entity == result.getFirst()) currentLocationEggs.dejeuner = null;
													else if (currentLocationEggs.dinner != null && currentLocationEggs.dinner.entity == result.getFirst()) currentLocationEggs.dinner = null;
													else if (currentLocationEggs.lunch != null && currentLocationEggs.lunch.entity == result.getFirst()) currentLocationEggs.lunch = null;
													else if (currentLocationEggs.supper != null && currentLocationEggs.supper.entity == result.getFirst()) currentLocationEggs.supper = null;
													result.getFirst().remove(Entity.RemovalReason.DISCARDED);

													return Command.SINGLE_SUCCESS;
												})
										)
								)
						)
				);
			});
			ClientPlayConnectionEvents.JOIN.register((a, b, c) -> {

			});
		}

		HudRenderEvents.LAST.register((context, tickCounter) -> {
			if (currentLocationEggs == null) return;
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Breakfast: " + currentLocationEggs.breakfast), 5, 200, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Brunch: " + currentLocationEggs.brunch), 5, 200 + 10, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Lunch: " + currentLocationEggs.lunch), 5, 200 + 20, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Dejeuner: " + currentLocationEggs.dejeuner), 5, 200 + 30, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Dinner: " + currentLocationEggs.dinner), 5, 200 + 40, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Supper: " + currentLocationEggs.supper), 5, 200 + 50, 0xFFFFFF);
		});
	}

	public static int spawnEggCommand(CommandContext<FabricClientCommandSource> context) {
		String type = StringArgumentType.getString(context, "eggType");
		boolean hourChanged = BoolArgumentType.getBool(context, "hourChanged");
		EggType eggType = EggType.entries.stream().filter(it -> it.namePattern.matcher(WordUtils.capitalizeFully(type)).matches()).findFirst().orElse(null);
		if (eggType == null) {
			context.getSource().sendError(Text.of("Invalid egg type."));
			return Command.SINGLE_SUCCESS;
		}
		boolean actuallySpawn = BoolArgumentType.getBool(context, "actuallySpawn");

		if (actuallySpawn) {
			Vec3d position = context.getSource().getPosition();
			ArmorStandEntity entity = new ArmorStandEntity(context.getSource().getWorld(), position.x, position.y + 1, position.z);
			ItemStack head = new ItemStack(Items.PLAYER_HEAD);
			PropertyMap propertyMap = new PropertyMap();
			propertyMap.put("textures", new Property("", eggType.texture, ""));
			head.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.empty(), propertyMap));
			entity.equipStack(EquipmentSlot.HEAD, head);
			context.getSource().getWorld().spawnEntity(entity);
		}

		if (hourChanged) {
			onHourChange(eggType.resetHour);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static void invalidateState(ClientPlayNetworkHandler handler, PacketSender ignored2, MinecraftClient ignored3) {
		if (Debug.debugEnabled() && handler.getServerInfo() != null && handler.getServerInfo().isLocal()) {
			handleLocationChange(Location.HUB); // Makes single-player debugging possible
		} else {
			isLocationCorrect = false;
			currentLocationEggs = null;
		}
	}

	private static void handleLocationChange(Location location) {
		isLocationCorrect = possibleLocations.contains(location);

		if (isLocationCorrect) {
			currentLocationEggs = ALL_EGGS.computeIfAbsent(location, it -> new Eggs());
		} else {
			armorStandQueue.clear();
			return;
		}
		while (!armorStandQueue.isEmpty()) {
			handleArmorStand(armorStandQueue.poll());
		}
	}

	public static void checkIfEgg(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) checkIfEgg(armorStand);
	}

	public static void checkIfEgg(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		if (SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return;
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || armorStand.shouldShowBasePlate()) return;
		if (Utils.getLocation() == Location.UNKNOWN) { // The location is unknown upon world change and will be changed via location change packets soon, so we can queue it for now
			armorStandQueue.add(armorStand);
			return;
		}
		if (isLocationCorrect) handleArmorStand(armorStand);
	}

	private static void handleArmorStand(ArmorStandEntity armorStand) {
		assert currentLocationEggs != null;
		ItemStack itemStack = armorStand.getEquippedStack(EquipmentSlot.HEAD);

		ItemUtils.getHeadTextureOptional(itemStack).ifPresent(texture -> {
			for (EggType type : EggType.entries) {
				if (!texture.equals(type.texture)) continue;
				if (currentLocationEggs.stream().anyMatch(egg -> egg.entity.getBlockPos().equals(armorStand.getBlockPos()))) return;
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Packet received"));

				Egg egg = new Egg(
						armorStand,
						new Waypoint(
								armorStand.getBlockPos().up(2),
								SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType,
								ColorUtils.getFloatComponents(type.color)
						),
						type
				);

				currentLocationEggs.add(egg);
				break;
			}
		});
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!isLocationCorrect || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder || MinecraftClient.getInstance().player == null || currentLocationEggs == null) return;
		currentLocationEggs.forEachNotNull(egg -> {
			if (egg.collected()) return;
			egg.waypoint.render(context);
		});
	}

	private static void onHourChange(int hour) {
		if (currentLocationEggs == null) return;
		Optional<EggType> eggTypeOptional = EggType.entries.stream()
		                                                   .filter(type -> type.resetHour == hour)
		                                                   .findFirst();
		if (eggTypeOptional.isEmpty()) return;
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Hour changed"));
		EggType eggType = eggTypeOptional.get();
		boolean isOddDay = getDay() % 2 == 1;

		currentLocationEggs.remove(eggType, isOddDay ? EggState.ODD_DAY : EggState.EVEN_DAY);
	}

	private static void onChatMessage(Text text, boolean overlay) {
		if (overlay || !isLocationCorrect || currentLocationEggs == null || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder || MinecraftClient.getInstance().player == null) return;
		Matcher matcher = eggFoundPattern.matcher(text.getString());
		if (!matcher.find()) return;
		String eggName = matcher.group(1);

		switch (eggName) {
			case "Breakfast" -> {
				if (currentLocationEggs.breakfast == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was breakfast, so we swap it with brunch and mark them accordingly.
				if (currentLocationEggs.breakfast.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.brunch != null) {
					Egg temp = currentLocationEggs.breakfast;
					currentLocationEggs.breakfast = currentLocationEggs.brunch;
					currentLocationEggs.brunch = temp;
				}
				currentLocationEggs.breakfast.collected(true);

				currentLocationEggs.breakfast.markAsOddDayEgg();
				if (currentLocationEggs.brunch != null) currentLocationEggs.brunch.markAsEvenDayEgg();
			}
			case "Brunch" -> {
				if (currentLocationEggs.brunch == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was brunch, so we swap it with breakfast and mark them accordingly.
				if (currentLocationEggs.brunch.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.breakfast != null) {
					Egg temp = currentLocationEggs.brunch;
					currentLocationEggs.brunch = currentLocationEggs.breakfast;
					currentLocationEggs.breakfast = temp;
				}
				currentLocationEggs.brunch.collected(true);

				currentLocationEggs.brunch.markAsEvenDayEgg();
				if (currentLocationEggs.breakfast != null) currentLocationEggs.breakfast.markAsOddDayEgg();
			}

			case "Lunch" -> {
				if (currentLocationEggs.lunch == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was lunch, so we swap it with dejeuner and mark them accordingly.
				if (currentLocationEggs.lunch.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.dejeuner != null) {
					Egg temp = currentLocationEggs.lunch;
					currentLocationEggs.lunch = currentLocationEggs.dejeuner;
					currentLocationEggs.dejeuner = temp;
				}
				currentLocationEggs.lunch.collected(true);

				currentLocationEggs.lunch.markAsOddDayEgg();
				if (currentLocationEggs.dejeuner != null) currentLocationEggs.dejeuner.markAsEvenDayEgg();
			}
			case "Déjeuner" -> {
				if (currentLocationEggs.dejeuner == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was dejeuner, so we swap it with lunch and mark them accordingly.
				if (currentLocationEggs.dejeuner.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.lunch != null) {
					Egg temp = currentLocationEggs.dejeuner;
					currentLocationEggs.dejeuner = currentLocationEggs.lunch;
					currentLocationEggs.lunch = temp;
				}
				currentLocationEggs.dejeuner.collected(true);

				currentLocationEggs.dejeuner.markAsEvenDayEgg();
				if (currentLocationEggs.lunch != null) currentLocationEggs.lunch.markAsOddDayEgg();
			}

			case "Dinner" -> {
				if (currentLocationEggs.dinner == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was dinner, so we swap it with supper and mark them accordingly.
				if (currentLocationEggs.dinner.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.supper != null) {
					Egg temp = currentLocationEggs.dinner;
					currentLocationEggs.dinner = currentLocationEggs.supper;
					currentLocationEggs.supper = temp;
				}
				currentLocationEggs.dinner.collected(true);

				currentLocationEggs.dinner.markAsOddDayEgg();
				if (currentLocationEggs.supper != null) currentLocationEggs.supper.markAsEvenDayEgg();
			}
			case "Supper" -> {
				if (currentLocationEggs.supper == null) return; // Theoretically shouldn't happen.
				//The player is too far from the egg we thought was supper, so we swap it with dinner and mark them accordingly.
				if (currentLocationEggs.supper.entity.squaredDistanceTo(MinecraftClient.getInstance().player) > MAX_DISTANCE && currentLocationEggs.dinner != null) {
					Egg temp = currentLocationEggs.supper;
					currentLocationEggs.supper = currentLocationEggs.dinner;
					currentLocationEggs.dinner = temp;
				}
				currentLocationEggs.supper.collected(true);

				currentLocationEggs.supper.markAsEvenDayEgg();
				if (currentLocationEggs.dinner != null) currentLocationEggs.dinner.markAsOddDayEgg();
			}
		}
	}

	/**
	 * <p>The chocolate eggs cycle like so: Breakfast → Lunch → Dinner → Brunch → Déjeuner → Supper.</p>
	 * <p>This cycle starts from early spring 1st and continues until the event ends at the end of late spring.</p>
	 * <p>Thus, we can figure out which egg </p>
	 *
	 * @return the skyblock day within the year.
	 * @implNote This will return the day of the year, from 1 to 372 (inclusive on both ends) without range check for simplicity.
	 * 		The event only takes place within the first 93 days, so the range is 1 to 93 when combined with the season checks.
	 */
	public static int getDay() {
		return SkyblockTime.skyblockMonth.get().ordinal() * 31 + SkyblockTime.skyblockDay.get();
	}

//	public static void tick(MinecraftClient client) {
//		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder || client.player == null) return;
//		if (!isLocationCorrect || SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return;
//		for (EggType type : EggType.entries) {
//			if (type.oddDayEgg != null && !type.oddDayCollected && FrustumUtils.isVisible(type.oddDayEgg.entity.getBoundingBox()) && client.player.canSee(type.oddDayEgg.entity)) {
//				type.oddDayEgg.waypoint.setSeen();
//			}
//			if (type.evenDayEgg != null && !type.evenDayCollected && FrustumUtils.isVisible(type.evenDayEgg.entity.getBoundingBox()) && client.player.canSee(type.evenDayEgg.entity)) {
//				type.evenDayEgg.waypoint.setSeen();
//			}
//		}
//	}
}
