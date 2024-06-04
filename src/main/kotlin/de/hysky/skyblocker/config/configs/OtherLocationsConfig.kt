package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class OtherLocationsConfig {
	@SerialEntry
	var barn: Barn = Barn()

	@SerialEntry
	var rift: Rift = Rift()

	@SerialEntry
	var end: TheEnd = TheEnd()

	@SerialEntry
	var spidersDen: SpidersDen = SpidersDen()

	class Barn {

		@SerialEntry
		var solveHungryHiker: Boolean = true

		@SerialEntry
		var solveTreasureHunter: Boolean = true
	}

	class Rift {

		@SerialEntry
		var mirrorverseWaypoints: Boolean = true

		@SerialEntry
		var blobbercystGlow: Boolean = true

		@SerialEntry
		var enigmaSoulWaypoints: Boolean = false

		@SerialEntry
		var highlightFoundEnigmaSouls: Boolean = true

		@SerialEntry
		var mcGrubberStacks: Int = 0
	}

	class TheEnd {

		@SerialEntry
		var enableEnderNodeHelper: Boolean = true

		@SerialEntry
		var hudEnabled: Boolean = true

		@SerialEntry
		var zealotKillsEnabled: Boolean = true

		@SerialEntry
		var protectorLocationEnabled: Boolean = true

		@SerialEntry
		var waypoint: Boolean = true

		@SerialEntry
		var x: Int = 10

		@SerialEntry
		var y: Int = 10
	}

	class SpidersDen {

		@SerialEntry
		var relics: Relics = Relics()
	}

	class Relics {

		@SerialEntry
		var enableRelicsHelper: Boolean = false

		@SerialEntry
		var highlightFoundRelics: Boolean = true
	}
}
