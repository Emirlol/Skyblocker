package de.hysky.skyblocker.skyblock.accessories.newyearcakes

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

class NewYearCakeBagHelper : ContainerSolver("New Year Cake Bag") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().helpers.enableNewYearCakesHelper

	override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		val client = MinecraftClient.getInstance()
		if (client.player != null) {
			for (slot in client.player!!.currentScreenHandler.slots) {
				NewYearCakesHelper.INSTANCE.addCake(NewYearCakesHelper.getCakeYear(slot.stack))
			}
		}
		return listOf<ColorHighlight>()
	}
}
