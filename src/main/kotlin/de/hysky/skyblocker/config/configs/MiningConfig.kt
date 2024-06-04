package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class MiningConfig {
	@SerialEntry
	var enableDrillFuel: Boolean = true

	@SerialEntry
	var dwarvenMines: DwarvenMines = DwarvenMines()

	@SerialEntry
	var dwarvenHud: DwarvenHud = DwarvenHud()

	@SerialEntry
	var crystalHollows: CrystalHollows = CrystalHollows()

	@SerialEntry
	var crystalsHud: CrystalsHud = CrystalsHud()

	@SerialEntry
	var crystalsWaypoints: CrystalsWaypoints = CrystalsWaypoints()

	@SerialEntry
	var commissionWaypoints: CommissionWaypoints = CommissionWaypoints()

	@SerialEntry
	var glacite: Glacite = Glacite()

	class DwarvenMines {

		@SerialEntry
		var solveFetchur: Boolean = true

		@SerialEntry
		var solvePuzzler: Boolean = true
	}

	class DwarvenHud {

		@SerialEntry
		var enabledCommissions: Boolean = true

		@SerialEntry
		var enabledPowder: Boolean = true

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

		@SerialEntry
		var metalDetectorHelper: Boolean = true
	}

	class CrystalsHud {

		@SerialEntry
		var enabled: Boolean = true

		@SerialEntry
		var showLocations: Boolean = true

		@SerialEntry
		var locationSize: Int = 8

		@SerialEntry
		var x: Int = 10

		@SerialEntry
		var y: Int = 130

		@SerialEntry
		var mapScaling: Float = 1f
	}

	class CrystalsWaypoints {

		@SerialEntry
		var enabled: Boolean = true

		@SerialEntry
		var findInChat: Boolean = true
	}

	class CommissionWaypoints {

		@SerialEntry
		var mode: CommissionWaypointMode = CommissionWaypointMode.BOTH

		@SerialEntry
		var useColor: Boolean = true

		@SerialEntry
		var textScale: Float = 1f

		@SerialEntry
		var showBaseCamp: Boolean = false

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
