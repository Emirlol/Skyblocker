package de.hysky.skyblocker.skyblock.dungeon

import com.google.gson.JsonObject
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.DungeonsConfig
import de.hysky.skyblocker.config.configs.DungeonsConfig.MimicMessage
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr.regexAt
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ProfileUtils.updateProfile
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.Utils.mayor
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min


object DungeonScore {
	private val SCORE_CONFIG: DungeonsConfig.DungeonScore = SkyblockerConfigManager.config.dungeons.dungeonScore
	private val MIMIC_MESSAGE_CONFIG: MimicMessage = SkyblockerConfigManager.config.dungeons.mimicMessage
	private const val LOGGER_PREFIX = "[Dungeon Score]"

	//Scoreboard patterns
	private val CLEARED_PATTERN: Pattern = Pattern.compile("Cleared: (?<cleared>\\d+)%.*")
	private val FLOOR_PATTERN: Pattern = Pattern.compile(".*?(?=T)The Catacombs \\((?<floor>[EFM]\\D*\\d*)\\)")

	//Playerlist patterns
	private val SECRETS_PATTERN: Pattern = Pattern.compile("Secrets Found: (?<secper>\\d+\\.?\\d*)%")
	private val PUZZLES_PATTERN: Pattern = Pattern.compile(".+?(?=:): \\[(?<state>.)](?: \\(\\w+\\))?")
	private val PUZZLE_COUNT_PATTERN: Pattern = Pattern.compile("Puzzles: \\((?<count>\\d+)\\)")
	private val CRYPTS_PATTERN: Pattern = Pattern.compile("Crypts: (?<crypts>\\d+)")
	private val COMPLETED_ROOMS_PATTERN: Pattern = Pattern.compile(" *Completed Rooms: (?<rooms>\\d+)")

	//Chat patterns
	private val DEATHS_PATTERN: Pattern = Pattern.compile(" \\u2620 (?<whodied>\\S+) .*")
	private val MIMIC_PATTERN: Pattern = Pattern.compile(".*?(?:Mimic dead!?|Mimic Killed!|\\\$SKYTILS-DUNGEON-SCORE-MIMIC\\$|\\Q" + MIMIC_MESSAGE_CONFIG.mimicMessage + "\\E)$")

	//Other patterns
	private val MIMIC_FLOORS_PATTERN = Regex("[FM][67]")

	private var floorRequirement: FloorRequirement? = null
	private var currentFloor: String? = null
	private var isCurrentFloorEntrance = false

	//Feel free to refactor this if you can think of a better name.
	var isMimicOnCurrentFloor: Boolean = false
		private set
	private var sentCrypts = false
	private var sent270 = false
	private var sent300 = false
	private var mimicKilled = false
	var isDungeonStarted: Boolean = false
		private set
	private var isMayorPaul = false
	private var firstDeathHasSpiritPet = false
	private var bloodRoomCompleted = false
	private var startingTime: Long = 0
	private var puzzleCount = 0
	private var deathCount = 0
	var score: Int = 0
		private set

	fun init() {
		Scheduler.scheduleCyclic(20) { tick() }
		ClientPlayConnectionEvents.JOIN.register{ _, _, _ -> reset() }
		ClientReceiveMessageEvents.GAME.register { message: Text, overlay: Boolean ->
			if (overlay || !isInDungeons) return@register
			val str = message.string
			if (!isDungeonStarted) {
				checkMessageForMort(str)
			} else {
				checkMessageForDeaths(str)
				checkMessageForWatcher(str)
				if (isMimicOnCurrentFloor) checkMessageForMimic(str) //Only called when the message is not cancelled & isn't on the action bar, complementing MimicFilter
			}
		}
		ClientReceiveMessageEvents.GAME_CANCELED.register{ message: Text, overlay: Boolean ->
			if (overlay || !isInDungeons || !isDungeonStarted) return@register
			checkMessageForDeaths(message.string)
		}
	}

	fun tick() {
		val client = MinecraftClient.getInstance()
		if (!isInDungeons || client.player == null) {
			reset()
			return
		}
		if (!isDungeonStarted) return

		score = calculateScore()
		if (!sent270 && !sent300 && score >= 270 && score < 300) {
			if (SCORE_CONFIG.enableDungeonScore270Message) {
				MessageScheduler.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().string + SCORE_CONFIG.dungeonScore270Message.replace("\\[score]".toRegex(), "270"))
			}
			if (SCORE_CONFIG.enableDungeonScore270Title) {
				client.inGameHud.setDefaultTitleFade()
				client.inGameHud.setTitle(Constants.PREFIX.get().append(SCORE_CONFIG.dungeonScore270Message.replace("\\[score]".toRegex(), "270")))
			}
			if (SCORE_CONFIG.enableDungeonScore270Sound) {
				client.player!!.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f)
			}
			sent270 = true
		}

		val crypts = crypts
		if (!sentCrypts && score >= SCORE_CONFIG.dungeonCryptsMessageThreshold && crypts < 5) {
			if (SCORE_CONFIG.enableDungeonCryptsMessage) {
				MessageScheduler.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().string + SCORE_CONFIG.dungeonCryptsMessage.replace("\\[crypts]".toRegex(), crypts.toString()))
			}
			sentCrypts = true
		}

		if (!sent300 && score >= 300) {
			if (SCORE_CONFIG.enableDungeonScore300Message) {
				MessageScheduler.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().string + SCORE_CONFIG.dungeonScore300Message.replace("\\[score]".toRegex(), "300"))
			}
			if (SCORE_CONFIG.enableDungeonScore300Title) {
				client.inGameHud.setDefaultTitleFade()
				client.inGameHud.setTitle(Constants.PREFIX.get().append(SCORE_CONFIG.dungeonScore300Message.replace("\\[score]".toRegex(), "300")))
			}
			if (SCORE_CONFIG.enableDungeonScore300Sound) {
				client.player!!.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f)
			}
			sent300 = true
		}
	}

	private fun reset() {
		floorRequirement = null
		currentFloor = ""
		isCurrentFloorEntrance = false
		isMimicOnCurrentFloor = false
		sentCrypts = false
		sent270 = false
		sent300 = false
		mimicKilled = false
		isDungeonStarted = false
		isMayorPaul = false
		firstDeathHasSpiritPet = false
		bloodRoomCompleted = false
		startingTime = 0L
		puzzleCount = 0
		deathCount = 0
		score = 0
	}

	private fun onDungeonStart() {
		setCurrentFloor()
		currentFloor ?: return
		isDungeonStarted = true
		puzzleCount = getPuzzleCount()
		isMayorPaul = mayor == "Paul"
		startingTime = System.currentTimeMillis()
		floorRequirement = FloorRequirement.valueOf(currentFloor!!)
		isMimicOnCurrentFloor = MIMIC_FLOORS_PATTERN.matches(currentFloor!!)
		if (currentFloor == "E") isCurrentFloorEntrance = true
	}

	private fun calculateScore(): Int {
		if (isCurrentFloorEntrance) return Math.round(calculateTimeScore() * 0.7f) + Math.round(calculateExploreScore() * 0.7f) + Math.round(calculateSkillScore() * 0.7f) + Math.round(calculateBonusScore() * 0.7f)
		return calculateTimeScore() + calculateExploreScore() + calculateSkillScore() + calculateBonusScore()
	}

	private fun calculateSkillScore(): Int {
		val totalRooms = totalRooms //Save in a variable to not recalculate
		return (20 + max(((if (totalRooms != 0) (80.0 * (completedRooms + extraCompletedRooms) / totalRooms).toInt() else 0) - puzzlePenalty - deathScorePenalty).toDouble(), 0.0)).toInt() //Can't go below 20 skill score
	}

	private fun calculateExploreScore(): Int {
		val totalRooms = totalRooms //Save in a variable to not recalculate
		val completedRoomScore = if (totalRooms != 0) (60.0 * (completedRooms + extraCompletedRooms) / totalRooms).toInt() else 0
		val secretsScore = (40 * min(floorRequirement!!.percentage.toDouble(), secretsPercentage) / floorRequirement!!.percentage).toInt()
		return max((completedRoomScore + secretsScore).toDouble(), 0.0).toInt()
	}

	private fun calculateTimeScore(): Int {
		val score = 100
		val timeSpent = (System.currentTimeMillis() - startingTime).toInt() / 1000
		if (timeSpent < floorRequirement!!.timeLimit) return score

		val timePastRequirement = ((timeSpent - floorRequirement!!.timeLimit).toDouble() / floorRequirement!!.timeLimit) * 100
		if (timePastRequirement < 20) return score - timePastRequirement.toInt() / 2
		if (timePastRequirement < 40) return score - (10 + (timePastRequirement - 20) / 4).toInt()
		if (timePastRequirement < 50) return score - (15 + (timePastRequirement - 40) / 5).toInt()
		if (timePastRequirement < 60) return score - (17 + (timePastRequirement - 50) / 6).toInt()
		return max((score - (18 + (2.0 / 3.0) + (timePastRequirement - 60) / 7).toInt()).toDouble(), 0.0).toInt() //This can theoretically go down to -20 if the time limit is one of the lower ones like 480, but individual score categories can't go below 0
	}

	private fun calculateBonusScore(): Int {
		val paulScore = if (isMayorPaul) 10 else 0
		val cryptsScore = min(crypts.toDouble(), 5.0).toInt()
		var mimicScore = if (mimicKilled) 2 else 0
		if (secretsPercentage >= 100 && isMimicOnCurrentFloor) mimicScore = 2 //If mimic kill is not announced but all secrets are found, mimic must've been killed

		return paulScore + cryptsScore + mimicScore
	}

	fun isEntityMimic(entity: Entity): Boolean {
		if (!isInDungeons || !isMimicOnCurrentFloor || entity !is ZombieEntity || !entity.isBaby) return false
		try {
			return entity.armorItems.all { it.isEmpty }
		} catch (e: Exception) {
			TextHandler.error("$LOGGER_PREFIX Failed to check if entity is a mimic!", e)
			return false
		}
	}

	@JvmStatic
	fun handleEntityDeath(entity: Entity) {
		if (mimicKilled) return
		if (!isEntityMimic(entity)) return
		if (MIMIC_MESSAGE_CONFIG.sendMimicMessage) MessageScheduler.sendMessageAfterCooldown(MIMIC_MESSAGE_CONFIG.mimicMessage)
		mimicKilled = true
	}

	fun onMimicKill() {
		mimicKilled = true
	}

	private val totalRooms: Int
		//This is not very accurate at the beginning of the dungeon since clear percentage is rounded to the closest integer, so at lower percentages its effect on the result is quite high.
		get() = Math.round(completedRooms / clearPercentage).toInt()

	private val completedRooms: Int
		get() {
			val matcher = regexAt(43, COMPLETED_ROOMS_PATTERN)
			return matcher?.group("rooms")?.toInt() ?: 0
		}

	private val extraCompletedRooms: Int
		//This is needed for calculating the score before going in the boss room & completing the blood room, so we have the result sooner
		get() {
			if (!bloodRoomCompleted) return if (isCurrentFloorEntrance) 1 else 2
			if (!DungeonManager.isInBoss && !isCurrentFloorEntrance) return 1
			return 0
		}

	private val clearPercentage: Double
		get() {
			for (sidebarLine in Utils.STRING_SCOREBOARD) {
				val clearMatcher = CLEARED_PATTERN.matcher(sidebarLine)
				if (!clearMatcher.matches()) continue
				return clearMatcher.group("cleared").toDouble() / 100.0
			}
			TextHandler.error("$LOGGER_PREFIX Clear pattern doesn't match!")
			return 0.0
		}

	private val deathScorePenalty: Int
		//Score might fluctuate when the first death has spirit pet as the boolean will be set to true after getting a response from the api, which might take a while
		get() = deathCount * 2 - (if (firstDeathHasSpiritPet) 1 else 0)

	private fun getPuzzleCount(): Int {
		val matcher = regexAt(47, PUZZLE_COUNT_PATTERN)
		return matcher?.group("count")?.toInt() ?: 0
	}

	private val puzzlePenalty: Int
		get() {
			var incompletePuzzles = 0
			for (index in 0 until puzzleCount) {
				val puzzleMatcher = regexAt(48 + index, PUZZLES_PATTERN) ?: break
				if (puzzleMatcher.group("state").matches("[✖✦]".toRegex())) incompletePuzzles++
			}
			return incompletePuzzles * 10
		}

	private val secretsPercentage: Double
		get() {
			val matcher = regexAt(44, SECRETS_PATTERN)
			return matcher?.group("secper")?.toDouble() ?: 0.0
		}

	private val crypts: Int
		get() {
			var matcher = regexAt(33, CRYPTS_PATTERN)
			if (matcher == null) matcher = regexAt(32, CRYPTS_PATTERN) //If class milestone 9 is reached, crypts goes up by 1

			return matcher?.group("crypts")?.toInt() ?: 0
		}

	fun hasSpiritPet(player: JsonObject?, name: String?): Boolean {
		player ?: return false
		try {
			for (pet in player.getAsJsonObject("pets_data").getAsJsonArray("pets")) {
				if (pet.asJsonObject["type"].asString != "SPIRIT") continue
				if (pet.asJsonObject["tier"].asString != "LEGENDARY") continue

				return true
			}
		} catch (e: Exception) {
			TextHandler.error("$LOGGER_PREFIX Spirit pet lookup by name failed! Name: $name", e)
		}
		return false
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun checkMessageForDeaths(message: String) {
		if (!message.startsWith("\u2620", 1)) return
		val matcher = DEATHS_PATTERN.matcher(message)
		if (!matcher.matches()) return
		deathCount++
		if (deathCount > 1) return
		val whoDied = matcher.group("whodied").let {
			if (it == "You") MinecraftClient.getInstance().session.username
			else it
		}
		updateProfile(whoDied).let { deferred ->
			deferred.invokeOnCompletion {
				if (it !is Exception) firstDeathHasSpiritPet = hasSpiritPet(deferred.getCompleted(), whoDied)
			}
		}
	}

	private fun checkMessageForWatcher(message: String) {
		if (message == "[BOSS] The Watcher: You have proven yourself. You may pass.") bloodRoomCompleted = true
	}

	private fun checkMessageForMort(message: String) {
		if (message == "§e[NPC] §bMort§f: You should find it useful if you get lost.") onDungeonStart()
	}

	private fun checkMessageForMimic(message: String) {
		if (!MIMIC_PATTERN.matcher(message).matches()) return
		onMimicKill()
	}

	private fun setCurrentFloor() {
		for (sidebarLine in Utils.STRING_SCOREBOARD) {
			val floorMatcher = FLOOR_PATTERN.matcher(sidebarLine)
			if (!floorMatcher.matches()) continue
			currentFloor = floorMatcher.group("floor")
			return
		}
		TextHandler.error("$LOGGER_PREFIX Floor pattern doesn't match!")
	}

	private enum class FloorRequirement(val percentage: Int, val timeLimit: Int) {
		E(30, 1200),
		F1(30, 600),
		F2(40, 600),
		F3(50, 600),
		F4(60, 720),
		F5(70, 600),
		F6(85, 720),
		F7(100, 840),
		M1(100, 480),
		M2(100, 480),
		M3(100, 480),
		M4(100, 480),
		M5(100, 480),
		M6(100, 600),
		M7(100, 840)
	}
}

