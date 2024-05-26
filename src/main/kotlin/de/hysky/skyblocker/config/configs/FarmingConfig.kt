package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class FarmingConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var garden: Garden = Garden()

	class Garden {
		@kotlin.jvm.JvmField
		@SerialEntry
		var farmingHud: FarmingHud = FarmingHud()

		@kotlin.jvm.JvmField
		@SerialEntry
		var dicerTitlePrevent: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var visitorHelper: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var lockMouseTool: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var lockMouseGroundOnly: Boolean = false
	}

	class FarmingHud {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableHud: Boolean = true

		@SerialEntry
		var x: Int = 0

		@SerialEntry
		var y: Int = 0
	}
}
