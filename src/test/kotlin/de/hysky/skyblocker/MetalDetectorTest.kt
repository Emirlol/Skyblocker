package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dwarven.MetalDetector
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MetalDetectorTest {
	@Test
	fun testFindPossibleBlocks() {
		//test starting without knowing middle
		MetalDetector.updatePossibleBlocks(10.0, Vec3d(0.0, 0.0, 0.0))
		Assertions.assertEquals(MetalDetector.possibleBlocks.size, 40)

		MetalDetector.updatePossibleBlocks(11.2, Vec3d(5.0, 0.0, 0.0))
		Assertions.assertEquals(MetalDetector.possibleBlocks.size, 2)

		MetalDetector.updatePossibleBlocks(10.0, Vec3d(10.0, 0.0, 10.0))
		Assertions.assertEquals(MetalDetector.possibleBlocks.first, Vec3i(0, 0, 10))

		//test while knowing the middle location
		MetalDetector.possibleBlocks = java.util.ArrayList()
		MetalDetector.newTreasure = true
		MetalDetector.minesCenter = Vec3i(0, 0, 0)

		MetalDetector.updatePossibleBlocks(24.9, Vec3d(10.0, 1.0, 10.0))
		Assertions.assertEquals(MetalDetector.possibleBlocks.size, 1)
		Assertions.assertEquals(MetalDetector.possibleBlocks.first, Vec3i(1, -20, 20))
	}
}
