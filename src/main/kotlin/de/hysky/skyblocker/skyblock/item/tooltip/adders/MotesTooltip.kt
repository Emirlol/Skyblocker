package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

class MotesTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		val itemStack = focusedSlot.stack
		val internalID: String = itemStack.getSkyblockId()
		if (internalID != null && TooltipInfoType.MOTES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(
				Text.literal(String.format("%-20s", "Motes Price:"))
					.formatted(Formatting.LIGHT_PURPLE)
					.append(getMotesMessage(TooltipInfoType.MOTES.data!![internalID].asInt, itemStack.count))
			)
		}
	}

	companion object {
		private fun getMotesMessage(price: Int, count: Int): Text {
			val motesMultiplier: Float = SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks * 0.05f + 1

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
	}
}
