package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Matcher

class Fetchur : ChatPatternListener("^\\[NPC] Fetchur: (?:its|theyre) ([a-zA-Z, \\-]*)$") {
	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.get().mining.dwarvenMines.solveFetchur) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		val client = MinecraftClient.getInstance()
		if (client.player == null) return false
		LOGGER.info("Original Fetchur message: {}", message!!.string)
		val riddle = matcher!!.group(1)
		val answer = answers.getOrDefault(riddle, riddle)
		client.player!!.sendMessage(Text.of("§e[NPC] Fetchur§f: $answer"), false)
		return true
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Fetchur::class.java)

		private val answers: MutableMap<String, String> = HashMap()

		init {
			answers["yellow and see through"] = Text.translatable("block.minecraft.yellow_stained_glass").string
			answers["circular and sometimes moves"] = Text.translatable("item.minecraft.compass").string
			answers["expensive minerals"] = "Mithril"
			answers["useful during celebrations"] = Text.translatable("item.minecraft.firework_rocket").string
			answers["hot and gives energy"] = "Cheap / Decent / Black Coffee"
			answers["tall and can be opened"] = String.format(
				"%s / %s",
				Text.translatable("block.minecraft.oak_door").string,
				Text.translatable("block.minecraft.iron_door").string
			)
			answers["brown and fluffy"] = Text.translatable("item.minecraft.rabbit_foot").string
			answers["explosive but more than usual"] = "Superboom TNT"
			answers["wearable and grows"] = Text.translatable("block.minecraft.pumpkin").string
			answers["shiny and makes sparks"] = Text.translatable("item.minecraft.flint_and_steel").string
			answers["green and some dudes trade stuff for it"] = Text.translatable("item.minecraft.emerald").string
			answers["red and soft"] = Text.translatable("block.minecraft.red_wool").string
		}
	}
}
