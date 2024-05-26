package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

/**
 * The terminal where you change all the panes that are red to green.
 *
 * This doesn't solve the terminal because you don't need a solver for it, but rather to simply allow for click blocking.
 */
class LightsOnTerminal : ContainerSolver("^Correct all the panes!$"), TerminalSolver {
	override val isEnabled: Boolean
		get() = shouldBlockIncorrectClicks()

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		return EMPTY
	}

	protected override fun onClickSlot(slot: Int, stack: ItemStack?, screenId: Int, groups: Array<String?>?): Boolean {
		return stack!!.isOf(Items.LIME_STAINED_GLASS_PANE)
	}

	companion object {
		private val EMPTY: List<ColorHighlight?> = listOf<ColorHighlight>()
	}
}
