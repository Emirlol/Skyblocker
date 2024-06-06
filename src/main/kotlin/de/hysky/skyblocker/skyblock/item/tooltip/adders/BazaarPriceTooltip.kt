package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.lang3.math.NumberUtils

class BazaarPriceTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		bazaarExist = false
		val itemStack = focusedSlot.stack
		val internalID: String = itemStack.getSkyblockId() ?: return
		var name: String = itemStack.getSkyblockName() ?: return

		if (name.startsWith("ISSHINY_")) name = "SHINY_$internalID"

		if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name)) {
			val amount: Int
			if (lines[1].string.endsWith("Sack")) {
				//The amount is in the 2nd sibling of the 3rd line of the lore.                                              here V
				//Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
				val line = lines[3].siblings[1].string.replace(",", "")
				amount = if (NumberUtils.isParsable(line) && line != "0") line.toInt() else itemStack.count
			} else {
				amount = itemStack.count
			}
			val getItem = TooltipInfoType.BAZAAR.data!!.getAsJsonObject(name)
			lines.add(
				Text.literal(String.format("%-18s", "Bazaar buy Price:"))
					.formatted(Formatting.GOLD)
					.append(
						if (getItem["buyPrice"].isJsonNull
						) Text.literal("No data").formatted(Formatting.RED)
						else ItemTooltip.getCoinsMessage(getItem["buyPrice"].asDouble, amount)
					)
			)
			lines.add(
				Text.literal(String.format("%-19s", "Bazaar sell Price:"))
					.formatted(Formatting.GOLD)
					.append(
						if (getItem["sellPrice"].isJsonNull
						) Text.literal("No data").formatted(Formatting.RED)
						else ItemTooltip.getCoinsMessage(getItem["sellPrice"].asDouble, amount)
					)
			)
			bazaarExist = true
		}
	}

	companion object {
		var bazaarExist: Boolean = false
	}
}
