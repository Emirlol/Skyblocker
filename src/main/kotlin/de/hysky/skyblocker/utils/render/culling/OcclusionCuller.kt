package de.hysky.skyblocker.utils.render.culling

import com.logisticscraft.occlusionculling.OcclusionCullingInstance
import com.logisticscraft.occlusionculling.cache.ArrayOcclusionCache
import com.logisticscraft.occlusionculling.util.Vec3d
import de.hysky.skyblocker.utils.render.FrustumUtils
import net.minecraft.client.MinecraftClient

class OcclusionCuller internal constructor(tracingDistance: Int, worldProvider: WorldProvider?, aabbExpansion: Double) {
	private val instance = OcclusionCullingInstance(tracingDistance, worldProvider, ArrayOcclusionCache(tracingDistance), aabbExpansion)

	// Reused objects to reduce allocation overhead
	private val cameraPos = Vec3d(0.0, 0.0, 0.0)
	private val min = Vec3d(0.0, 0.0, 0.0)
	private val max = Vec3d(0.0, 0.0, 0.0)

	private fun updateCameraPos() {
		val camera = MinecraftClient.getInstance().gameRenderer.camera.pos
		cameraPos[camera.x, camera.y] = camera.z
	}

	/**
	 * This first checks checks if the bounding box is within the camera's FOV, if
	 * it is then it checks for whether it's occluded or not.
	 *
	 * @return A boolean representing whether the bounding box is fully visible or
	 * not as per the instance's settings.
	 */
	fun isVisible(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean {
		if (!FrustumUtils.isVisible(x1, y1, z1, x2, y2, z2)) return false

		updateCameraPos()
		min[x1, y1] = z1
		max[x2, y2] = z2

		return instance.isAABBVisible(min, max, cameraPos)
	}
}
