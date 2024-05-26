package de.hysky.skyblocker.skyblock.barn

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher

class TreasureHunter : ChatPatternListener("^\\[NPC] Treasure Hunter: ([a-zA-Z, '\\-\\.]*)$") {
	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.get().otherLocations.barn.solveTreasureHunter) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		val client = MinecraftClient.getInstance()
		if (client.player == null) return false
		val hint = matcher!!.group(1)
		val location = locations[hint] ?: return false
		client.player!!.sendMessage(Text.of("§e[NPC] Treasure Hunter§f: Go mine around $location."), false)
		return true
	}

	companion object {
		private val locations: MutableMap<String, String> = HashMap()

		init {
			locations["There's a treasure chest somewhere in a small cave in the gorge."] = "258 70 -492"
			locations["I was in the desert earlier, and I saw something near a red sand rock."] = "357 82 -319"
			locations["There's this guy who collects animals to experiment on, I think I saw something near his house."] = "259 184 -564"
			locations["There's a small house in the gorge, I saw some treasure near there."] = "297 87 -562"
			locations["There's this guy who says he has the best sheep in the world. I think I saw something around his hut."] = "392 85 -372"
			locations["I spotted something by an odd looking mushroom on one of the ledges in the Mushroom Gorge, you should check it out."] = "305 73 -557"
			locations["There are some small ruins out in the desert, might want to check them out."] = "320 102 -471"
			locations["Some dirt was kicked up by the water pool in the overgrown Mushroom Cave. Have a look over there."] = "234 56 -410"
			locations["There are some old stone structures in the Mushroom Gorge, give them a look."] = "223 54 -503"
			locations["In the Mushroom Gorge where blue meets the ceiling and floor, you will find what you are looking for."] = "205 42 -527"
			locations["There was a haystack with a crop greener than usual around it, I think there is something near there."] = "334 82 -389"
			locations["There's a single piece of tall grass growing in the desert, I saw something there."] = "283 76 -363"
			locations["I saw some treasure by a cow skull near the village."] = "141 77 -397"
			locations["Near a melon patch inside a tunnel in the mountain I spotted something."] = "257 100 -569"
			locations["I saw something near a farmer's cart, you should check it out."] = "155 90 -591"
			locations["I remember there was a stone pillar made only of cobblestone in the oasis, could be something there."] = "122 66 -409"
			locations["I thought I saw something near the smallest stone pillar in the oasis."] = "94 65 -455"
			locations["I found something by a mossy stone pillar in the oasis, you should take a look."] = "179 93 -537"
			locations["Down in the glowing Mushroom Cave, there was a weird looking mushroom, check it out."] = "182 44 -451"
			locations["Something caught my eye by the red sand near the bridge over the gorge."] = "306 105 -489"
			locations["I seem to recall seeing something near the well in the village."] = "170 77 -375"
			locations["I was down near the lower oasis yesterday, I think I saw something under the bridge."] = "142 69 -448"
			locations["I was at the upper oasis today, I recall seeing something on the cobblestone stepping stones."] = "188 77 -459"
		}
	}
}
