package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

object DeathFilter : ChatPatternListener(" \\u2620 .*") {
	override fun state() = SkyblockerConfigManager.config.chat.hideDeath
	override fun onMatch(message: Text, matcher: Matcher) = true
}
