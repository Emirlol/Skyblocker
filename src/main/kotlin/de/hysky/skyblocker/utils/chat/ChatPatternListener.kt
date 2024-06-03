package de.hysky.skyblocker.utils.chat

import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class ChatPatternListener(pattern: String) : ChatMessageListener {
    val pattern: Pattern = Pattern.compile(pattern)

	override fun onMessage(message: Text, asString: String): ChatFilterResult {
		val state = state()
		if (state == ChatFilterResult.PASS) return ChatFilterResult.PASS
		val m = pattern.matcher(asString)
		if (m.matches() && onMatch(message, m) && state != null) {
			return state
		}
		return ChatFilterResult.PASS
	}

	protected abstract fun state(): ChatFilterResult?

	protected abstract fun onMatch(message: Text, matcher: Matcher): Boolean

	companion object {
		protected const val NUMBER: String = "-?[0-9]{1,3}(?>,[0-9]{3})*(?:\\.[1-9])?"
	}
}
