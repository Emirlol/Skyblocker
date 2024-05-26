package de.hysky.skyblocker

import de.hysky.skyblocker.utils.PosUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PosUtilsTest {
	@Test
	fun testParsePosString() {
		Assertions.assertEquals(PosUtils.parsePosString("-1,0,1"), BlockPos(-1, 0, 1))
	}

	@Test
	fun testGetPosString() {
		Assertions.assertEquals(PosUtils.getPosString(BlockPos(-1, 0, 1)), "-1,0,1")
	}
}