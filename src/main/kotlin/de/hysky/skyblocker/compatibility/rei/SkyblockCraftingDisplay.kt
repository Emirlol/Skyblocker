package de.hysky.skyblocker.compatibility.rei

import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient


/**
 * Skyblock Crafting Recipe display class for REI
 */
class SkyblockCraftingDisplay(input: List<EntryIngredient>, output: List<EntryIngredient>, val craftText: String?) : BasicDisplay(input, output), SimpleGridMenuDisplay {
	override fun getWidth() = 3

	override fun getHeight() = 3

	override fun getCategoryIdentifier() = SkyblockerREIClientPlugin.SKYBLOCK
}