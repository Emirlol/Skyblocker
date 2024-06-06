package de.hysky.skyblocker.skyblock.dungeon.puzzle

import com.mojang.logging.LogUtils
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.waypoint.FairySouls.getFairySoulsSize
import de.hysky.skyblocker.skyblock.waypoint.FairySouls.runAsyncAfterFairySoulsLoad
import de.hysky.skyblocker.utils.Utils.sendMessageToBypassEvents
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import java.util.*
import java.util.regex.Matcher

class Trivia : ChatPatternListener("^ +(?:([A-Za-z,' ]*\\?)| ([ⓐⓑⓒ]) ([a-zA-Z0-9 ]+))$") {
	private var solutions = emptyList<String>()

	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveTrivia) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		val riddle = matcher!!.group(3)
		if (riddle != null) {
			if (!solutions.contains(riddle)) {
				val player = MinecraftClient.getInstance().player
				if (player != null) sendMessageToBypassEvents(Text.of("     " + Formatting.GOLD + matcher.group(2) + Formatting.RED + " " + riddle))
				return player != null
			}
		} else updateSolutions(matcher.group(0))
		return false
	}

	private fun updateSolutions(question: String) {
		try {
			val trimmedQuestion = question.trim { it <= ' ' }
			if (trimmedQuestion == "What SkyBlock year is it?") {
				val currentTime = System.currentTimeMillis() / 1000L
				val diff = currentTime - 1560276000
				val year = (diff / 446400 + 1).toInt()
				solutions = listOf("Year $year")
			} else {
				val questionAnswers = answers[trimmedQuestion]
				if (questionAnswers != null) solutions = Arrays.asList(*questionAnswers)
			}
		} catch (e: Exception) { //Hopefully the solver doesn't go south
			LOGGER.error("[Skyblocker] Failed to update the Trivia puzzle answers!", e)
		}
	}

	companion object {
		private val LOGGER: Logger = LogUtils.getLogger()
		private val answers: MutableMap<String, Array<String>> = Collections.synchronizedMap(HashMap())

		init {
			answers["What is the status of The Watcher?"] = arrayOf("Stalker")
			answers["What is the status of Bonzo?"] = arrayOf("New Necromancer")
			answers["What is the status of Scarf?"] = arrayOf("Apprentice Necromancer")
			answers["What is the status of The Professor?"] = arrayOf("Professor")
			answers["What is the status of Thorn?"] = arrayOf("Shaman Necromancer")
			answers["What is the status of Livid?"] = arrayOf("Master Necromancer")
			answers["What is the status of Sadan?"] = arrayOf("Necromancer Lord")
			answers["What is the status of Maxor?"] = arrayOf("The Wither Lords")
			answers["What is the status of Goldor?"] = arrayOf("The Wither Lords")
			answers["What is the status of Storm?"] = arrayOf("The Wither Lords")
			answers["What is the status of Necron?"] = arrayOf("The Wither Lords")
			answers["What is the status of Maxor, Storm, Goldor, and Necron?"] = arrayOf("The Wither Lords")
			answers["Which brother is on the Spider's Den?"] = arrayOf("Rick")
			answers["What is the name of Rick's brother?"] = arrayOf("Pat")
			answers["What is the name of the Painter in the Hub?"] = arrayOf("Marco")
			answers["What is the name of the person that upgrades pets?"] = arrayOf("Kat")
			answers["What is the name of the lady of the Nether?"] = arrayOf("Elle")
			answers["Which villager in the Village gives you a Rogue Sword?"] = arrayOf("Jamie")
			answers["How many unique minions are there?"] = arrayOf("59 Minions")
			answers["Which of these enemies does not spawn in the Spider's Den?"] = arrayOf("Zombie Spider", "Cave Spider", "Wither Skeleton", "Dashing Spooder", "Broodfather", "Night Spider")
			answers["Which of these monsters only spawns at night?"] = arrayOf("Zombie Villager", "Ghast")
			answers["Which of these is not a dragon in The End?"] = arrayOf("Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon", "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon")
			runAsyncAfterFairySoulsLoad {
				answers["How many total Fairy Souls are there?"] = getFairySoulsSizeString(null)
				answers["How many Fairy Souls are there in Spider's Den?"] = getFairySoulsSizeString("combat_1")
				answers["How many Fairy Souls are there in The End?"] = getFairySoulsSizeString("combat_3")
				answers["How many Fairy Souls are there in The Farming Islands?"] = getFairySoulsSizeString("farming_1")
				answers["How many Fairy Souls are there in Crimson Isle?"] = getFairySoulsSizeString("crimson_isle")
				answers["How many Fairy Souls are there in The Park?"] = getFairySoulsSizeString("foraging_1")
				answers["How many Fairy Souls are there in Jerry's Workshop?"] = getFairySoulsSizeString("winter")
				answers["How many Fairy Souls are there in Hub?"] = getFairySoulsSizeString("hub")
				answers["How many Fairy Souls are there in The Hub?"] = getFairySoulsSizeString("hub")
				answers["How many Fairy Souls are there in Deep Caverns?"] = getFairySoulsSizeString("mining_2")
				answers["How many Fairy Souls are there in Gold Mine?"] = getFairySoulsSizeString("mining_1")
				answers["How many Fairy Souls are there in Dungeon Hub?"] = getFairySoulsSizeString("dungeon_hub")
			}
		}

		private fun getFairySoulsSizeString(location: String?): Array<String> {
			return arrayOf("%d Fairy Souls".formatted(getFairySoulsSize(location)))
		}
	}
}
