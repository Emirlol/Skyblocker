package de.hysky.skyblocker.skyblock.waypoint

import org.junit.jupiter.api.Assertions

class MythologicalRitualTest {
	@Test
	fun testFillLine() {
		val line = arrayOfNulls<net.minecraft.util.math.Vec3d>(21)
		val start = net.minecraft.util.math.Vec3d(0.0, 0.0, 0.0)
		val direction = net.minecraft.util.math.Vec3d(1.0, 0.0, 0.0)
		MythologicalRitual.fillLine(line, start, direction)
		for (i in line.indices) {
			Assertions.assertEquals(net.minecraft.util.math.Vec3d((i - 10).toDouble(), 0.0, 0.0), line[i])
		}
	}
}
