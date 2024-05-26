package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard

import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Cell.SwitchCell
import java.util.*

class Switch(val id: Int) : AbstractCollection<SwitchCell>() {
	val cells: MutableList<SwitchCell> = ArrayList()

	override fun iterator(): MutableIterator<SwitchCell> {
		return cells.iterator()
	}

	override fun size(): Int {
		return cells.size
	}

	override fun add(cell: SwitchCell): Boolean {
		return cells.add(cell)
	}

	fun toggle() {
		for (cell in cells) {
			cell.toggle()
		}
	}
}
