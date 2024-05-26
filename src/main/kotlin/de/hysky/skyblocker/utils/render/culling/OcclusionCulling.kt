package de.hysky.skyblocker.utils.render.culling

object OcclusionCulling {
	private const val TRACING_DISTANCE = 128
	var regularCuller: OcclusionCuller? = null
		private set
	@JvmStatic
	var reducedCuller: OcclusionCuller? = null
		private set

	/**
	 * Initializes the occlusion culling instances
	 */
	fun init() {
		regularCuller = OcclusionCuller(TRACING_DISTANCE, WorldProvider(), 2.0)
		reducedCuller = OcclusionCuller(TRACING_DISTANCE, ReducedWorldProvider(), 0.0)
	}
}
