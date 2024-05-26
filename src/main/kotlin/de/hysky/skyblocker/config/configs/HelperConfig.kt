package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry

class HelperConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var enableNewYearCakesHelper: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var mythologicalRitual: MythologicalRitual = MythologicalRitual()

	@kotlin.jvm.JvmField
	@SerialEntry
	var experiments: Experiments = Experiments()

	@kotlin.jvm.JvmField
	@SerialEntry
	var fishing: Fishing = Fishing()

	@kotlin.jvm.JvmField
	@SerialEntry
	var fairySouls: FairySouls = FairySouls()

	@kotlin.jvm.JvmField
	@SerialEntry
	var chocolateFactory: ChocolateFactory = ChocolateFactory()

	class MythologicalRitual {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableMythologicalRitualHelper: Boolean = true
	}

	class Experiments {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableChronomatronSolver: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableSuperpairsSolver: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableUltrasequencerSolver: Boolean = true
	}

	class Fishing {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableFishingHelper: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableFishingTimer: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var changeTimerColor: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var fishingTimerScale: Float = 1f

		@kotlin.jvm.JvmField
		@SerialEntry
		var hideOtherPlayersRods: Boolean = false
	}

	class FairySouls {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableFairySoulsHelper: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightFoundSouls: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightOnlyNearbySouls: Boolean = false
	}

	class ChocolateFactory {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableChocolateFactoryHelper: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableEggFinder: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var sendEggFoundMessages: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableTimeTowerReminder: Boolean = true
	}
}
