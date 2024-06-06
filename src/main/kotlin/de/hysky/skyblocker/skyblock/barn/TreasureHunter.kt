package de.hysky.skyblocker.skyblock.barn

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher

object TreasureHunter : ChatPatternListener("^\\[NPC] Treasure Hunter: ([a-zA-Z, '\\-\\.]*)$") {
	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.config.otherLocations.barn.solveTreasureHunter) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text, matcher: Matcher): Boolean {
		val player = MinecraftClient.getInstance().player ?: return false
		val hint = matcher.group(1)
		val location = locations[hint] ?: return false
		player.sendMessage(Text.of("§e[NPC] Treasure Hunter§f: Go mine around $location."), false)
		return true
	}

	private val locations: MutableMap<String, String> = hashMapOf(
		"There's a treasure chest somewhere in a small cave in the gorge." to "258 70 -492",
		"I was in the desert earlier, and I saw something near a red sand rock." to "357 82 -319",
		"There's this guy who collects animals to experiment on, I think I saw something near his house." to "259 184 -564",
		"There's a small house in the gorge, I saw some treasure near there." to "297 87 -562",
		"There's this guy who says he has the best sheep in the world. I think I saw something around his hut." to "392 85 -372",
		"I spotted something by an odd looking mushroom on one of the ledges in the Mushroom Gorge, you should check it out." to "305 73 -557",
		"There are some small ruins out in the desert, might want to check them out." to "320 102 -471",
		"Some dirt was kicked up by the water pool in the overgrown Mushroom Cave. Have a look over there." to "234 56 -410",
		"There are some old stone structures in the Mushroom Gorge, give them a look." to "223 54 -503",
		"In the Mushroom Gorge where blue meets the ceiling and floor, you will find what you are looking for." to "205 42 -527",
		"There was a haystack with a crop greener than usual around it, I think there is something near there." to "334 82 -389",
		"There's a single piece of tall grass growing in the desert, I saw something there." to "283 76 -363",
		"I saw some treasure by a cow skull near the village." to "141 77 -397",
		"Near a melon patch inside a tunnel in the mountain I spotted something." to "257 100 -569",
		"I saw something near a farmer's cart, you should check it out." to "155 90 -591",
		"I remember there was a stone pillar made only of cobblestone in the oasis, could be something there." to "122 66 -409",
		"I thought I saw something near the smallest stone pillar in the oasis." to "94 65 -455",
		"I found something by a mossy stone pillar in the oasis, you should take a look." to "179 93 -537",
		"Down in the glowing Mushroom Cave, there was a weird looking mushroom, check it out." to "182 44 -451",
		"Something caught my eye by the red sand near the bridge over the gorge." to "306 105 -489",
		"I seem to recall seeing something near the well in the village." to "170 77 -375",
		"I was down near the lower oasis yesterday, I think I saw something under the bridge." to "142 69 -448",
		"I was at the upper oasis today, I recall seeing something on the cobblestone stepping stones." to "188 77 -459",
	)
}
