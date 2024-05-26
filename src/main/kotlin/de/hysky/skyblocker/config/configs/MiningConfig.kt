package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class MiningConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var enableDrillFuel: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var dwarvenMines: DwarvenMines = DwarvenMines()

	@kotlin.jvm.JvmField
	@SerialEntry
	var dwarvenHud: DwarvenHud = DwarvenHud()

	@kotlin.jvm.JvmField
	@SerialEntry
	var crystalHollows: CrystalHollows = CrystalHollows()

	@kotlin.jvm.JvmField
	@SerialEntry
	var crystalsHud: CrystalsHud = CrystalsHud()

	@kotlin.jvm.JvmField
	@SerialEntry
	var crystalsWaypoints: CrystalsWaypoints = CrystalsWaypoints()

	@kotlin.jvm.JvmField
	@SerialEntry
	var commissionWaypoints: CommissionWaypoints = CommissionWaypoints()

	@kotlin.jvm.JvmField
	@SerialEntry
	var glacite: Glacite = Glacite()

	class DwarvenMines {
		@kotlin.jvm.JvmField
		@SerialEntry
		var solveFetchur: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solvePuzzler: Boolean = true
	}

	class DwarvenHud {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enabledCommissions: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enabledPowder: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var style: DwarvenHudStyle = DwarvenHudStyle.SIMPLE

		@SerialEntry
		var commissionsX: Int = 10

		@SerialEntry
		var commissionsY: Int = 10

		@SerialEntry
		var powderX: Int = 10

		@SerialEntry
		var powderY: Int = 70
	}

	class CrystalHollows {
		@kotlin.jvm.JvmField
		@SerialEntry
		var metalDetectorHelper: Boolean = true
	}

	class CrystalsHud {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enabled: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var showLocations: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var locationSize: Int = 8

		@SerialEntry
		var x: Int = 10

		@SerialEntry
		var y: Int = 130

		@kotlin.jvm.JvmField
		@SerialEntry
		var mapScaling: Float = 1f
	}

	class CrystalsWaypoints {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enabled: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var findInChat: Boolean = true
	}

	class CommissionWaypoints {
		@kotlin.jvm.JvmField
		@SerialEntry
		var mode: CommissionWaypointMode = CommissionWaypointMode.BOTH

		@kotlin.jvm.JvmField
		@SerialEntry
		var useColor: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var textScale: Float = 1f

		@kotlin.jvm.JvmField
		@SerialEntry
		var showBaseCamp: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var showEmissary: Boolean = true
	}

	enum class CommissionWaypointMode {
		OFF, DWARVEN, GLACITE, BOTH;

		override fun toString(): String {
			return when (this) {
				OFF -> "Off"
				DWARVEN -> "Dwarven"
				GLACITE -> "Glacite"
				BOTH -> "Both"
			}
		}
	}

	class Glacite {
		@kotlin.jvm.JvmField
		@SerialEntry
		var coldOverlay: Boolean = true
	}

	enum class DwarvenHudStyle {
		SIMPLE, FANCY, CLASSIC;

		override fun toString(): String {
			return when (this) {
				SIMPLE -> "Simple"
				FANCY -> "Fancy"
				CLASSIC -> "Classic"
			}
		}
	}
}
