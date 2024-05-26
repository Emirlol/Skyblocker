package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

class DeathFilter : ChatPatternListener(" \\u2620 .*") {
	override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.get().chat.hideDeath
	}

	override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		return true
	}
}
