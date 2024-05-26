package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class SlayersConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var endermanSlayer: EndermanSlayer = EndermanSlayer()

	@kotlin.jvm.JvmField
	@SerialEntry
	var vampireSlayer: VampireSlayer = VampireSlayer()

	class EndermanSlayer {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableYangGlyphsNotification: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightBeacons: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightNukekubiHeads: Boolean = true
	}

	class VampireSlayer {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableEffigyWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var compactEffigyWaypoints: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var effigyUpdateFrequency: Int = 5

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableHolyIceIndicator: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var holyIceIndicatorTickDelay: Int = 10

		@kotlin.jvm.JvmField
		@SerialEntry
		var holyIceUpdateFrequency: Int = 5

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableHealingMelonIndicator: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var healingMelonHealthThreshold: Float = 4f

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableSteakStakeIndicator: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var steakStakeUpdateFrequency: Int = 5

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableManiaIndicator: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var maniaUpdateFrequency: Int = 5
	}
}
