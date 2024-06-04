package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class SlayersConfig {
	@SerialEntry
	var endermanSlayer: EndermanSlayer = EndermanSlayer()

	@SerialEntry
	var vampireSlayer: VampireSlayer = VampireSlayer()

	class EndermanSlayer {

		@SerialEntry
		var enableYangGlyphsNotification: Boolean = true

		@SerialEntry
		var highlightBeacons: Boolean = true

		@SerialEntry
		var highlightNukekubiHeads: Boolean = true
	}

	class VampireSlayer {

		@SerialEntry
		var enableEffigyWaypoints: Boolean = true

		@SerialEntry
		var compactEffigyWaypoints: Boolean = false

		@SerialEntry
		var effigyUpdateFrequency: Int = 5

		@SerialEntry
		var enableHolyIceIndicator: Boolean = true

		@SerialEntry
		var holyIceIndicatorTickDelay: Int = 10

		@SerialEntry
		var holyIceUpdateFrequency: Int = 5

		@SerialEntry
		var enableHealingMelonIndicator: Boolean = true

		@SerialEntry
		var healingMelonHealthThreshold: Float = 4f

		@SerialEntry
		var enableSteakStakeIndicator: Boolean = true

		@SerialEntry
		var steakStakeUpdateFrequency: Int = 5

		@SerialEntry
		var enableManiaIndicator: Boolean = true

		@SerialEntry
		var maniaUpdateFrequency: Int = 5
	}
}
