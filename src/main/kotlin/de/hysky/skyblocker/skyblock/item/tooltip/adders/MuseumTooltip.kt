package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.MuseumItemCache.hasItemInMuseum
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class MuseumTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		val itemStack = focusedSlot.stack
		val internalID: String = itemStack.getSkyblockId()
		if (TooltipInfoType.MUSEUM.isTooltipEnabledAndHasOrNullWarning(internalID)) {
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
				val customData = getCustomData(itemStack)
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
	}
}
