package de.hysky.skyblocker.compatibility.emi

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository.itemsStream
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository.recipesStream
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe
import de.hysky.skyblocker.utils.ItemUtils.skyblockerStack
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import java.util.function.Function

/**
 * EMI integration
 */
class SkyblockerEMIPlugin : EmiPlugin {
	override fun register(registry: EmiRegistry) {
		itemsStream.map<EmiStack>(Function { stack: ItemStack? -> EmiStack.of(stack) }).forEach { emiStack: EmiStack? ->
			registry.addEmiStack(emiStack)
			registry.setDefaultComparison(emiStack, Comparison.compareNbt())
		}
		registry.addCategory(SKYBLOCK)
		registry.addWorkstation(SKYBLOCK, EmiStack.of(Items.CRAFTING_TABLE))
		recipesStream.map { recipe: SkyblockCraftingRecipe -> SkyblockEmiRecipe(recipe) }.forEach { recipe: SkyblockEmiRecipe? -> registry.addRecipe(recipe) }
	}

	companion object {
		val SIMPLIFIED_TEXTURES: Identifier = Identifier("emi", "textures/gui/widgets.png")

		// TODO: Custom simplified texture for Skyblock
		val SKYBLOCK: EmiRecipeCategory = EmiRecipeCategory(Identifier(SkyblockerMod.NAMESPACE, "skyblock"), EmiStack.of(skyblockerStack), EmiTexture(SIMPLIFIED_TEXTURES, 240, 240, 16, 16))
	}
}
