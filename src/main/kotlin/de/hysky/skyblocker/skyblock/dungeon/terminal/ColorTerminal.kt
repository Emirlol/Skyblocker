package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal.Companion.colorFromName
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal.Companion.itemColor
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ColorTerminal : ContainerSolver("^Select all the ([A-Z ]+) items!$"), TerminalSolver {
	private var targetColor: DyeColor? = null
	override val isEnabled: Boolean
		get() {
			targetColor = null
			return SkyblockerConfigManager.config.dungeons.terminals.solveColor
		}

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		trimEdges(slots!!, 6)
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		val colorString = groups!![0]
		if (targetColor == null) {
			targetColor = colorFromName[colorString]
			if (targetColor == null) {
				LOGGER.error("[Skyblocker] Couldn't find dye color corresponding to \"$colorString\"")
				return emptyList<ColorHighlight>()
			}
		}
		for (slot in slots.int2ObjectEntrySet()) {
			val itemStack = slot.value
			if (!itemStack!!.hasGlint() && targetColor == itemColor[itemStack.item]) {
				highlights.add(green(slot.intKey))
			}
		}
		return highlights
	}

	protected override fun onClickSlot(slot: Int, stack: ItemStack?, screenId: Int, groups: Array<String?>?): Boolean {
		if (stack!!.hasGlint() || targetColor != itemColor[stack.item]) {
			return shouldBlockIncorrectClicks()
		}

		return false
	}


		private val LOGGER: Logger = LoggerFactory.getLogger(ColorTerminal::class.java.name)
		private val colorFromName: MutableMap<String?, DyeColor> = HashMap()
		private val itemColor: MutableMap<Item, DyeColor>

		init {
			for (color in DyeColor.entries) colorFromName[color.getName().uppercase()] = color
			colorFromName["SILVER"] = DyeColor.LIGHT_GRAY
			colorFromName["LIGHT BLUE"] = DyeColor.LIGHT_BLUE

			itemColor = HashMap()
			for (color in DyeColor.entries) for (item in arrayOf("dye", "wool", "stained_glass", "terracotta")) itemColor[Registries.ITEM[Identifier(color.getName() + '_' + item)]] = color
			itemColor[Items.BONE_MEAL] = DyeColor.WHITE
			itemColor[Items.LAPIS_LAZULI] = DyeColor.BLUE
			itemColor[Items.COCOA_BEANS] = DyeColor.BROWN
			itemColor[Items.INK_SAC] = DyeColor.BLACK
		}

}