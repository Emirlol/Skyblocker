package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import kotlin.math.min

object OrderTerminal : ContainerSolver("^Click in order!$"), TerminalSolver {
	private val PANES_NUM = 14
	private var orderedSlots: IntArray? = null
	private var currentNum = Int.MAX_VALUE

	override val isEnabled: Boolean
		get() {
			orderedSlots = null
			currentNum = 0
			return SkyblockerConfigManager.config.dungeons.terminals.solveOrder
		}

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		if (orderedSlots == null && !orderSlots(slots)) return emptyList()
		while (currentNum < PANES_NUM && Items.LIME_STAINED_GLASS_PANE == slots[orderedSlots!![currentNum]].stack.item) currentNum++
		val highlights: MutableList<ColorHighlight> = ArrayList(3)
		for (i in 0 until min(3, PANES_NUM - currentNum)) {
			highlights += ColorHighlight(orderedSlots!![currentNum + i], (224 - 64 * i) shl 24 or (64 shl 16) or (96 shl 8) or 255)
		}
		return highlights
	}

	private fun orderSlots(slots: List<Slot>): Boolean {
		trimEdges(slots as MutableList, 4)
		orderedSlots = IntArray(PANES_NUM)
		for (slot in slots) {
			if (slot.stack.isEmpty) {
				orderedSlots = null
				return false
			} else orderedSlots!![slot.stack.count - 1] = slot.id
		}
		currentNum = 0
		return true
	}

	override fun onClickSlot(slot: Int, stack: ItemStack, screenId: Int, groups: Array<String>) = if (!stack.isEmpty || !stack.isOf(Items.RED_STAINED_GLASS_PANE) || stack.count != currentNum + 1) shouldBlockIncorrectClicks() else false
}