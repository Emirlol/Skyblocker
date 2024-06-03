package de.hysky.skyblocker.utils.waypoint

import de.hysky.skyblocker.utils.Utils.profile
import net.minecraft.util.math.BlockPos

class ProfileAwareWaypoint(pos: BlockPos, typeSupplier: () -> Type, private val missingColor: FloatArray, private val foundColor: FloatArray) : Waypoint(pos, typeSupplier, FloatArray(3)) {
	val foundProfiles: MutableSet<String> = HashSet()

	override fun shouldRender(): Boolean {
		return !foundProfiles.contains(profile)
	}

	override fun setFound() {
		foundProfiles.add(profile)
	}

	fun setFound(profile: String) {
		foundProfiles.add(profile)
	}

	override fun setMissing() {
		foundProfiles.remove(profile)
	}

	override val colorComponents: FloatArray
		get() = if (foundProfiles.contains(profile)) foundColor else missingColor
}
