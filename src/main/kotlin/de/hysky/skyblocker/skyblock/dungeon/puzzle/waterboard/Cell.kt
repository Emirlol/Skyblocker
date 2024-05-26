package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard

open class Cell private constructor(@JvmField val type: Type) {
	open val isOpen: Boolean
		get() = type == Type.EMPTY

	class SwitchCell(@JvmField val id: Int) : Cell(Type.SWITCH) {
		override var isOpen: Boolean = false
			private set

		override fun equals(obj: Any?): Boolean {
			return super.equals(obj) || obj is SwitchCell && id == obj.id && isOpen == obj.isOpen
		}

		fun toggle() {
			isOpen = !isOpen
		}

		companion object {
			@JvmStatic
            fun ofOpened(id: Int): SwitchCell {
				val switchCell = SwitchCell(id)
				switchCell.isOpen = true
				return switchCell
			}
		}
	}

	enum class Type {
		BLOCK,
		EMPTY,
		SWITCH
	}

	companion object {
		@JvmField
        val BLOCK: Cell = Cell(Type.BLOCK)
		@JvmField
        val EMPTY: Cell = Cell(Type.EMPTY)
	}
}
