package de.hysky.skyblocker.utils.render.gui

import de.hysky.skyblocker.SkyblockerMod
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import java.util.regex.Pattern

/**
 * Abstract class for gui solvers. Extend this class to add a new gui solver, like terminal solvers or experiment solvers.
 */
abstract class ContainerSolver protected constructor(containerName: String?) {
	val name: Pattern = Pattern.compile(containerName)

	abstract val isEnabled: Boolean

	open fun start(screen: GenericContainerScreen?) {
	}

	open fun reset() {
	}

	protected fun markHighlightsDirty() {
		SkyblockerMod.getInstance().containerSolverManager.markDirty()
	}

	open fun onClickSlot(slot: Int, stack: ItemStack?, screenId: Int, groups: Array<String?>?): Boolean {
		return false
	}

	abstract fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>?

	protected fun trimEdges(slots: Int2ObjectMap<ItemStack?>, rows: Int) {
		for (i in 0 until rows) {
			slots.remove(9 * i)
			slots.remove(9 * i + 8)
		}
		for (i in 1..7) {
			slots.remove(i)
			slots.remove((rows - 1) * 9 + i)
		}
	}
}
