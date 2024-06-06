package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.GeneralConfig
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class AvgBinTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		val itemStack = focusedSlot.stack
		val neuName: String = itemStack.getNeuName()
		val internalID: String = itemStack.getSkyblockId()
		if (neuName == null || internalID == null) return

		if (SkyblockerConfigManager.get().general.itemTooltip.enableAvgBIN) {
			if (TooltipInfoType.ONE_DAY_AVERAGE.data == null || TooltipInfoType.THREE_DAY_AVERAGE.data == null) {
				ItemTooltip.nullWarning()
			} else {
				/*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
				if (!neuName.isEmpty() && LBinTooltip.Companion.lbinExist) {
					val type = ItemTooltip.config.avg

					// "No data" line because of API not keeping old data, it causes NullPointerException
					if (type == GeneralConfig.Average.ONE_DAY || type == GeneralConfig.Average.BOTH) {
						lines.add(
							Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
								.formatted(Formatting.GOLD)
								.append(
									if (TooltipInfoType.ONE_DAY_AVERAGE.data!![neuName] == null
									) Text.literal("No data").formatted(Formatting.RED)
									else ItemTooltip.getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.data!![neuName].asDouble, itemStack.count)
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
									else ItemTooltip.getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.data!![neuName].asDouble, itemStack.count)
								)
						)
					}
				}
			}
		}
	}
}
