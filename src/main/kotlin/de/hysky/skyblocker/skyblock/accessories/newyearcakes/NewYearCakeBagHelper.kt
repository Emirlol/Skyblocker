package de.hysky.skyblocker.skyblock.accessories.newyearcakes

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.minecraft.screen.slot.Slot

object NewYearCakeBagHelper : ContainerSolver("New Year Cake Bag") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.config.helpers.enableNewYearCakesHelper

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		slots.forEach { NewYearCakesHelper.addCake(NewYearCakesHelper.getCakeYear(it.stack)) }
		returnemptyList()
	}
}
