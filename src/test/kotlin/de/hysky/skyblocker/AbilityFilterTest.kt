package de.hysky.skyblocker

import de.hysky.skyblocker.ChatPatternListenerTest.assertMatches
import de.hysky.skyblocker.skyblock.filters.AbilityFilter
import de.hysky.skyblocker.skyblock.filters.ChatFilterTest
import org.junit.jupiter.api.Test

internal class AbilityFilterTest : ChatFilterTest<AbilityFilter?>(AbilityFilter()) {
	@Test
	fun charges() {
		assertMatches("No more charges, next one in 13.2s!")
	}

	@Test
	fun cooldown() {
		assertMatches("This ability is on cooldown for 42s.")
	}
}