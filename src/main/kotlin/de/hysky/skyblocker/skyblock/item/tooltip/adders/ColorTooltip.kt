package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.Slot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.StringIdentifiable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ColorTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: List<Text>, focusedSlot: Slot) {
		val itemStack = focusedSlot.stack
		val internalID: String = itemStack.getSkyblockId()
		if (TooltipInfoType.COLOR.isTooltipEnabledAndHasOrNullWarning(internalID) && itemStack.contains(DataComponentTypes.DYED_COLOR)) {
			val uuid = getItemUuid(itemStack)
			val hasCustomDye = SkyblockerConfigManager.get().general.customDyeColors.containsKey(uuid) || SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(uuid)
			//DyedColorComponent#getColor returns ARGB so we mask out the alpha bits
			var dyeColor = DyedColorComponent.getColor(itemStack, 0)

			// dyeColor will have alpha = 255 if it's dyed, and alpha = 0 if it's not dyed,
			if (!hasCustomDye && dyeColor != 0) {
				dyeColor = dyeColor and 0x00FFFFFF
				val colorHex = String.format("%06X", dyeColor)
				val expectedHex = getExpectedHex(internalID)

				var correctLine = false
				for (text in lines) {
					val existingTooltip = text.string + " "
					if (existingTooltip.startsWith("Color: ")) {
						correctLine = true

						addExoticTooltip(lines, internalID, getCustomData(itemStack), colorHex, expectedHex, existingTooltip)
						break
					}
				}

				if (!correctLine) {
					addExoticTooltip(lines, internalID, getCustomData(itemStack), colorHex, expectedHex, "")
				}
			}
		}
	}

	enum class DyeType(override val name: String, private val formatting: Formatting) : StringIdentifiable {
		CRYSTAL("crystal", Formatting.AQUA),
		FAIRY("fairy", Formatting.LIGHT_PURPLE),
		OG_FAIRY("og_fairy", Formatting.DARK_PURPLE),
		SPOOK("spook", Formatting.RED),
		GLITCHED("glitched", Formatting.BLUE),
		EXOTIC("exotic", Formatting.GOLD);

		override fun asString(): String {
			return name
		}

		val translatedText: MutableText
			get() = Text.translatable("skyblocker.exotic.$name").formatted(formatting)
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(ColorTooltip::class.java)

		private fun addExoticTooltip(lines: List<Text>, internalID: String, customData: NbtCompound, colorHex: String, expectedHex: String?, existingTooltip: String) {
			if (expectedHex != null && !colorHex.equals(expectedHex, ignoreCase = true) && !isException(internalID, colorHex) && !intendedDyed(customData)) {
				val type = checkDyeType(colorHex)
				lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + "(").append(type.translatedText).append(Formatting.DARK_GRAY.toString() + ")"))
			}
		}

		fun getExpectedHex(id: String?): String? {
			val color = TooltipInfoType.COLOR.data!![id].asString
			if (color != null) {
				val RGBValues = color.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				return String.format("%02X%02X%02X", RGBValues[0].toInt(), RGBValues[1].toInt(), RGBValues[2].toInt())
			} else {
				LOGGER.warn("[Skyblocker Exotics] No expected color data found for id {}", id)
				return null
			}
		}

		fun isException(id: String?, hex: String?): Boolean {
			return when (id) {
				-> true
				-> Constants.RANCHERS.contains(hex)
				-> Constants.ADAPTIVE_CHEST.contains(hex)
				-> Constants.ADAPTIVE.contains(hex)
				-> Constants.REAPER.contains(hex)
				-> Constants.FAIRY_HEXES.contains(hex)
				-> Constants.CRYSTAL_HEXES.contains(hex)
				-> Constants.SPOOK.contains(hex)
				else -> false
			}
		}

		fun checkDyeType(hex: String?): DyeType {
			return when (hex) {
				-> DyeType.CRYSTAL
				-> DyeType.FAIRY
				-> DyeType.OG_FAIRY
				-> DyeType.SPOOK
				-> DyeType.GLITCHED
				else -> DyeType.EXOTIC
			}
		}

		fun intendedDyed(customData: NbtCompound): Boolean {
			return customData.contains("dye_item")
		}
	}
}
