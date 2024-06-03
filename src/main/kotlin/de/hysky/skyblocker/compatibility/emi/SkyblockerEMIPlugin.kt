package de.hysky.skyblocker.compatibility.emi

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.utils.ItemUtils.skyblockerStack
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier

/**
 * EMI integration
 */
class SkyblockerEMIPlugin : EmiPlugin {
	override fun register(registry: EmiRegistry) {
		ItemRepository.items.asSequence().map { EmiStack.of(it) }.forEach {
			registry.addEmiStack(it)
			registry.setDefaultComparison(it, Comparison.compareNbt())
		}
		registry.addCategory(SKYBLOCK)
		registry.addWorkstation(SKYBLOCK, EmiStack.of(Items.CRAFTING_TABLE))
		ItemRepository.recipes.asSequence().map { SkyblockEmiRecipe(it) }.forEach(registry::addRecipe)
	}

	companion object {
		private val SIMPLIFIED_TEXTURES = Identifier("emi", "textures/gui/widgets.png")

		// TODO: Custom simplified texture for Skyblock
		val SKYBLOCK = EmiRecipeCategory(Identifier(SkyblockerMod.NAMESPACE, "skyblock"), EmiStack.of(skyblockerStack), EmiTexture(SIMPLIFIED_TEXTURES, 240, 240, 16, 16))
	}
}
