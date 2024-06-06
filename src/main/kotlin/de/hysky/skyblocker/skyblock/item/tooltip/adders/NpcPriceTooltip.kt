package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class NpcPriceTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		val stack = focusedSlot.stack
		val internalID: String = stack.getSkyblockId()
		if (internalID != null && TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(
				Text.literal(String.format("%-21s", "NPC Sell Price:"))
					.formatted(Formatting.YELLOW)
					.append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.data!![internalID].asDouble, stack.count))
			)
		}
	}
}
