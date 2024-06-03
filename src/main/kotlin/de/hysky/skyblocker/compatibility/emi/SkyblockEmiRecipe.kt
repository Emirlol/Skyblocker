package de.hysky.skyblocker.compatibility.emi

import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import dev.emi.emi.api.recipe.EmiCraftingRecipe
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

class SkyblockEmiRecipe(recipe: SkyblockCraftingRecipe) : EmiCraftingRecipe(recipe.grid.asSequence().map { stack -> EmiStack.of(stack) }.map { it as EmiIngredient }.toList(), EmiStack.of(recipe.result), Identifier.of("skyblock", getItemId(recipe.result!!).lowercase(Locale.getDefault()).replace(';', '_') + "_" + recipe.result!!.count)) {
	private val craftText = recipe.craftText

	override fun getCategory() = SkyblockerEMIPlugin.SKYBLOCK

	override fun getDisplayHeight() = super.getDisplayHeight() + (if (craftText!!.isEmpty()) 0 else 10)

	override fun addWidgets(widgets: WidgetHolder) {
		super.addWidgets(widgets)
		widgets.addText(Text.of(craftText), 59 - MinecraftClient.getInstance().textRenderer.getWidth(craftText) / 2, 55, 0xFFFFFF, true)
	}
}
