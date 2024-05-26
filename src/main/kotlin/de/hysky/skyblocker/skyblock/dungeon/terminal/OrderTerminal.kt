package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class OrderTerminal : ContainerSolver("^Click in order!$"), TerminalSolver {
	private val PANES_NUM = 14
	private var orderedSlots: IntArray?
	private var currentNum = Int.MAX_VALUE

	override val isEnabled: Boolean
		get() {
			orderedSlots = null
			currentNum = 0
			return SkyblockerConfigManager.get().dungeons.terminals.solveOrder
		}

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		if (orderedSlots == null && !orderSlots(slots)) return emptyList<ColorHighlight>()
		while (currentNum < PANES_NUM && Items.LIME_STAINED_GLASS_PANE == slots!![orderedSlots!![currentNum]]!!.item) currentNum++
		val highlights: MutableList<ColorHighlight?> = ArrayList(3)
		val last = Integer.min(3, PANES_NUM - currentNum)
		for (i in 0 until last) {
			highlights.add(ColorHighlight(orderedSlots!![currentNum + i], (224 - 64 * i) shl 24 or (64 shl 16) or (96 shl 8) or 255))
		}
		return highlights
	}

	fun orderSlots(slots: Int2ObjectMap<ItemStack?>?): Boolean {
		trimEdges(slots!!, 4)
		orderedSlots = IntArray(PANES_NUM)
		for (slot in slots.int2ObjectEntrySet()) {
			if (Items.AIR == slot.value!!.item) {
				orderedSlots = null
				return false
			} else orderedSlots!![slot.value!!.count - 1] = slot.intKey
		}
		currentNum = 0
		return true
	}

	protected override fun onClickSlot(slot: Int, stack: ItemStack?, screenId: Int, groups: Array<String?>?): Boolean {
		if (stack == null || stack.isEmpty) return false

		if (!stack.isOf(Items.RED_STAINED_GLASS_PANE) || stack.count != currentNum + 1) {
			return shouldBlockIncorrectClicks()
		}

		return false
	}
}