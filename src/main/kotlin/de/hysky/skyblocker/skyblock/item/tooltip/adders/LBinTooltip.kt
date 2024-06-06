package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class LBinTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		lbinExist = false
		val itemStack = focusedSlot.stack
		val internalID: String = itemStack.getSkyblockId() ?: return
		var name: String = itemStack.getSkyblockName() ?: return

		if (name.startsWith("ISSHINY_")) name = "SHINY_$internalID"

		// bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
		if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !BazaarPriceTooltip.Companion.bazaarExist) {
			lines.add(
				Text.literal(String.format("%-19s", "Lowest BIN Price:"))
					.formatted(Formatting.GOLD)
					.append(ItemTooltip.getCoinsMessage(TooltipInfoType.LOWEST_BINS.data!![name].asDouble, itemStack.count))
			)
			lbinExist = true
		}
	}

	companion object {
		var lbinExist: Boolean = false
	}
}
