package de.hysky.skyblocker.compatibility.rei

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository.recipesStream
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.item.ItemStack
import java.util.*
import java.util.function.Consumer

class SkyblockCraftingDisplayGenerator : DynamicDisplayGenerator<SkyblockCraftingDisplay> {
	override fun getRecipeFor(entry: EntryStack<*>): Optional<List<SkyblockCraftingDisplay>> {
		if (entry.value !is ItemStack) return Optional.empty()
		val inputItem = EntryStacks.of(entry.value as ItemStack)
		val filteredRecipes = recipesStream
			.filter { recipe: SkyblockCraftingRecipe ->
				val itemStack = inputItem.value
				val itemStack1 = recipe.result
				getItemId(itemStack1!!) == getItemId(itemStack)
			}
			.toList()

		return Optional.of(generateDisplays(filteredRecipes))
	}

	override fun getUsageFor(entry: EntryStack<*>): Optional<List<SkyblockCraftingDisplay>> {
		if (entry.value !is ItemStack) return Optional.empty()
		val inputItem = EntryStacks.of(entry.value as ItemStack)
		val filteredRecipes = recipesStream
			.filter { recipe: SkyblockCraftingRecipe ->
				for (item in recipe.getGrid()) {
					if (!getItemId(item!!).isEmpty()) {
						val itemStack = inputItem.value
						if (getItemId(item) == getItemId(itemStack)) return@filter true
					}
				}
				false
			}
			.toList()
		return Optional.of(generateDisplays(filteredRecipes))
	}

	/**
	 * Generate Displays from a list of recipes
	 */
	private fun generateDisplays(recipes: List<SkyblockCraftingRecipe>): List<SkyblockCraftingDisplay> {
		val displays: MutableList<SkyblockCraftingDisplay> = ArrayList()
		for (recipe in recipes) {
			val inputs: MutableList<EntryIngredient> = ArrayList()
			val outputs: MutableList<EntryIngredient> = ArrayList()

			val inputEntryStacks = ArrayList<EntryStack<ItemStack>>()
			recipe.getGrid().forEach(Consumer { item: ItemStack? -> inputEntryStacks.add(EntryStacks.of(item)) })

			for (entryStack in inputEntryStacks) {
				inputs.add(EntryIngredient.of(entryStack))
			}
			outputs.add(EntryIngredient.of(EntryStacks.of(recipe.result)))

			displays.add(SkyblockCraftingDisplay(inputs, outputs, recipe.craftText))
		}
		return displays
	}
}
