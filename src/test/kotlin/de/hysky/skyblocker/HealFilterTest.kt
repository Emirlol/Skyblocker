package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.ChatFilterTest
import de.hysky.skyblocker.skyblock.filters.HealFilter
import org.junit.jupiter.api.Test

internal class HealFilterTest : ChatFilterTest<HealFilter?>(HealFilter()) {
	@Test
	fun healSelf() {
		assertMatches("You healed yourself for 18.3 health!")
	}

	@Test
	fun healedYou() {
		assertMatches("H3aler_ healed you for 56 health!")
	}
}