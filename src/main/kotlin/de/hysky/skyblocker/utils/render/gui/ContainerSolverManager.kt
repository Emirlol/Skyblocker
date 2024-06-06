package de.hysky.skyblocker.utils.render.gui

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakeBagHelper
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakesHelper
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver
import de.hysky.skyblocker.skyblock.dungeon.CroesusHelper
import de.hysky.skyblocker.skyblock.dungeon.CroesusProfit
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal
import de.hysky.skyblocker.skyblock.dungeon.terminal.LightsOnTerminal
import de.hysky.skyblocker.skyblock.dungeon.terminal.OrderTerminal
import de.hysky.skyblocker.skyblock.dungeon.terminal.StartsWithTerminal
import de.hysky.skyblocker.skyblock.experiment.ChronomatronSolver
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver
import de.hysky.skyblocker.utils.Utils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.util.regex.Pattern

object ContainerSolverManager {
	private val PLACEHOLDER_PATTERN = Pattern.compile("")
	private var currentSolver: ContainerSolver? = null
	/**
	 * Useful for keeping track of a solver's state in a Screen instance, such as if Hypixel closes & reopens a screen after every click (as they do with terminals).
	 */
	private var screenId = 0
	private var groups: Array<String>? = null

	private var highlights: List<ColorHighlight>? = null
	private val solvers = arrayOf(
		ColorTerminal,
		OrderTerminal,
		StartsWithTerminal,
		LightsOnTerminal,
		CroesusHelper,
		CroesusProfit,
		ChronomatronSolver,
		SuperpairsSolver,
		UltrasequencerSolver,
		NewYearCakeBagHelper,
		NewYearCakesHelper,
		ChocolateFactorySolver
	)

	init {
		ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
			if (Utils.isOnSkyblock && screen is GenericContainerScreen) {
				ScreenEvents.afterRender(screen).register { _, context, _, _, _ ->
					with(context.matrices) {
						push()
						translate((screen as HandledScreenAccessor).x.toDouble(), (screen as HandledScreenAccessor).y.toDouble(), 0.0)
						onDraw(context, screen.screenHandler.slots.subList(0, screen.screenHandler.rows*9))
						pop()
					}
				}
				ScreenEvents.remove(screen).register { clearScreen() }
				onSetScreen(screen)
			} else clearScreen()
		}
	}

	private fun onSetScreen(screen: GenericContainerScreen) {
		val screenName = screen.title.string
		val matcher = PLACEHOLDER_PATTERN.matcher(screenName)
		for (solver in solvers) {
			if (!solver.isEnabled) continue
			matcher.usePattern(solver.name)
			matcher.reset()
			if (!matcher.matches()) continue
			++screenId
			currentSolver = solver
			groups = Array(matcher.groupCount()) { matcher.group(it + 1) }
			currentSolver!!.start(screen)
			markDirty()
			return
		}
		clearScreen()
	}

	fun clearScreen() {
		if (currentSolver != null) {
			currentSolver!!.reset();
			currentSolver = null;
		}
	}

	fun markDirty() {
		highlights = null;
	}

	fun onSlotClick(slot: Int, stack: ItemStack) = currentSolver?.onClickSlot(slot, stack, screenId, groups!!) ?: false


	private fun onDraw(context: DrawContext, slots: List<Slot>) {
		currentSolver ?: return
		if (highlights == null) highlights = currentSolver!!.getColors(groups!!, slots)
		RenderSystem.enableDepthTest()
		RenderSystem.colorMask(true, true, true, false)
		for (highlight in highlights!!) {
			with(slots[highlight.slot]) {
				context.fill(x, y, x + 16, y + 16, highlight.color)
			}
		}
		RenderSystem.colorMask(true, true, true, true)
	}
}