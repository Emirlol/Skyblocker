package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import org.intellij.lang.annotations.Language
import java.util.regex.Matcher

abstract class SimpleChatFilter(@Language("RegExp") pattern: String) : ChatPatternListener(pattern) {
	override fun onMatch(message: Text, matcher: Matcher) = true
}
