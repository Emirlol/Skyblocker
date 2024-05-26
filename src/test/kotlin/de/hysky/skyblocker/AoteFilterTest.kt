package de.hysky.skyblocker

import de.hysky.skyblocker.ChatPatternListenerTest.assertMatches
import de.hysky.skyblocker.skyblock.filters.AoteFilter
import de.hysky.skyblocker.skyblock.filters.ChatFilterTest
import org.junit.jupiter.api.Test

internal class AoteFilterTest : ChatFilterTest<AoteFilter?>(AoteFilter()) {
	@Test
	fun testRegex() {
		assertMatches("There are blocks in the way!")
	}
}