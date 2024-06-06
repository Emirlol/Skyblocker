package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.AutopetFilter
import org.junit.jupiter.api.Test

internal class AutopetFilterTest : ChatPatternListenerTest<AutopetFilter?>(AutopetFilter) {
	@Test
	fun testAutopet() {
		assertMatches("Autopet equipped your [Lvl 85] Tiger! VIEW RULE")
	}
}