package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dwarven.CrystalsLocationsManager
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CrystalsLocationManagerTest {
	@Test
	fun testLocationInCrystals() {
		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(BlockPos(512, 70, 512)))

		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(BlockPos(202, 31, 202)))
		Assertions.assertTrue(CrystalsLocationsManager.checkInCrystals(BlockPos(823, 188, 823)))

		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(201, 31, 202)))
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(202, 30, 202)))
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(202, 31, 201)))

		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(824, 188, 823)))
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(823, 189, 823)))
		Assertions.assertFalse(CrystalsLocationsManager.checkInCrystals(BlockPos(823, 188, 824)))
	}

	@Test
	fun testSetLocationMessage() {
		Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Jungle Temple", BlockPos(10, 11, 12)).string, de.hysky.skyblocker.utils.Constants.PREFIX.string + "Added waypoint for Jungle Temple at : 10 11 12.")
		Assertions.assertEquals(CrystalsLocationsManager.getSetLocationMessage("Fairy Grotto", BlockPos(0, 0, 0)).string, de.hysky.skyblocker.utils.Constants.PREFIX.string + "Added waypoint for Fairy Grotto at : 0 0 0.")
	}
}
