package de.hysky.skyblocker.compatibility.rei

import com.google.common.collect.Lists
import de.hysky.skyblocker.utils.ItemUtils.skyblockerStack
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.text.Text

/**
 * Skyblock recipe category class for REI
 */
class SkyblockCategory : DisplayCategory<SkyblockCraftingDisplay> {
	override fun getCategoryIdentifier(): CategoryIdentifier<SkyblockCraftingDisplay> {
		return SkyblockerREIClientPlugin.Companion.SKYBLOCK
	}

	override fun getTitle(): Text {
		return Text.translatable("emi.category.skyblocker.skyblock")
	}

	override fun getIcon(): Renderer {
		return EntryStacks.of(skyblockerStack)
	}

	override fun getDisplayHeight(): Int {
		return 73
	}

	/**
	 * Draws display for SkyblockCraftingDisplay
	 *
	 * @param display the display
	 * @param bounds  the bounds of the display, configurable with overriding the width, height methods.
	 */
	override fun setupDisplay(display: SkyblockCraftingDisplay, bounds: Rectangle): List<Widget> {
		val out: MutableList<Widget> = ArrayList()
		out.add(Widgets.createRecipeBase(bounds))
		val startPoint = if (!display.craftText.isEmpty() && display.craftText != null) {
			Point(bounds.centerX - 58, bounds.centerY - 31)
		} else {
			Point(bounds.centerX - 58, bounds.centerY - 26)
		}
		val resultPoint = Point(startPoint.x + 95, startPoint.y + 19)
		out.add(Widgets.createArrow(Point(startPoint.x + 60, startPoint.y + 18)))
		out.add(Widgets.createResultSlotBackground(resultPoint))

		// Generate Slots
		val input = display.inputEntries
		val slots: MutableList<Slot> = Lists.newArrayList()
		for (y in 0..2) for (x in 0..2) slots.add(Widgets.createSlot(Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput())
		for (i in input.indices) {
			slots[i].entries(input[i]).markInput()
		}
		out.addAll(slots)
		out.add(Widgets.createSlot(resultPoint).entries(display.outputEntries.first).disableBackground().markOutput())

		// Add craftingText Label
		val craftTextLabel = Widgets.createLabel(Point(bounds.centerX, startPoint.y + 55), Text.of(display.craftText))
		out.add(craftTextLabel)
		return out
	}
}
