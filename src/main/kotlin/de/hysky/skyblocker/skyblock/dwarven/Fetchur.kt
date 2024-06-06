package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Chat
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

object Fetchur : ChatPatternListener("^\\[NPC] Fetchur: (?:its|theyre) ([a-zA-Z, \\-]*)$") {
	public override fun state() = if (SkyblockerConfigManager.config.mining.dwarvenMines.solveFetchur) ChatFilterResult.FILTER else ChatFilterResult.PASS

	public override fun onMatch(message: Text, matcher: Matcher): Boolean {
		TextHandler.info("Original Fetchur message: ${message.string}")
		val riddle = matcher.group(1)
		TextHandler.info("§e[NPC] Fetchur§f: ${answers[riddle] ?: riddle}", Chat)
		return true
	}

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
