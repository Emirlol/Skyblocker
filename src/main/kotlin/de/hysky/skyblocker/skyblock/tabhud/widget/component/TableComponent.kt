package de.hysky.skyblocker.skyblock.tabhud.widget.component

import net.minecraft.client.gui.DrawContext
import kotlin.math.max

/**
 * Meta-Component that consists of a grid of other components
 * Grid cols are separated by lines.
 */
class TableComponent(private val cols: Int, private val rows: Int, col: Int) : Component() {
	private val comps = Array(cols) { arrayOfNulls<Component>(rows) }
	private val color = -0x1000000 or col
	private var cellW = 0
	private var cellH = 0

	fun addToCell(x: Int, y: Int, c: Component) {
		comps[x][y] = c

		// pad extra to add a vertical line later
		this.cellW = max(cellW.toDouble(), (c.width + Component.Companion.PAD_S + Component.Companion.PAD_L).toDouble()).toInt()
		this.cellH = max((c.height + Component.Companion.PAD_S).toDouble(), cellH.toDouble()).toInt()

		this.width = this.cellW * this.cols
		this.height = (this.cellH * this.rows) - Component.Companion.PAD_S / 2
	}

	override fun render(context: DrawContext, xpos: Int, ypos: Int) {
		for (x in 0 until cols) {
			for (y in 0 until rows) {
				val comp = comps[x][y]
				comp?.render(context, xpos + (x * cellW), ypos + y * cellH + (cellH / 2 - comp.height / 2))
			}
			// add a line before the col if we're not drawing the first one
			if (x != 0) {
				val lineX1: Int = xpos + (x * cellW) - Component.Companion.PAD_S - 1
				val lineX2: Int = xpos + (x * cellW) - Component.Companion.PAD_S
				val lineY1 = ypos + 1
				val lineY2: Int = ypos + this.height - Component.Companion.PAD_S - 1 // not sure why but it looks correct
				context.fill(lineX1, lineY1, lineX2, lineY2, this.color)
			}
		}
	}
}
