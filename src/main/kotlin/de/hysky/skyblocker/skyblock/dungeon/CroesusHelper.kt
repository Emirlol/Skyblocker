package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getLoreLineIf
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.gray
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

class CroesusHelper : ContainerSolver("^Croesus$") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().dungeons.croesusHelper

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		for (entry in slots!!.int2ObjectEntrySet()) {
			val stack = entry.value
			if (stack != null && stack.contains(DataComponentTypes.LORE)) {
				if (getLoreLineIf(stack) { s: String -> s.contains("Opened Chest:") } != null) {
					highlights.add(gray(entry.intKey))
				} else if (getLoreLineIf(stack) { s: String -> s.contains("No more Chests to open!") } != null) {
					highlights.add(red(entry.intKey))
				}
			}
		}
		return highlights
	}
}
