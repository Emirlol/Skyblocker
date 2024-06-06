package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher

object AutopetFilter : ChatPatternListener("^Autopet equipped your .*! VIEW RULE$") {
	public override fun onMatch(message: Text, matcher: Matcher): Boolean {
		if (SkyblockerConfigManager.config.chat.hideAutopet == ChatFilterResult.ACTION_BAR) {
			MinecraftClient.getInstance().player?.sendMessage(Text.literal(message.string.replace("VIEW RULE", "")), true)
		}
		return true
	}

	public override fun state() = if (SkyblockerConfigManager.config.chat.hideAutopet == ChatFilterResult.ACTION_BAR) ChatFilterResult.FILTER
	else SkyblockerConfigManager.config.chat.hideAutopet
}