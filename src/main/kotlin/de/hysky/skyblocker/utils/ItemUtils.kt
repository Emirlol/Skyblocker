package de.hysky.skyblocker.utils

import com.google.gson.JsonParser
import com.mojang.authlib.properties.PropertyMap
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.SkyblockerMod
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.dynamic.Codecs
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object ItemUtils {
	const val ID: String = "id"
	const val UUID: String = "uuid"
	private val OBTAINED_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneId.systemDefault()).localizedBy(Locale.ENGLISH)
	private val OLD_OBTAINED_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy h:m a").withZone(ZoneId.of("UTC")).localizedBy(Locale.ENGLISH)
	val NOT_DURABILITY: Pattern = Pattern.compile("[^0-9 /]")
	val FUEL_PREDICATE = { line: String -> line.contains("Fuel: ") }
	private val EMPTY_ALLOWING_ITEM_CODEC: Codec<RegistryEntry<Item>> = Registries.ITEM.entryCodec

	val EMPTY_ALLOWING_ITEMSTACK_CODEC: Codec<ItemStack> = Codec.lazyInitialized {
		RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ItemStack> ->
			instance.group(
				EMPTY_ALLOWING_ITEM_CODEC.fieldOf("id").forGetter { it.registryEntry },
				Codec.INT.orElse(1).fieldOf("count").forGetter { it.count },
				ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter { it.componentChanges }
			).apply(instance) { item, count, changes -> ItemStack(item, count, changes) }
		}
	}

	fun dumpHeldItemCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
		return ClientCommandManager.literal("dumpHeldItem").executes { context ->
			context.source.sendFeedback(Text.literal("[Skyblocker Debug] Held Item: " + SkyblockerMod.GSON_COMPACT.toJson(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, context.source.player.mainHandStack).getOrThrow())))
			Command.SINGLE_SUCCESS
		}
	}

	@Suppress("deprecation")
	fun getCustomData(stack: ItemStack): NbtCompound {
		return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).nbt
	}

	/**
	 * Gets the Skyblock item id of the item stack.
	 *
	 * @param stack the item stack to get the internal name from
	 * @return the internal name of the item stack, or an empty string if the item stack is null or does not have an internal name
	 */
	fun getItemId(stack: ItemStack): String = getCustomData(stack).getString(ID)

	/**
	 * Gets the UUID of the item stack.
	 *
	 * @param stack the item stack to get the UUID from
	 * @return the UUID of the item stack, or an empty string if the item stack is null or does not have a UUID
	 */
	fun getItemUuid(stack: ItemStack): String = getCustomData(stack).getString(UUID)

	/**
	 * This method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
	 * Currently, there are two types of string timestamps the legacy which is built like this
	 * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
	 * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
	 * taking into account of their formats, we do the same. The final result looks like this
	 * "MMMM dd, yyyy" (December 24, 2020).
	 * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
	 * "25/04/20 16:38" → "January 04, 2022" instead of "April 25, 2020".
	 * This causes the museum rank to be much worse than it should be.
	 *
	 *
	 * This also handles the long timestamp format introduced in January 2024 where the timestamp is in epoch milliseconds.
	 *
	 * @param stack the item under the pointer
	 * @return if the item have a "Timestamp" it will be shown formated on the tooltip
	 */
	fun getTimestamp(stack: ItemStack): String? {
		val customData = getCustomData(stack)

		if (customData.contains("timestamp", NbtElement.LONG_TYPE.toInt())) {
			val date = Instant.ofEpochMilli(customData.getLong("timestamp"))
			return OBTAINED_DATE_FORMATTER.format(date)
		}

		if (customData.contains("timestamp", NbtElement.STRING_TYPE.toInt())) {
			val date = OLD_OBTAINED_DATE_FORMAT.parse(customData.getString("timestamp"))
			return OBTAINED_DATE_FORMATTER.format(date)
		}

		return null
	}

	fun hasCustomDurability(stack: ItemStack) = getCustomData(stack).let { it.contains("drill_fuel") || it.getString(ID) == "PICKONIMBUS" }

	fun getDurability(stack: ItemStack): Pair<Int, Int>? {
		val customData = getCustomData(stack)

		// TODO Calculate drill durability based on the drill_fuel flag, fuel_tank flag, and hotm level
		// TODO Cache the max durability and only update the current durability on inventory tick
		val pickonimbusDurability = customData.getInt("pickonimbus_durability")
		if (pickonimbusDurability > 0) {
			return pickonimbusDurability to 5000
		}

		val drillFuel = Formatting.strip(getLoreLineIf(stack, FUEL_PREDICATE))
		if (drillFuel != null) {
			val drillFuelStrings = NOT_DURABILITY.matcher(drillFuel).replaceAll("").trim { it <= ' ' }.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			return drillFuelStrings[0].toInt() to drillFuelStrings[1].toInt() * 1000
		}

		return null
	}

	fun getLoreLineIf(item: ItemStack, predicate: (String) -> Boolean): String? {
		for (line in getLore(item)) {
			val string = line.string
			if (predicate.invoke(string)) {
				return string
			}
		}

		return null
	}

	fun getLoreLineIfMatch(item: ItemStack, pattern: Pattern): Matcher? {
		for (line in getLore(item)) {
			val string = line.string
			val matcher = pattern.matcher(string)
			if (matcher.matches()) {
				return matcher
			}
		}

		return null
	}

	fun getLore(item: ItemStack): List<Text> = item.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()

	fun propertyMapWithTexture(textureValue: String): PropertyMap {
		return Codecs.GAME_PROFILE_PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString("[{\"name\":\"textures\",\"value\":\"$textureValue\"}]")).getOrThrow()
	}

	fun getHeadTexture(stack: ItemStack) = getHeadTextureNullable(stack) ?: ""

	fun getHeadTextureNullable(stack: ItemStack): String? {
		if (!stack.isOf(Items.PLAYER_HEAD) || !stack.contains(DataComponentTypes.PROFILE)) return null

		return stack[DataComponentTypes.PROFILE]!!.properties()["textures"]
			.asSequence()
			.map { it.value() }
			.firstOrNull()
	}

	val skyblockerStack: ItemStack
		get() {
			try {
				return ItemStack(Items.PLAYER_HEAD).apply {
					this[DataComponentTypes.PROFILE] = ProfileComponent(Optional.of("SkyblockerStack"), Optional.of(java.util.UUID.randomUUID()), propertyMapWithTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0="))
				}
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
}
