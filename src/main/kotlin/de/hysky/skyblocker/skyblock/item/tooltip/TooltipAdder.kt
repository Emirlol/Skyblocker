package de.hysky.skyblocker.skyblock.item.tooltip

import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import org.intellij.lang.annotations.Language

/**
 * Extend this class and add it to [TooltipManager.adders] to add additional text to tooltips.
 */
abstract class TooltipAdder(val titleRegex: Regex?, val priority: Int) {
	protected constructor(@Language("RegExp") titlePattern: String, priority: Int) : this(Regex(titlePattern), priority)

	/**
	 * Creates a TooltipAdder that will be applied to all screens.
	 */
	protected constructor(priority: Int) : this(null, priority)

	/**
	 * @implNote The first element of the lines list holds the item's display name,
	 * as it's a list of all lines that will be displayed in the tooltip.
	 */
	abstract fun addToTooltip(lines: List<Text>, focusedSlot: Slot)
}
