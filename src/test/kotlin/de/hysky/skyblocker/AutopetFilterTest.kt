package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.AutopetFilter

internal class AutopetFilterTest : ChatPatternListenerTest<AutopetFilter?>(AutopetFilter()) {
	@org.junit.jupiter.api.Test
	fun testAutopet() {
		assertMatches("Autopet equipped your [Lvl 85] Tiger! VIEW RULE")
	}
}