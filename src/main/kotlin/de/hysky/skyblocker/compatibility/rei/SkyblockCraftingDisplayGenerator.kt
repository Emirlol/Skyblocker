package de.hysky.skyblocker.compatibility.rei

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.item.ItemStack
import java.util.*

class SkyblockCraftingDisplayGenerator : DynamicDisplayGenerator<SkyblockCraftingDisplay> {
	override fun getRecipeFor(entry: EntryStack<*>): Optional<List<SkyblockCraftingDisplay>> {
		if (entry.value !is ItemStack) return Optional.empty()
		val inputItem = EntryStacks.of(entry.value as ItemStack)
		val filteredRecipes = ItemRepository.recipes.asSequence().filter { recipe: SkyblockCraftingRecipe ->
			val itemStack = inputItem.value
			val itemStack1 = recipe.result
			getItemId(itemStack1!!) == getItemId(itemStack)
		}.toList()

		return Optional.of(generateDisplays(filteredRecipes))
	}

	override fun getUsageFor(entry: EntryStack<*>): Optional<List<SkyblockCraftingDisplay>> {
		if (entry.value !is ItemStack) return Optional.empty()
		val inputItem = EntryStacks.of(entry.value as ItemStack)
		val filteredRecipes = ItemRepository.recipes.asSequence().filter { recipe ->
			for (item in recipe.grid) {
				if (getItemId(item).isNotEmpty()) {
					val itemStack = inputItem.value
					if (getItemId(item) == getItemId(itemStack)) return@filter true
				}
			}
			false
		}.toList()
		return Optional.of(generateDisplays(filteredRecipes))
	}

	/**
	 * Generate Displays from a list of recipes
	 */
	private fun generateDisplays(recipes: List<SkyblockCraftingRecipe>): List<SkyblockCraftingDisplay> {
		val displays = arrayListOf<SkyblockCraftingDisplay>()
		for (recipe in recipes) {
			val inputs = arrayListOf<EntryIngredient>()
			val outputs = arrayListOf<EntryIngredient>()

			val inputEntryStacks = arrayListOf<EntryStack<ItemStack>>()
			recipe.grid.forEach { inputEntryStacks.add(EntryStacks.of(it)) }

			for (entryStack in inputEntryStacks) {
				inputs += EntryIngredient.of(entryStack)
			}
			outputs += EntryIngredient.of(EntryStacks.of(recipe.result))

			displays += SkyblockCraftingDisplay(inputs, outputs, recipe.craftText)
		}
		return displays
	}
}
