package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper.AccessoryReport
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class AccessoryTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		val internalID: String = focusedSlot.stack.getSkyblockId()
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
}
