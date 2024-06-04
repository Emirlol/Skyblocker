package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry

class HelperConfig {
	@SerialEntry
	var enableNewYearCakesHelper: Boolean = true

	@SerialEntry
	var mythologicalRitual: MythologicalRitual = MythologicalRitual()

	@SerialEntry
	var experiments: Experiments = Experiments()

	@SerialEntry
	var fishing: Fishing = Fishing()

	@SerialEntry
	var fairySouls: FairySouls = FairySouls()

	@SerialEntry
	var chocolateFactory: ChocolateFactory = ChocolateFactory()

	class MythologicalRitual {
		@SerialEntry
		var enableMythologicalRitualHelper: Boolean = true
	}

	class Experiments {
		@SerialEntry
		var enableChronomatronSolver: Boolean = true

		@SerialEntry
		var enableSuperpairsSolver: Boolean = true

		@SerialEntry
		var enableUltrasequencerSolver: Boolean = true
	}

	class Fishing {
		@SerialEntry
		var enableFishingHelper: Boolean = true

		@SerialEntry
		var enableFishingTimer: Boolean = false

		@SerialEntry
		var changeTimerColor: Boolean = true

		@SerialEntry
		var fishingTimerScale: Float = 1f

		@SerialEntry
		var hideOtherPlayersRods: Boolean = false
	}

	class FairySouls {
		@SerialEntry
		var enableFairySoulsHelper: Boolean = false

		@SerialEntry
		var highlightFoundSouls: Boolean = true

		@SerialEntry
		var highlightOnlyNearbySouls: Boolean = false
	}

	class ChocolateFactory {
		@SerialEntry
		var enableChocolateFactoryHelper: Boolean = true

		@SerialEntry
		var enableEggFinder: Boolean = true

		@SerialEntry
		var sendEggFoundMessages: Boolean = true

		@SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@SerialEntry
		var enableTimeTowerReminder: Boolean = true
	}
}
