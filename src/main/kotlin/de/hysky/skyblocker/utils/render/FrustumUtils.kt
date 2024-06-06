package de.hysky.skyblocker.utils.render

import de.hysky.skyblocker.mixins.accessors.FrustumInvoker
import de.hysky.skyblocker.mixins.accessors.WorldRendererAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Frustum
import net.minecraft.util.math.Box

object FrustumUtils {
	private val frustum: Frustum
		get() = (MinecraftClient.getInstance().worldRenderer as WorldRendererAccessor).frustum

	fun isVisible(box: Box)= frustum.isVisible(box)

	fun isVisible(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) = (frustum as FrustumInvoker).invokeIsVisible(minX, minY, minZ, maxX, maxY, maxZ)
}