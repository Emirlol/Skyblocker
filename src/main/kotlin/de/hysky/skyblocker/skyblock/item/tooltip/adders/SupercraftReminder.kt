package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

class SupercraftReminder : TooltipAdder(Pattern.compile("^.+ Recipe$"), Int.MIN_VALUE) {
	override fun addToTooltip(lines: List<Text>, focusedSlot: Slot) {
		if (focusedSlot.id != SUPERCRAFT_SLOT.toInt() || !focusedSlot.stack.isOf(Items.GOLDEN_PICKAXE)) return
		val uuid = getItemUuid(focusedSlot.inventory.getStack(RECIPE_RESULT_SLOT.toInt()))
		if (!uuid!!.isEmpty()) return  //Items with UUID can't be stacked, and therefore the shift-click feature doesn't matter

		var index = lines.size - 1
		if (lines[lines.size - 2].string == "Recipe not unlocked!") index-- //Place it right below the "Right-Click to set amount" line

		lines.add(index, Text.literal("Shift-Click to maximize the amount!").formatted(Formatting.GOLD))
	}

	companion object {
		private const val SUPERCRAFT_SLOT: Byte = 32
		private const val RECIPE_RESULT_SLOT: Byte = 25
	}
}
