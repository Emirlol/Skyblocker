package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.util.Formatting

class DungeonsConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var fancyPartyFinder: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var croesusHelper: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var playerSecretsTracker: Boolean = false

	@kotlin.jvm.JvmField
	@SerialEntry
	var starredMobGlow: Boolean = false

	@kotlin.jvm.JvmField
	@SerialEntry
	var starredMobBoundingBoxes: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var allowDroppingProtectedItems: Boolean = false

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideSoulweaverSkulls: Boolean = false

	@kotlin.jvm.JvmField
	@SerialEntry
	var dungeonMap: DungeonMap = DungeonMap()

	@kotlin.jvm.JvmField
	@SerialEntry
	var puzzleSolvers: PuzzleSolvers = PuzzleSolvers()

	@kotlin.jvm.JvmField
	@SerialEntry
	var theProfessor: TheProfessor = TheProfessor()

	@kotlin.jvm.JvmField
	@SerialEntry
	var livid: Livid = Livid()

	@kotlin.jvm.JvmField
	@SerialEntry
	var terminals: Terminals = Terminals()

	@kotlin.jvm.JvmField
	@SerialEntry
	var secretWaypoints: SecretWaypoints = SecretWaypoints()

	@kotlin.jvm.JvmField
	@SerialEntry
	var mimicMessage: MimicMessage = MimicMessage()

	@kotlin.jvm.JvmField
	@SerialEntry
	var doorHighlight: DoorHighlight = DoorHighlight()

	@kotlin.jvm.JvmField
	@SerialEntry
	var dungeonScore: DungeonScore = DungeonScore()

	@kotlin.jvm.JvmField
	@SerialEntry
	var dungeonChestProfit: DungeonChestProfit = DungeonChestProfit()

	class DungeonMap {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableMap: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var mapScaling: Float = 1f

		@SerialEntry
		var mapX: Int = 2

		@SerialEntry
		var mapY: Int = 2
	}

	class PuzzleSolvers {
		@kotlin.jvm.JvmField
		@SerialEntry
		var solveTicTacToe: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveThreeWeirdos: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var creeperSolver: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveWaterboard: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var blazeSolver: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveBoulder: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveIceFill: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveSilverfish: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveTrivia: Boolean = true
	}

	class TheProfessor {
		@kotlin.jvm.JvmField
		@SerialEntry
		var fireFreezeStaffTimer: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var floor3GuardianHealthDisplay: Boolean = true
	}

	class Livid {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableLividColorGlow: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableLividColorText: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableLividColorTitle: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var lividColorText: String = "The livid color is [color]"
	}

	class Terminals {
		@kotlin.jvm.JvmField
		@SerialEntry
		var solveColor: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveOrder: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveStartsWith: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var blockIncorrectClicks: Boolean = false
	}

	class SecretWaypoints {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableRoomMatching: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableSecretWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT

		@kotlin.jvm.JvmField
		@SerialEntry
		var showSecretText: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableEntranceWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableSuperboomWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableChestWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableItemWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableBatWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableWitherWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableLeverWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableFairySoulWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableStonkWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableAotvWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enablePearlWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDefaultWaypoints: Boolean = true
	}

	class MimicMessage {
		@kotlin.jvm.JvmField
		@SerialEntry
		var sendMimicMessage: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var mimicMessage: String = "Mimic dead!"
	}

	class DoorHighlight {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDoorHighlight: Boolean = true

		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore270Message: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore270Title: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore270Sound: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var dungeonScore270Message: String = "270 Score Reached!"

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore300Message: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore300Title: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonScore300Sound: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var dungeonScore300Message: String = "300 Score Reached!"

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableDungeonCryptsMessage: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var dungeonCryptsMessageThreshold: Int = 250

		@kotlin.jvm.JvmField
		@SerialEntry
		var dungeonCryptsMessage: String = "We only have [crypts] crypts out of 5, we need more!"

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableScoreHUD: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var scoreX: Int = 29

		@kotlin.jvm.JvmField
		@SerialEntry
		var scoreY: Int = 134

		@kotlin.jvm.JvmField
		@SerialEntry
		var scoreScaling: Float = 1f
	}

	class DungeonChestProfit {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableProfitCalculator: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var includeKismet: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var includeEssence: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var croesusProfit: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var neutralThreshold: Int = 1000

		@kotlin.jvm.JvmField
		@SerialEntry
		var neutralColor: Formatting = Formatting.DARK_GRAY

		@kotlin.jvm.JvmField
		@SerialEntry
		var profitColor: Formatting = Formatting.DARK_GREEN

		@kotlin.jvm.JvmField
		@SerialEntry
		var lossColor: Formatting = Formatting.RED

		@kotlin.jvm.JvmField
		@SerialEntry
		var incompleteColor: Formatting = Formatting.BLUE
	}
}
