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

	private val answers = hashMapOf(
		"yellow and see through" to Text.translatable("block.minecraft.yellow_stained_glass").string,
		"circular and sometimes moves" to Text.translatable("item.minecraft.compass").string,
		"expensive minerals" to "Mithril",
		"useful during celebrations" to Text.translatable("item.minecraft.firework_rocket").string,
		"hot and gives energy" to "Cheap / Decent / Black Coffee",
		"tall and can be opened" to String.format("%s / %s", Text.translatable("block.minecraft.oak_door").string, Text.translatable("block.minecraft.iron_door").string),
		"brown and fluffy" to Text.translatable("item.minecraft.rabbit_foot").string,
		"explosive but more than usual" to "Superboom TNT",
		"wearable and grows" to Text.translatable("block.minecraft.pumpkin").string,
		"shiny and makes sparks" to Text.translatable("item.minecraft.flint_and_steel").string,
		"green and some dudes trade stuff for it" to Text.translatable("item.minecraft.emerald").string,
		"red and soft" to Text.translatable("block.minecraft.red_wool").string
	)
}
