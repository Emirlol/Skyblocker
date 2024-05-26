package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest

internal class FetchurTest : ChatPatternListenerTest<Fetchur?>(Fetchur()) {
	@Test
	fun patternCaptures() {
		assertGroup("[NPC] Fetchur: its a hint", 1, "a hint")
	}
}
