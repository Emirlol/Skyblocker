package de.hysky.skyblocker.utils.render.gui

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.util.regex.Pattern

/**
 * Abstract class for gui solvers. Extend this class to add a new gui solver, like terminal solvers or experiment solvers.
 */
abstract class ContainerSolver(containerName: String) {
	val name: Pattern = Pattern.compile(containerName)

	abstract val isEnabled: Boolean

	open fun start(screen: GenericContainerScreen) {}

	open fun reset() {}

	protected fun markHighlightsDirty() = ContainerSolverManager.markDirty()

	open fun onClickSlot(slot: Int, stack: ItemStack, screenId: Int, groups: Array<String>) = false

	abstract fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight>

	protected fun trimEdges(slots: MutableList<Slot>, rows: Int) {
		for (i in 0 until rows) {
			slots.removeAt(9 * i)
			slots.removeAt(9 * i + 8)
		}
		for (i in 1..7) {
			slots.removeAt(i)
			slots.removeAt((rows - 1) * 9 + i)
		}
	}
}
