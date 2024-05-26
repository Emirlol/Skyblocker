package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dungeon.puzzle.Silverfish
import org.joml.Vector2i
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SilverfishTest {
	@Test
	fun testSilverfishSolve() {
		for (i in silverfishBoard.indices) {
			System.arraycopy(silverfishBoard[i], 0, Silverfish.INSTANCE.silverfishBoard.get(i), 0, silverfishBoard[i].size)
		}
		Silverfish.INSTANCE.silverfishPos = Vector2i(15, 15)
		Silverfish.INSTANCE.solve()
		val expectedSilverfishPath: List<Vector2i> = java.util.List.of<Vector2i>(Vector2i(15, 15), Vector2i(15, 11), Vector2i(16, 11), Vector2i(16, 3), Vector2i(0, 3), Vector2i(0, 4), Vector2i(1, 4), Vector2i(1, 2), Vector2i(10, 2), Vector2i(10, 9), Vector2i(0, 9))
		Assertions.assertEquals(expectedSilverfishPath, Silverfish.INSTANCE.silverfishPath)
	}

	companion object {
		private val silverfishBoard = arrayOf(
			booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false, false),
			booleanArrayOf(true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false),
			booleanArrayOf(false, true, false, false, false, false, false, false, false, false, true, false, false, false, false, true, false),
			booleanArrayOf(false, false, true, false, false, false, false, false, false, false, false, true, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
			booleanArrayOf(false, false, false, false, false, false, true, false, false, false, true, false, false, false, false, false, false),
			booleanArrayOf(false, false, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false)
		)
	}
}
