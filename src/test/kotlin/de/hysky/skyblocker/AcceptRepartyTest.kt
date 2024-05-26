package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dungeon.Reparty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Matcher

class AcceptRepartyTest : ChatPatternListenerTest<Reparty?>(Reparty()) {
	protected fun assertGroup(message: String?, group: String?, expect: String?) {
		val matcher: Matcher = matcher(message)
		assertTrue(matcher.matches())
		assertEquals(expect, matcher.group(group))
	}

	@Test
	fun testDisband() {
		assertGroup(
			"[VIP+] KoloiYolo has disbanded the party!",  /* group: */
			"disband",  /* expect: */
			"KoloiYolo"
		)
	}

	@Test
	fun testInvite() {
		assertGroup(
			"""
	-----------------------------------------------------
	[MVP+] 1wolvesgaming has invited you to join their party!
	You have 60 seconds to accept. Click here to join!
	-----------------------------------------------------
	""".trimIndent(),  /* group: */
			"invite",  /* expect: */
			"1wolvesgaming"
		)
	}
}