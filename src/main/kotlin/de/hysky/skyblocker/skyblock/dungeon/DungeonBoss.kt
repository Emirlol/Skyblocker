package de.hysky.skyblocker.skyblock.dungeon

import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

enum class DungeonBoss(private val floor: Int, private val message: String) {
	NONE(-1, ""),
	BONZO(1, "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable."),
	SCARF(2, "[BOSS] Scarf: This is where the journey ends for you, Adventurers."),
	PROFESSOR(3, "[BOSS] The Professor: I was burdened with terrible news recently..."),
	THORN(4, "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!"),
	LIVID(5, "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows."),
	SADAN(6, "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"),
	MAXOR(7, "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!");

	fun floor(): Int {
		return floor
	}

	fun message(): String {
		return message
	}

	val isInBoss: Boolean
		get() = this != NONE

	fun isFloor(floor: Int): Boolean {
		return this.floor == floor
	}

	companion object {
		private val BOSSES: Map<String, DungeonBoss> = Arrays.stream(entries.toTypedArray()).collect(Collectors.toUnmodifiableMap(Function { obj: DungeonBoss -> obj.message() }, Function.identity()))

		@JvmStatic
        fun fromMessage(message: String): DungeonBoss {
			return BOSSES.getOrDefault(message, NONE)
		}
	}
}
