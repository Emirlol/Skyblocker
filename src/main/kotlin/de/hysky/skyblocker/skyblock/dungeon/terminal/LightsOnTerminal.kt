package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

/**
 * The terminal where you change all the panes that are red to green.
 *
 * This doesn't solve the terminal because you don't need a solver for it, but rather to simply allow for click blocking.
 */
object LightsOnTerminal : ContainerSolver("^Correct all the panes!$"), TerminalSolver {
	override val isEnabled: Boolean
		get() = shouldBlockIncorrectClicks()

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> =emptyList()


	override fun onClickSlot(slot: Int, stack: ItemStack, screenId: Int, groups: Array<String>) = stack.isOf(Items.LIME_STAINED_GLASS_PANE)
}
