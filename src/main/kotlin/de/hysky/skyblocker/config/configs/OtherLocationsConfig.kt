package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class OtherLocationsConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var barn: Barn = Barn()

	@kotlin.jvm.JvmField
	@SerialEntry
	var rift: Rift = Rift()

	@kotlin.jvm.JvmField
	@SerialEntry
	var end: TheEnd = TheEnd()

	@kotlin.jvm.JvmField
	@SerialEntry
	var spidersDen: SpidersDen = SpidersDen()

	class Barn {
		@kotlin.jvm.JvmField
		@SerialEntry
		var solveHungryHiker: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var solveTreasureHunter: Boolean = true
	}

	class Rift {
		@kotlin.jvm.JvmField
		@SerialEntry
		var mirrorverseWaypoints: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var blobbercystGlow: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enigmaSoulWaypoints: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightFoundEnigmaSouls: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var mcGrubberStacks: Int = 0
	}

	class TheEnd {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableEnderNodeHelper: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var hudEnabled: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var zealotKillsEnabled: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var protectorLocationEnabled: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var waypoint: Boolean = true

		@SerialEntry
		var x: Int = 10

		@SerialEntry
		var y: Int = 10
	}

	class SpidersDen {
		@kotlin.jvm.JvmField
		@SerialEntry
		var relics: Relics = Relics()
	}

	class Relics {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableRelicsHelper: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var highlightFoundRelics: Boolean = true
	}
}
