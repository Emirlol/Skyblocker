package de.hysky.skyblocker.skyblock.item.tooltip

import com.google.gson.JsonObject
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.GeneralConfig
import de.hysky.skyblocker.skyblock.item.MuseumItemCache.hasItemInMuseum
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper.AccessoryReport
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.ItemUtils.getTimestamp
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.Map
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.dropLastWhile
import kotlin.collections.indices
import kotlin.collections.toTypedArray
import kotlin.concurrent.Volatile

object ItemTooltip {
	@JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(ItemTooltip::class.java.name)
	private val client: MinecraftClient = MinecraftClient.getInstance()
	internal val config: GeneralConfig.ItemTooltip = SkyblockerConfigManager.config.general.itemTooltip

	@Volatile
	private var sentNullWarning = false

	fun getTooltip(stack: ItemStack, tooltipContext: Item.TooltipContext?, tooltipType: TooltipType?, lines: MutableList<Text>) {
		if (!isOnSkyblock || client.player == null) return

		smoothenLines(lines)

		var name = getInternalNameFromNBT(stack, false)
		val internalID = getInternalNameFromNBT(stack, true)
		var neuName = name
		if (name == null || internalID == null) return

		if (name.startsWith("ISSHINY_")) {
			name = "SHINY_$internalID"
			neuName = internalID
		}

		if (lines.isEmpty()) {
			return
		}

		val count = stack.count
		val bazaarOpened = lines.stream().anyMatch { each: Text -> each.string.contains("Buy price:") || each.string.contains("Sell price:") }

		if (TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(
				Text.literal(String.format("%-21s", "NPC Sell Price:"))
					.formatted(Formatting.YELLOW)
					.append(getCoinsMessage(TooltipInfoType.NPC.data!![internalID].asDouble, count))
			)
		}

		var bazaarExist = false

		if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name) && !bazaarOpened) {
			val getItem = TooltipInfoType.BAZAAR.data!!.getAsJsonObject(name)
			lines.add(
				Text.literal(String.format("%-18s", "Bazaar buy Price:"))
					.formatted(Formatting.GOLD)
					.append(
						if (getItem["buyPrice"].isJsonNull
						) Text.literal("No data").formatted(Formatting.RED)
						else getCoinsMessage(getItem["buyPrice"].asDouble, count)
					)
			)
			lines.add(
				Text.literal(String.format("%-19s", "Bazaar sell Price:"))
					.formatted(Formatting.GOLD)
					.append(
						if (getItem["sellPrice"].isJsonNull
						) Text.literal("No data").formatted(Formatting.RED)
						else getCoinsMessage(getItem["sellPrice"].asDouble, count)
					)
			)
			bazaarExist = true
		}

		// bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
		var lbinExist = false
		if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !bazaarOpened && !bazaarExist) {
			lines.add(
				Text.literal(String.format("%-19s", "Lowest BIN Price:"))
					.formatted(Formatting.GOLD)
					.append(getCoinsMessage(TooltipInfoType.LOWEST_BINS.data!![name].asDouble, count))
			)
			lbinExist = true
		}

		if (SkyblockerConfigManager.config.general.itemTooltip.enableAvgBIN) {
			if (TooltipInfoType.ONE_DAY_AVERAGE.data == null || TooltipInfoType.THREE_DAY_AVERAGE.data == null) {
				nullWarning()
			} else {
				/*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
				neuName = getNeuName(internalID, neuName!!)

				if (!neuName.isEmpty() && lbinExist) {
					val type = config.avg

					// "No data" line because of API not keeping old data, it causes NullPointerException
					if (type == GeneralConfig.Average.ONE_DAY || type == GeneralConfig.Average.BOTH) {
						lines.add(
							Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
								.formatted(Formatting.GOLD)
								.append(
									if (TooltipInfoType.ONE_DAY_AVERAGE.data!![neuName] == null
									) Text.literal("No data").formatted(Formatting.RED)
									else getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.data!![neuName].asDouble, count)
								)
						)
					}
					if (type == GeneralConfig.Average.THREE_DAY || type == GeneralConfig.Average.BOTH) {
						lines.add(
							Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
								.formatted(Formatting.GOLD)
								.append(
									if (TooltipInfoType.THREE_DAY_AVERAGE.data!![neuName] == null
									) Text.literal("No data").formatted(Formatting.RED)
									else getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.data!![neuName].asDouble, count)
								)
						)
					}
				}
			}
		}

		val itemTierFloors = Map.of(
			1, "F1",
			2, "F2",
			3, "F3",
			4, "F4/M1",
			5, "F5/M2",
			6, "F6/M3",
			7, "F7/M4",
			8, "M5",
			9, "M6",
			10, "M7"
		)

		if (SkyblockerConfigManager.config.general.itemTooltip.dungeonQuality) {
			val customData = getCustomData(stack)
			if (customData != null && customData.contains("baseStatBoostPercentage")) {
				val baseStatBoostPercentage = customData.getInt("baseStatBoostPercentage")
				val maxQuality = baseStatBoostPercentage == 50
				if (maxQuality) {
					lines.add(Text.literal(String.format("%-17s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.RED).formatted(Formatting.BOLD))
				} else {
					lines.add(Text.literal(String.format("%-21s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.BLUE))
				}
				if (customData.contains("item_tier")) {     // sometimes it just isn't here?
					val itemTier = customData.getInt("item_tier")
					if (maxQuality) {
						lines.add(Text.literal(String.format("%-17s", "Floor Tier:") + itemTier + " (" + itemTierFloors[itemTier] + ")").formatted(Formatting.RED).formatted(Formatting.BOLD))
					} else {
						lines.add(Text.literal(String.format("%-21s", "Floor Tier:") + itemTier + " (" + itemTierFloors[itemTier] + ")").formatted(Formatting.BLUE))
					}
				}
			}
		}

		if (TooltipInfoType.MOTES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(
				Text.literal(String.format("%-20s", "Motes Price:"))
					.formatted(Formatting.LIGHT_PURPLE)
					.append(getMotesMessage(TooltipInfoType.MOTES.data!![internalID].asInt, count))
			)
		}

		if (TooltipInfoType.OBTAINED.isTooltipEnabled()) {
			val timestamp = getTimestamp(stack)

			if (!timestamp.isEmpty()) {
				lines.add(
					Text.literal(String.format("%-21s", "Obtained: "))
						.formatted(Formatting.LIGHT_PURPLE)
						.append(Text.literal(timestamp).formatted(Formatting.RED))
				)
			}
		}

		if (TooltipInfoType.MUSEUM.isTooltipEnabledAndHasOrNullWarning(internalID) && !bazaarOpened) {
			val itemCategory = TooltipInfoType.MUSEUM.data!![internalID].asString
			val format = when (itemCategory) {
				"Weapons" -> "%-18s"
				"Armor" -> "%-19s"
				else -> "%-20s"
			}

			//Special case the special category so that it doesn't always display not donated
			if (itemCategory == "Special") {
				lines.add(
					Text.literal(String.format(format, "Museum: ($itemCategory)"))
						.formatted(Formatting.LIGHT_PURPLE)
				)
			} else {
				val customData = getCustomData(stack)
				val isInMuseum = (customData.contains("donated_museum") && customData.getBoolean("donated_museum")) || hasItemInMuseum(internalID)

				val donatedIndicatorFormatting = if (isInMuseum) Formatting.GREEN else Formatting.RED

				lines.add(
					Text.literal(String.format(format, "Museum ($itemCategory):"))
						.formatted(Formatting.LIGHT_PURPLE)
						.append(Text.literal(if (isInMuseum) "✔" else "✖").formatted(donatedIndicatorFormatting, Formatting.BOLD))
						.append(Text.literal(if (isInMuseum) " Donated" else " Not Donated").formatted(donatedIndicatorFormatting))
				)
			}
		}

		if (TooltipInfoType.COLOR.isTooltipEnabledAndHasOrNullWarning(internalID) && stack.contains(DataComponentTypes.DYED_COLOR)) {
			val uuid = getItemUuid(stack)
			val hasCustomDye = SkyblockerConfigManager.config.general.customDyeColors.containsKey(uuid) || SkyblockerConfigManager.config.general.customAnimatedDyes.containsKey(uuid)
			//DyedColorComponent#getColor returns ARGB so we mask out the alpha bits
			var dyeColor = DyedColorComponent.getColor(stack, 0)

			// dyeColor will have alpha = 255 if it's dyed, and alpha = 0 if it's not dyed,
			if (!hasCustomDye && dyeColor != 0) {
				dyeColor = dyeColor and 0x00FFFFFF
				val colorHex = String.format("%06X", dyeColor)
				val expectedHex = ExoticTooltip.getExpectedHex(internalID)

				var correctLine = false
				for (text in lines) {
					val existingTooltip = text.string + " "
					if (existingTooltip.startsWith("Color: ")) {
						correctLine = true

						addExoticTooltip(lines, internalID, getCustomData(stack), colorHex, expectedHex, existingTooltip)
						break
					}
				}

				if (!correctLine) {
					addExoticTooltip(lines, internalID, getCustomData(stack), colorHex, expectedHex, "")
				}
			}
		}

		if (TooltipInfoType.ACCESSORIES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			val report = AccessoriesHelper.calculateReport4Accessory(internalID)

			if (report.left() != AccessoryReport.INELIGIBLE) {
				val title = Text.literal(String.format("%-19s", "Accessory: ")).withColor(0xf57542)

				val stateText: Text = when (report.left()) {
					AccessoryReport.HAS_HIGHEST_TIER -> Text.literal("✔ Collected").formatted(Formatting.GREEN)
					AccessoryReport.IS_GREATER_TIER -> Text.literal("✦ Upgrade ").withColor(0x218bff).append(Text.literal(report.right()).withColor(0xf8f8ff))
					AccessoryReport.HAS_GREATER_TIER -> Text.literal("↑ Upgradable ").withColor(0xf8d048).append(Text.literal(report.right()).withColor(0xf8f8ff))
					AccessoryReport.OWNS_BETTER_TIER -> Text.literal("↓ Downgrade ").formatted(Formatting.GRAY).append(Text.literal(report.right()).withColor(0xf8f8ff))
					AccessoryReport.MISSING -> Text.literal("✖ Missing ").formatted(Formatting.RED).append(Text.literal(report.right()).withColor(0xf8f8ff))
					else -> Text.literal("? Unknown").formatted(Formatting.GRAY)
				}

				lines.add(title.append(stateText))
			}
		}
	}

	@JvmStatic
    fun getNeuName(internalID: String, neuName: String): String {
		var neuName = neuName
		when (internalID) {
			"PET" -> {
				neuName = neuName.replace("LVL_\\d*_".toRegex(), "")
				val parts = neuName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				val type = parts[0]
				neuName = neuName.replace((type + "_").toRegex(), "")
				neuName = "$neuName-$type"
				neuName = neuName.replace("UNCOMMON", "1")
					.replace("COMMON", "0")
					.replace("RARE", "2")
					.replace("EPIC", "3")
					.replace("LEGENDARY", "4")
					.replace("MYTHIC", "5")
					.replace("-", ";")
			}

			"RUNE" -> neuName = neuName.replace("_(?!.*_)".toRegex(), ";")
			"POTION" -> neuName = ""
			"ATTRIBUTE_SHARD" -> neuName = internalID + "+" + neuName.replace("SHARD-", "").replace("_(?!.*_)".toRegex(), ";")
			else -> neuName = neuName.replace(":", "-")
		}
		return neuName
	}

	private fun addExoticTooltip(lines: List<Text>, internalID: String, customData: NbtCompound, colorHex: String, expectedHex: String?, existingTooltip: String) {
		if (expectedHex != null && !colorHex.equals(expectedHex, ignoreCase = true) && !ExoticTooltip.isException(internalID, colorHex) && !ExoticTooltip.intendedDyed(customData)) {
			val type = ExoticTooltip.checkDyeType(colorHex)
			lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + "(").append(type.translatedText).append(Formatting.DARK_GRAY.toString() + ")"))
		}
	}

	fun nullWarning() {
		if (!sentNullWarning && client.player != null) {
			LOGGER.warn(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemTooltip.nullMessage")).string)
			sentNullWarning = true
		}
	}

	// TODO What in the world is this?
    @JvmStatic
    fun getInternalNameFromNBT(stack: ItemStack?, internalIDOnly: Boolean): String? {
		val customData = getCustomData(stack!!)

		if (customData == null || !customData.contains(ItemUtils.ID, NbtElement.STRING_TYPE.toInt())) {
			return null
		}
		val internalName = customData.getString(ItemUtils.ID)

		if (internalIDOnly) {
			return internalName
		}

		// Transformation to API format.
		if (customData.contains("is_shiny")) {
			return "ISSHINY_$internalName"
		}

		when (internalName) {
			"ENCHANTED_BOOK" -> {
				if (customData.contains("enchantments")) {
					val enchants = customData.getCompound("enchantments")
					val firstEnchant = enchants.keys.stream().findFirst()
					val enchant = firstEnchant.orElse("")
					return "ENCHANTMENT_" + enchant.uppercase() + "_" + enchants.getInt(enchant)
				}
			}

			"PET" -> {
				if (customData.contains("petInfo")) {
					val petInfo = SkyblockerMod.GSON.fromJson(customData.getString("petInfo"), JsonObject::class.java)
					return "LVL_1_" + petInfo["tier"].asString + "_" + petInfo["type"].asString
				}
			}

			"POTION" -> {
				val enhanced = if (customData.contains("enhanced")) "_ENHANCED" else ""
				val extended = if (customData.contains("extended")) "_EXTENDED" else ""
				val splash = if (customData.contains("splash")) "_SPLASH" else ""
				if (customData.contains("potion") && customData.contains("potion_level")) {
					return (customData.getString("potion") + "_" + internalName + "_" + customData.getInt("potion_level")
							+ enhanced + extended + splash).uppercase()
				}
			}

			"RUNE" -> {
				if (customData.contains("runes")) {
					val runes = customData.getCompound("runes")
					val firstRunes = runes.keys.stream().findFirst()
					val rune = firstRunes.orElse("")
					return rune.uppercase() + "_RUNE_" + runes.getInt(rune)
				}
			}

			"ATTRIBUTE_SHARD" -> {
				if (customData.contains("attributes")) {
					val shards = customData.getCompound("attributes")
					val firstShards = shards.keys.stream().findFirst()
					val shard = firstShards.orElse("")
					return internalName + "-" + shard.uppercase() + "_" + shards.getInt(shard)
				}
			}
		}
		return internalName
	}

	private fun getCoinsMessage(price: Double, count: Int): Text {
		// Format the price string once
		val priceString = String.format(Locale.ENGLISH, "%1$,.1f", price)

		// If count is 1, return a simple message
		if (count == 1) {
			return Text.literal("$priceString Coins").formatted(Formatting.DARK_AQUA)
		}

		// If count is greater than 1, include the "each" information
		val priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", price * count)
		val message = Text.literal("$priceStringTotal Coins ").formatted(Formatting.DARK_AQUA)
		message.append(Text.literal("($priceString each)").formatted(Formatting.GRAY))

		return message
	}

	private fun getMotesMessage(price: Int, count: Int): Text {
		val motesMultiplier = SkyblockerConfigManager.config.otherLocations.rift.mcGrubberStacks * 0.05f + 1

		// Calculate the total price
		val totalPrice = price * count
		val totalPriceString = String.format(Locale.ENGLISH, "%1$,.1f", totalPrice * motesMultiplier)

		// If count is 1, return a simple message
		if (count == 1) {
			return Text.literal(totalPriceString.replace(".0", "") + " Motes").formatted(Formatting.DARK_AQUA)
		}

		// If count is greater than 1, include the "each" information
		val eachPriceString = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier)
		val message = Text.literal(totalPriceString.replace(".0", "") + " Motes ").formatted(Formatting.DARK_AQUA)
		message.append(Text.literal("(" + eachPriceString.replace(".0", "") + " each)").formatted(Formatting.GRAY))

		return message
	}

	//This is static to not create a new text object for each line in every item
	private val BUMPY_LINE: Text = Text.literal("-----------------").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH)

	private fun smoothenLines(lines: MutableList<Text>) {
		for (i in lines.indices) {
			val lineSiblings = lines[i].siblings
			//Compare the first sibling rather than the whole object as the style of the root object can change while visually staying the same
			if (lineSiblings.size == 1 && lineSiblings.first == BUMPY_LINE) {
				lines[i] = createSmoothLine()
			}
		}
	}

	@JvmStatic
    fun createSmoothLine(): Text {
		return Text.literal("                    ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH, Formatting.BOLD)
	}

	// If these options is true beforehand, the client will get first data of these options while loading.
	// After then, it will only fetch the data if it is on Skyblock.
	var minute: Int = 0

	fun init() {
		Scheduler.INSTANCE.scheduleCyclic({
			if (!isOnSkyblock && 0 < minute) {
				sentNullWarning = false
				return@scheduleCyclic
			}
			if (++minute % 60 == 0) {
				sentNullWarning = false
			}

			val futureList: List<CompletableFuture<Void?>?> = ArrayList()

			TooltipInfoType.NPC.downloadIfEnabled(futureList)
			TooltipInfoType.BAZAAR.downloadIfEnabled(futureList)
			TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList)

			if (config.enableAvgBIN) {
				val type = config.avg

				if (type == GeneralConfig.Average.BOTH || TooltipInfoType.ONE_DAY_AVERAGE.data == null || TooltipInfoType.THREE_DAY_AVERAGE.data == null || minute % 5 == 0) {
					TooltipInfoType.ONE_DAY_AVERAGE.download(futureList)
					TooltipInfoType.THREE_DAY_AVERAGE.download(futureList)
				} else if (type == GeneralConfig.Average.ONE_DAY) {
					TooltipInfoType.ONE_DAY_AVERAGE.download(futureList)
				} else if (type == GeneralConfig.Average.THREE_DAY) {
					TooltipInfoType.THREE_DAY_AVERAGE.download(futureList)
				}
			}

			TooltipInfoType.MOTES.downloadIfEnabled(futureList)
			TooltipInfoType.MUSEUM.downloadIfEnabled(futureList)
			TooltipInfoType.COLOR.downloadIfEnabled(futureList)
			TooltipInfoType.ACCESSORIES.downloadIfEnabled(futureList)
			CompletableFuture.allOf(*futureList.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).exceptionally { e: Throwable? ->
				LOGGER.error("Encountered unknown error while downloading tooltip data", e)
				null
			}
		}, 1200, true)
	}
}