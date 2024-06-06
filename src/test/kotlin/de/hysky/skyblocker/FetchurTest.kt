package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dwarven.Fetchur
import org.junit.jupiter.api.Test

internal class FetchurTest : ChatPatternListenerTest<Fetchur>(Fetchur) {
	@Test
	fun patternCaptures() {
		assertGroup("[NPC] Fetchur: its a hint", 1, "a hint")
	}
}
