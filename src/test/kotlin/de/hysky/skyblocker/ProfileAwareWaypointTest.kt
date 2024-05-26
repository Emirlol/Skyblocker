package de.hysky.skyblocker

import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProfileAwareWaypointTest {
	@Test
	fun testShouldRender() {
		val waypoint = ProfileAwareWaypoint(BlockPos.ORIGIN, null, null, null)
		waypoint.setFound("profile")
		Assertions.assertTrue(waypoint.shouldRender())
		waypoint.setFound("")
		Assertions.assertFalse(waypoint.shouldRender())
		waypoint.setMissing()
		Assertions.assertTrue(waypoint.shouldRender())
	}

	@Test
	fun testGetColorComponents() {
		val waypoint = ProfileAwareWaypoint(BlockPos.ORIGIN, null, floatArrayOf(0f, 0.5f, 1f), floatArrayOf(1f, 0.5f, 0f))
		waypoint.setFound("profile")
		var colorComponents = waypoint.getColorComponents()
		Assertions.assertEquals(0f, colorComponents!![0])
		Assertions.assertEquals(0.5f, colorComponents!![1])
		Assertions.assertEquals(1f, colorComponents!![2])
		waypoint.setFound("")
		colorComponents = waypoint.getColorComponents()
		Assertions.assertEquals(1f, colorComponents!![0])
		Assertions.assertEquals(0.5f, colorComponents!![1])
		Assertions.assertEquals(0f, colorComponents!![2])
		waypoint.setMissing()
		colorComponents = waypoint.getColorComponents()
		Assertions.assertEquals(0f, colorComponents!![0])
		Assertions.assertEquals(0.5f, colorComponents!![1])
		Assertions.assertEquals(1f, colorComponents!![2])
	}
}