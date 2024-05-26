package de.hysky.skyblocker.skyblock.experiment

import com.google.common.collect.ImmutableMap
import de.hysky.skyblocker.config.configs.HelperConfig.Experiments
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.AbstractMap.SimpleImmutableEntry

class ChronomatronSolver : ExperimentSolver("^Chronomatron \\(\\w+\\)$") {
	private val chronomatronSlots: MutableList<Item?> = ArrayList()
	private var chronomatronChainLengthCount = 0
	private var chronomatronCurrentSlot = 0
	var chronomatronCurrentOrdinal: Int = 0
		private set

	fun getChronomatronSlots(): List<Item?> {
		return chronomatronSlots
	}

	fun incrementChronomatronCurrentOrdinal(): Int {
		return ++chronomatronCurrentOrdinal
	}

	override fun isEnabled(experimentsConfig: Experiments): Boolean {
		return experimentsConfig.enableChronomatronSolver
	}

	override fun tick(screen: Screen) {
		if (isEnabled && screen is GenericContainerScreen && screen.getTitle().string.startsWith("Chronomatron (")) {
			when (state) {
				State.REMEMBER -> {
					val inventory: Inventory = screen.screenHandler.inventory
					if (chronomatronCurrentSlot == 0) {
						for (index in 10..42) {
							if (inventory.getStack(index).hasGlint()) {
								if (chronomatronSlots.size <= chronomatronChainLengthCount) {
									chronomatronSlots.add(TERRACOTTA_TO_GLASS[inventory.getStack(index).item])
									state = State.WAIT
								} else {
									chronomatronChainLengthCount++
								}
								chronomatronCurrentSlot = index
								return
							}
						}
					} else if (!inventory.getStack(chronomatronCurrentSlot).hasGlint()) {
						chronomatronCurrentSlot = 0
					}
				}

				State.WAIT -> {
					if (screen.screenHandler.inventory.getStack(49).name.string.startsWith("Timer: ")) {
						state = State.SHOW
					}
				}

				State.END -> {
					val name: String = screen.screenHandler.inventory.getStack(49).name.string
					if (!name.startsWith("Timer: ")) {
						if (name == "Remember the pattern!") {
							chronomatronChainLengthCount = 0
							chronomatronCurrentOrdinal = 0
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
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		if (state == State.SHOW && chronomatronSlots.size > chronomatronCurrentOrdinal) {
			for (indexStack in slots!!.int2ObjectEntrySet()) {
				val index = indexStack.intKey
				val stack = indexStack.value
				val item = chronomatronSlots[chronomatronCurrentOrdinal]
				if (stack!!.isOf(item) || TERRACOTTA_TO_GLASS[stack.item] === item) {
					highlights.add(green(index))
				}
			}
		}
		return highlights
	}

	override fun reset() {
		super.reset()
		chronomatronSlots.clear()
		chronomatronChainLengthCount = 0
		chronomatronCurrentSlot = 0
		chronomatronCurrentOrdinal = 0
	}

	companion object {
		@JvmField
        val TERRACOTTA_TO_GLASS: ImmutableMap<Item, Item> = ImmutableMap.ofEntries(
			SimpleImmutableEntry(Items.RED_TERRACOTTA, Items.RED_STAINED_GLASS),
			SimpleImmutableEntry(Items.ORANGE_TERRACOTTA, Items.ORANGE_STAINED_GLASS),
			SimpleImmutableEntry(Items.YELLOW_TERRACOTTA, Items.YELLOW_STAINED_GLASS),
			SimpleImmutableEntry(Items.LIME_TERRACOTTA, Items.LIME_STAINED_GLASS),
			SimpleImmutableEntry(Items.GREEN_TERRACOTTA, Items.GREEN_STAINED_GLASS),
			SimpleImmutableEntry(Items.CYAN_TERRACOTTA, Items.CYAN_STAINED_GLASS),
			SimpleImmutableEntry(Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_STAINED_GLASS),
			SimpleImmutableEntry(Items.BLUE_TERRACOTTA, Items.BLUE_STAINED_GLASS),
			SimpleImmutableEntry(Items.PURPLE_TERRACOTTA, Items.PURPLE_STAINED_GLASS),
			SimpleImmutableEntry(Items.PINK_TERRACOTTA, Items.PINK_STAINED_GLASS)
		)
	}
}
