package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.ComboFilter
import org.junit.jupiter.api.Test

class ComboFilterTest : ChatFilterTest<ComboFilter?>(ComboFilter()) {
	@Test
	fun testComboMF() {
		assertMatches("+5 Kill Combo +3✯ Magic Find")
	}

	@Test
	fun testComboCoins() {
		assertMatches("+10 Kill Combo +10 coins per kill")
	}

	@Test
	fun testComboWisdom() {
		assertMatches("+20 Kill Combo +15☯ Combat Wisdom")
	}

	@Test
	fun testComboNoBonus() {
		assertMatches("+50 Kill Combo")
	}

	@Test
	fun testComboExpired() {
		assertMatches("Your Kill Combo has expired! You reached a 11 Kill Combo!")
	}
}
