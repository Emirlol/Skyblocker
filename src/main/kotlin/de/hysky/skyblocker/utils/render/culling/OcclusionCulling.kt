package de.hysky.skyblocker.utils.render.culling

object OcclusionCulling {
	private const val TRACING_DISTANCE = 128
	val regularCuller: OcclusionCuller = OcclusionCuller(TRACING_DISTANCE, WorldProvider(), 2.0)
	val reducedCuller: OcclusionCuller = OcclusionCuller(TRACING_DISTANCE, ReducedWorldProvider(), 0.0)
}
