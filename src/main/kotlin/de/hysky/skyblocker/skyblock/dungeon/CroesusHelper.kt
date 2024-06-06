package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getLoreLineIf
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.gray
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.minecraft.component.DataComponentTypes
import net.minecraft.screen.slot.Slot

object CroesusHelper : ContainerSolver("^Croesus$") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.config.dungeons.croesusHelper

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		val highlights: MutableList<ColorHighlight> = ArrayList()
		for (slot in slots) {
			val stack = slot.stack
			if (!stack.isEmpty && stack.contains(DataComponentTypes.LORE)) {
				if (getLoreLineIf(stack) { it.contains("Opened Chest:") } != null) {
					highlights += gray(slot.id)
				} else if (getLoreLineIf(stack) { it.contains("No more Chests to open!") } != null) {
					highlights += red(slot.id)
				}
			}
		}
		return highlights
	}
}
