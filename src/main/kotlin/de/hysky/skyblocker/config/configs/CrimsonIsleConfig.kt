package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry

class CrimsonIsleConfig {
	@SerialEntry
	var kuudra: Kuudra = Kuudra()

	class Kuudra {

		@SerialEntry
		var supplyWaypoints: Boolean = true

		@SerialEntry
		var fuelWaypoints: Boolean = true

		@SerialEntry
		var suppliesAndFuelWaypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@SerialEntry
		var ballistaBuildWaypoints: Boolean = true

		@SerialEntry
		var safeSpotWaypoints: Boolean = true

		@SerialEntry
		var pearlWaypoints: Boolean = true

		@SerialEntry
		var noArrowPoisonWarning: Boolean = true

		@SerialEntry
		var arrowPoisonThreshold: Int = 32
	}
}
