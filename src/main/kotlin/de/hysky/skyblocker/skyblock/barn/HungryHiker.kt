package de.hysky.skyblocker.skyblock.barn

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.sendMessageToBypassEvents
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher

class HungryHiker : ChatPatternListener("^\\[NPC] Hungry Hiker: (The food I want is|(I asked for) food that is) ([a-zA-Z, '\\-]*\\.)$") {
	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.get().otherLocations.barn.solveHungryHiker) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		val client = MinecraftClient.getInstance()
		if (client.player == null) return false
		val foodDescription = matcher!!.group(3)
		val food = foods[foodDescription] ?: return false
		val middlePartOfTheMessageToSend = if (matcher.group(2) != null) matcher.group(2) else matcher.group(1)
		sendMessageToBypassEvents(Text.of("§e[NPC] Hungry Hiker§f: $middlePartOfTheMessageToSend $food."))
		return true
	}

	companion object {
		private val foods: MutableMap<String, String> = HashMap()

		init {
			foods["from a cow."] = Text.translatable("item.minecraft.cooked_beef").string
			foods["meat from a fowl."] = Text.translatable("item.minecraft.cooked_chicken").string
			foods["red on the inside, green on the outside."] = Text.translatable("item.minecraft.melon_slice").string
			foods["a cooked potato."] = Text.translatable("item.minecraft.baked_potato").string
			foods["a stew."] = Text.translatable("item.minecraft.rabbit_stew").string
			foods["a grilled meat."] = Text.translatable("item.minecraft.cooked_porkchop").string
			foods["red and crunchy."] = Text.translatable("item.minecraft.apple").string
			foods["made of wheat."] = Text.translatable("item.minecraft.bread").string
		}
	}
}
