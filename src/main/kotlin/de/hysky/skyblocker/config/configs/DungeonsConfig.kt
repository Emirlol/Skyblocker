package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.util.Formatting

class DungeonsConfig {
	@SerialEntry
	var fancyPartyFinder: Boolean = true

	@SerialEntry
	var croesusHelper: Boolean = true

	@SerialEntry
	var playerSecretsTracker: Boolean = false

	@SerialEntry
	var starredMobGlow: Boolean = false

	@SerialEntry
	var starredMobBoundingBoxes: Boolean = true

	@SerialEntry
	var allowDroppingProtectedItems: Boolean = false

	@SerialEntry
	var hideSoulweaverSkulls: Boolean = false

	@SerialEntry
	var dungeonMap: DungeonMap = DungeonMap()

	@SerialEntry
	var puzzleSolvers: PuzzleSolvers = PuzzleSolvers()

	@SerialEntry
	var theProfessor: TheProfessor = TheProfessor()

	@SerialEntry
	var livid: Livid = Livid()

	@SerialEntry
	var terminals: Terminals = Terminals()

	@SerialEntry
	var secretWaypoints: SecretWaypoints = SecretWaypoints()

	@SerialEntry
	var mimicMessage: MimicMessage = MimicMessage()

	@SerialEntry
	var doorHighlight: DoorHighlight = DoorHighlight()

	@SerialEntry
	var dungeonScore: DungeonScore = DungeonScore()

	@SerialEntry
	var dungeonChestProfit: DungeonChestProfit = DungeonChestProfit()

	class DungeonMap {

		@SerialEntry
		var enableMap: Boolean = true

		@SerialEntry
		var mapScaling: Float = 1f

		@SerialEntry
		var mapX: Int = 2

		@SerialEntry
		var mapY: Int = 2
	}

	class PuzzleSolvers {

		@SerialEntry
		var solveTicTacToe: Boolean = true

		@SerialEntry
		var solveThreeWeirdos: Boolean = true

		@SerialEntry
		var creeperSolver: Boolean = true

		@SerialEntry
		var solveWaterboard: Boolean = true

		@SerialEntry
		var blazeSolver: Boolean = true

		@SerialEntry
		var solveBoulder: Boolean = true

		@SerialEntry
		var solveIceFill: Boolean = true

		@SerialEntry
		var solveSilverfish: Boolean = true

		@SerialEntry
		var solveTrivia: Boolean = true
	}

	class TheProfessor {

		@SerialEntry
		var fireFreezeStaffTimer: Boolean = true

		@SerialEntry
		var floor3GuardianHealthDisplay: Boolean = true
	}

	class Livid {

		@SerialEntry
		var enableLividColorGlow: Boolean = true

		@SerialEntry
		var enableLividColorText: Boolean = true

		@SerialEntry
		var enableLividColorTitle: Boolean = true

		@SerialEntry
		var lividColorText: String = "The livid color is [color]"
	}

	class Terminals {

		@SerialEntry
		var solveColor: Boolean = true

		@SerialEntry
		var solveOrder: Boolean = true

		@SerialEntry
		var solveStartsWith: Boolean = true

		@SerialEntry
		var blockIncorrectClicks: Boolean = false
	}

	class SecretWaypoints {

		@SerialEntry
		var enableRoomMatching: Boolean = true

		@SerialEntry
		var enableSecretWaypoints: Boolean = true

		@SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@SerialEntry
		var showSecretText: Boolean = true

		@SerialEntry
		var enableEntranceWaypoints: Boolean = true

		@SerialEntry
		var enableSuperboomWaypoints: Boolean = true

		@SerialEntry
		var enableChestWaypoints: Boolean = true

		@SerialEntry
		var enableItemWaypoints: Boolean = true

		@SerialEntry
		var enableBatWaypoints: Boolean = true

		@SerialEntry
		var enableWitherWaypoints: Boolean = true

		@SerialEntry
		var enableLeverWaypoints: Boolean = true

		@SerialEntry
		var enableFairySoulWaypoints: Boolean = true

		@SerialEntry
		var enableStonkWaypoints: Boolean = true

		@SerialEntry
		var enableAotvWaypoints: Boolean = true

		@SerialEntry
		var enablePearlWaypoints: Boolean = true

		@SerialEntry
		var enableDefaultWaypoints: Boolean = true
	}

	class MimicMessage {

		@SerialEntry
		var sendMimicMessage: Boolean = true

		@SerialEntry
		var mimicMessage: String = "Mimic dead!"
	}

	class DoorHighlight {

		@SerialEntry
		var enableDoorHighlight: Boolean = true

		@SerialEntry
		var doorHighlightType: Type = Type.OUTLINED_HIGHLIGHT

		enum class Type {
			HIGHLIGHT,
			OUTLINED_HIGHLIGHT,
			OUTLINE;

			override fun toString(): String {
				return when (this) {
					HIGHLIGHT -> "Highlight"
					OUTLINED_HIGHLIGHT -> "Outlined Highlight"
					OUTLINE -> "Outline"
				}
			}
		}
	}

	class DungeonScore {

		@SerialEntry
		var enableDungeonScore270Message: Boolean = false

		@SerialEntry
		var enableDungeonScore270Title: Boolean = false

		@SerialEntry
		var enableDungeonScore270Sound: Boolean = false

		@SerialEntry
		var dungeonScore270Message: String = "270 Score Reached!"

		@SerialEntry
		var enableDungeonScore300Message: Boolean = true

		@SerialEntry
		var enableDungeonScore300Title: Boolean = true

		@SerialEntry
		var enableDungeonScore300Sound: Boolean = true

		@SerialEntry
		var dungeonScore300Message: String = "300 Score Reached!"

		@SerialEntry
		var enableDungeonCryptsMessage: Boolean = true

		@SerialEntry
		var dungeonCryptsMessageThreshold: Int = 250

		@SerialEntry
		var dungeonCryptsMessage: String = "We only have [crypts] crypts out of 5, we need more!"

		@SerialEntry
		var enableScoreHUD: Boolean = true

		@SerialEntry
		var scoreX: Int = 29

		@SerialEntry
		var scoreY: Int = 134

		@SerialEntry
		var scoreScaling: Float = 1f
	}

	class DungeonChestProfit {

		@SerialEntry
		var enableProfitCalculator: Boolean = true

		@SerialEntry
		var includeKismet: Boolean = false

		@SerialEntry
		var includeEssence: Boolean = true

		@SerialEntry
		var croesusProfit: Boolean = true

		@SerialEntry
		var neutralThreshold: Int = 1000

		@SerialEntry
		var neutralColor: Formatting = Formatting.DARK_GRAY

		@SerialEntry
		var profitColor: Formatting = Formatting.DARK_GREEN

		@SerialEntry
		var lossColor: Formatting = Formatting.RED

		@SerialEntry
		var incompleteColor: Formatting = Formatting.BLUE
	}
}
