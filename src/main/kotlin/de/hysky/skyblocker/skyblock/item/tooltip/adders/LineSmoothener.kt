package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class LineSmoothener : TooltipAdder(Int.MIN_VALUE) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		for (i in lines.indices) {
			val lineSiblings = lines[i].siblings
			//Compare the first sibling rather than the whole object as the style of the root object can change while visually staying the same
			if (lineSiblings.size == 1 && lineSiblings.first == BUMPY_LINE) {
				lines[i] = createSmoothLine()
			}
		}
	}

	companion object {
		//This is static to not create a new text object for each line in every item
		private val BUMPY_LINE: Text = Text.literal("-----------------").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH)

		@JvmStatic
		fun createSmoothLine(): Text {
			return Text.literal("                    ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH, Formatting.BOLD)
		}
	}
}
