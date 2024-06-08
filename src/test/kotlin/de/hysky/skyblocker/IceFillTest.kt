package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dungeon.puzzle.IceFill
import org.joml.Vector2i
import org.joml.Vector2ic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IceFillTest {
	@Test
	fun testIceFillSolve() {
		IceFill.INSTANCE.solve(iceFillBoard, iceFillPath)
		val expectedIceFillPath: List<Vector2ic> = java.util.List.of<Vector2ic>(Vector2i(6, 3), Vector2i(5, 3), Vector2i(4, 3), Vector2i(3, 3), Vector2i(3, 2), Vector2i(4, 2), Vector2i(5, 2), Vector2i(6, 2), Vector2i(6, 1), Vector2i(5, 1), Vector2i(5, 0), Vector2i(4, 0), Vector2i(4, 1), Vector2i(3, 1), Vector2i(3, 0), Vector2i(2, 0), Vector2i(1, 0), Vector2i(0, 0), Vector2i(0, 1), Vector2i(1, 1), Vector2i(2, 1), Vector2i(2, 2), Vector2i(1, 2), Vector2i(1, 3), Vector2i(1, 4), Vector2i(1, 5), Vector2i(2, 5), Vector2i(3, 5), Vector2i(3, 4), Vector2i(4, 4), Vector2i(5, 4), Vector2i(6, 4), Vector2i(6, 5), Vector2i(6, 6), Vector2i(5, 6), Vector2i(5, 5), Vector2i(4, 5), Vector2i(4, 6), Vector2i(3, 6), Vector2i(2, 6), Vector2i(1, 6), Vector2i(0, 6), Vector2i(0, 5), Vector2i(0, 4), Vector2i(0, 3))
		Assertions.assertEquals(expectedIceFillPath, iceFillPath)
	}

	companion object {
		private val iceFillBoard = arrayOf(
			booleanArrayOf(false, false, true, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, true, true, false, false),
			booleanArrayOf(false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false),
			booleanArrayOf(true, false, false, false, false, false, false),
		)
		private val iceFillPath: MutableList<Vector2ic> = ArrayList<Vector2ic>()
	}
}
