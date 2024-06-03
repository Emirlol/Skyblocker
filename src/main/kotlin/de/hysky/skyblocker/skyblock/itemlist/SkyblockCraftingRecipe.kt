package de.hysky.skyblocker.skyblock.itemlist

import de.hysky.skyblocker.utils.TextHandler
import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUIngredient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class SkyblockCraftingRecipe(val craftText: String?) {
	val grid: MutableList<ItemStack> = ArrayList(9)
	var result: ItemStack? = null
		private set

	companion object {
		fun fromNEURecipe(neuCraftingRecipe: NEUCraftingRecipe): SkyblockCraftingRecipe {
			val recipe = SkyblockCraftingRecipe(if (neuCraftingRecipe.extraText != null) neuCraftingRecipe.extraText else "")
			for (input in neuCraftingRecipe.inputs) {
				recipe.grid.add(getItemStack(input))
			}
			recipe.result = getItemStack(neuCraftingRecipe.output)
			return recipe
		}

		private fun getItemStack(input: NEUIngredient): ItemStack {
			if (input != NEUIngredient.SENTINEL_EMPTY) {
				val stack = ItemRepository.getItemStack(input.itemId)
				if (stack != null) {
					return stack.copyWithCount(input.amount.toInt())
				} else {
					TextHandler.warn("[Recipe] Unable to find item ${input.itemId}")
				}
			}
			return Items.AIR.defaultStack
		}
	}
}
