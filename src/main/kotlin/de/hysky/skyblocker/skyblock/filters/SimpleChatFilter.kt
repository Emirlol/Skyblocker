package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

abstract class SimpleChatFilter(pattern: String?) : ChatPatternListener(pattern) {
	override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		return true
	}
}
