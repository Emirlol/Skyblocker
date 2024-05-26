package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.ChatFilterTest
import de.hysky.skyblocker.skyblock.filters.TeleportPadFilter
import org.junit.jupiter.api.Test

class TeleportPadFilterTest : ChatFilterTest<TeleportPadFilter?>(TeleportPadFilter()) {
	@Test
	fun testTeleport() {
		assertMatches("Warped from the Base Teleport Pad to the Minion Teleport Pad!")
	}

	@Test
	fun testNoDestination() {
		assertMatches("This Teleport Pad does not have a destination set!")
	}
}