package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class FarmingConfig {
	@SerialEntry
	var garden: Garden = Garden()

	class Garden {
		@SerialEntry
		var farmingHud: FarmingHud = FarmingHud()

		@SerialEntry
		var dicerTitlePrevent: Boolean = true

		@SerialEntry
		var visitorHelper: Boolean = true

		@SerialEntry
		var lockMouseTool: Boolean = false

		@SerialEntry
		var lockMouseGroundOnly: Boolean = false
	}

	class FarmingHud {
		@SerialEntry
		var enableHud: Boolean = true

		@SerialEntry
		var x: Int = 0

		@SerialEntry
		var y: Int = 0
	}
}
