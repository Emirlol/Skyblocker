package de.hysky.skyblocker.skyblock.experiment

import de.hysky.skyblocker.config.configs.HelperConfig.Experiments
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class UltrasequencerSolver private constructor() : ExperimentSolver("^Ultrasequencer \\(\\w+\\)$") {
	@JvmField
    var ultrasequencerNextSlot: Int = 0

	override fun isEnabled(experimentsConfig: Experiments): Boolean {
		return experimentsConfig.enableUltrasequencerSolver
	}

	override fun tick(screen: Screen) {
		if (isEnabled && screen is GenericContainerScreen && screen.getTitle().string.startsWith("Ultrasequencer (")) {
			when (state) {
				State.REMEMBER -> {
					val inventory: Inventory = screen.screenHandler.inventory
					if (inventory.getStack(49).name.string == "Remember the pattern!") {
						for (index in 9..44) {
							val itemStack = inventory.getStack(index)
							val name = itemStack.name.string
							if (name.matches("\\d+".toRegex())) {
								if (name == "1") {
									ultrasequencerNextSlot = index
								}
								slots[index] = itemStack
							}
						}
						state = State.WAIT
					}
				}

				State.WAIT -> {
					if (screen.screenHandler.inventory.getStack(49).name.string.startsWith("Timer: ")) {
						state = State.SHOW
						markHighlightsDirty()
					}
				}

				State.END -> {
					val name: String = screen.screenHandler.inventory.getStack(49).name.string
					if (!name.startsWith("Timer: ")) {
						if (name == "Remember the pattern!") {
							slots.clear()
							state = State.REMEMBER
						} else {
							reset()
						}
					}
				}
			}
		} else {
			reset()
		}
	}

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		return if (state == State.SHOW && ultrasequencerNextSlot != 0) java.util.List.of(green(ultrasequencerNextSlot)) else ArrayList()
	}

	companion object {
		@JvmField
        val INSTANCE: UltrasequencerSolver = UltrasequencerSolver()
	}
}
