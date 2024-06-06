package de.hysky.skyblocker.skyblock.barn

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.sendMessageToBypassEvents
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

object HungryHiker : ChatPatternListener("^\\[NPC] Hungry Hiker: (The food I want is|(I asked for) food that is) ([a-zA-Z, '\\-]*\\.)$") {
	private val foods: HashMap<String, String> = hashMapOf(
		"from a cow." to Text.translatable("item.minecraft.cooked_beef").string,
		"meat from a fowl." to Text.translatable("item.minecraft.cooked_chicken").string,
		"red on the inside, green on the outside." to Text.translatable("item.minecraft.melon_slice").string,
		"a cooked potato." to Text.translatable("item.minecraft.baked_potato").string,
		"a stew." to Text.translatable("item.minecraft.rabbit_stew").string,
		"a grilled meat." to Text.translatable("item.minecraft.cooked_porkchop").string,
		"red and crunchy." to Text.translatable("item.minecraft.apple").string,
		"made of wheat." to Text.translatable("item.minecraft.bread").string
	)

	public override fun state() = if (SkyblockerConfigManager.config.otherLocations.barn.solveHungryHiker) ChatFilterResult.FILTER else ChatFilterResult.PASS

	public override fun onMatch(message: Text, matcher: Matcher): Boolean {
		val foodDescription = matcher.group(3)
		val food = foods[foodDescription] ?: return false
		val middlePartOfTheMessageToSend = matcher.group(2) ?: matcher.group(1)
		sendMessageToBypassEvents(Text.of("§e[NPC] Hungry Hiker§f: $middlePartOfTheMessageToSend $food."))
		return true
	}
}
