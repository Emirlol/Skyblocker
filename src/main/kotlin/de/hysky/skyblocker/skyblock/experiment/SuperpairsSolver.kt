package de.hysky.skyblocker.skyblock.experiment

import de.hysky.skyblocker.config.configs.HelperConfig.Experiments
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.yellow
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.function.Predicate

class SuperpairsSolver : ExperimentSolver("^Superpairs \\(\\w+\\)$") {
	private var superpairsPrevClickedSlot = 0
	private var superpairsCurrentSlot: ItemStack? = null
	private val superpairsDuplicatedSlots: MutableSet<Int> = HashSet()

	fun setSuperpairsPrevClickedSlot(superpairsPrevClickedSlot: Int) {
		this.superpairsPrevClickedSlot = superpairsPrevClickedSlot
	}

	fun setSuperpairsCurrentSlot(superpairsCurrentSlot: ItemStack?) {
		this.superpairsCurrentSlot = superpairsCurrentSlot
	}

	override fun isEnabled(experimentsConfig: Experiments): Boolean {
		return experimentsConfig.enableSuperpairsSolver
	}

	override fun start(screen: GenericContainerScreen?) {
		super.start(screen)
		state = State.SHOW
	}

	override fun reset() {
		super.reset()
		superpairsPrevClickedSlot = 0
		superpairsCurrentSlot = null
		superpairsDuplicatedSlots.clear()
	}

	override fun tick(screen: Screen) {
		if (isEnabled && screen is GenericContainerScreen && screen.getTitle().string.startsWith("Superpairs (")) {
			if (state == State.SHOW && slots[superpairsPrevClickedSlot] == null) {
				val itemStack: ItemStack = screen.screenHandler.inventory.getStack(superpairsPrevClickedSlot)
				if (!(itemStack.isOf(Items.CYAN_STAINED_GLASS) || itemStack.isOf(Items.BLACK_STAINED_GLASS_PANE) || itemStack.isOf(Items.AIR))) {
					slots.entries.stream().filter((Predicate<Map.Entry<Int?, ItemStack?>> { entry: Map.Entry<Int?, ItemStack?> -> ItemStack.areEqual(entry.value, itemStack) })).findAny().ifPresent { entry: Map.Entry<Int, ItemStack?> -> superpairsDuplicatedSlots.add(entry.key) }
					slots[superpairsPrevClickedSlot] = itemStack
					superpairsCurrentSlot = itemStack
				}
			}
		} else {
			reset()
		}
	}

	protected override fun getColors(groups: Array<String?>?, displaySlots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		if (state == State.SHOW) {
			for (indexStack in displaySlots!!.int2ObjectEntrySet()) {
				val index = indexStack.intKey
				val displayStack = indexStack.value
				val stack = slots[index]
				if (stack != null && !ItemStack.areEqual(stack, displayStack)) {
					if (ItemStack.areEqual(superpairsCurrentSlot, stack) && displayStack!!.name.string == "Click a second button!") {
						highlights.add(green(index))
					} else if (superpairsDuplicatedSlots.contains(index)) {
						highlights.add(yellow(index))
					} else {
						highlights.add(red(index))
					}
				}
			}
		}
		return highlights
	}
}
