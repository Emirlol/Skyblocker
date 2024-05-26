package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry

class CrimsonIsleConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var kuudra: Kuudra = Kuudra()

	class Kuudra {
		@kotlin.jvm.JvmField
		@SerialEntry
		var supplyWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var fuelWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var suppliesAndFuelWaypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@kotlin.jvm.JvmField
		@SerialEntry
		var ballistaBuildWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var safeSpotWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var pearlWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var noArrowPoisonWarning: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var arrowPoisonThreshold: Int = 32
	}
}
