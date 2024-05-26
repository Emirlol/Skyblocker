package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.AdFilter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.regex.Matcher

internal class AdFilterTest : ChatPatternListenerTest<AdFilter?>(AdFilter()) {
	@Test
	fun noRank() {
		assertMatches("[86] Advertiser: advertisement")
	}

	@Test
	fun vip() {
		assertMatches("[280] [VIP] Advertiser: advertisement")
	}

	@Test
	fun mvp() {
		assertMatches("[256] ⚡ [MVP+] Advertiser: advertisement")
	}

	@Test
	fun plusPlus() {
		assertMatches("[222] [MVP++] Advertiser: advertisement")
	}

	@Test
	fun capturesMessage() {
		assertGroup("[325] [MVP+] b2dderr: buying prismapump", 2, "buying prismapump")
	}

	@Test
	fun simpleAd() {
		assertFilters("[320] [MVP+] b2dderr: buying prismapump")
	}

	@Test
	fun uppercaseAd() {
		assertFilters("[70] [VIP] Tecnoisnoob: SELLING REJUVENATE 5 Book on ah!")
	}

	@Test
	fun characterSpam() {
		assertFilters("[144] [VIP] Benyyy_: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<")
	}

	@Test
	fun notAd() {
		val matcher: Matcher = listener.pattern.matcher("[200] [VIP] NotMatching: This message shouldn't match!")
		Assertions.assertTrue(matcher.matches())
		Assertions.assertFalse(listener.onMatch(null, matcher))
	}

	fun assertFilters(message: String?) {
		val matcher: Matcher = listener.pattern.matcher(message)
		Assertions.assertTrue(matcher.matches())
		Assertions.assertTrue(listener.onMatch(null, matcher))
	}
}