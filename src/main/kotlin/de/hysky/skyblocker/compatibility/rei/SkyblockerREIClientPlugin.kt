package de.hysky.skyblocker.compatibility.rei

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.item.Items

/**
 * REI integration
 */
class SkyblockerREIClientPlugin : REIClientPlugin {
	override fun registerCategories(categoryRegistry: CategoryRegistry) {
		categoryRegistry.addWorkstations(SKYBLOCK, EntryStacks.of(Items.CRAFTING_TABLE))
		categoryRegistry.add(SkyblockCategory())
	}

	override fun registerDisplays(displayRegistry: DisplayRegistry) {
		displayRegistry.registerDisplayGenerator(SKYBLOCK, SkyblockCraftingDisplayGenerator())
	}

	override fun registerEntries(entryRegistry: EntryRegistry) {
		entryRegistry.addEntries(ItemRepository.items.asSequence().map(EntryStacks::of).toList())
	}

	companion object {
		val SKYBLOCK: CategoryIdentifier<SkyblockCraftingDisplay> = CategoryIdentifier.of(SkyblockerMod.NAMESPACE, "skyblock")
	}
}
