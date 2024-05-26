package de.hysky.skyblocker

import de.hysky.skyblocker.utils.chat.ChatPatternListener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.regex.Matcher

abstract class ChatPatternListenerTest<T : ChatPatternListener?>(@JvmField protected val listener: T) {
	protected fun matcher(message: String?): Matcher {
		return listener!!.pattern.matcher(message)
	}

	protected fun assertMatches(message: String?) {
		assertTrue(matcher(message).matches())
	}

	protected fun assertGroup(message: String?, group: Int, expect: String?) {
		assertGroup(matcher(message), group, expect)
	}

	companion object {
		@JvmStatic
        fun assertGroup(matcher: Matcher, group: Int, expect: String?) {
			assertTrue(matcher.matches())
			assertEquals(expect, matcher.group(group))
		}
	}
}