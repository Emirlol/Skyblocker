package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.ShowOffFilter
import org.junit.jupiter.api.Test

class ShowOffFilterTest : ChatPatternListenerTest<ShowOffFilter?>(ShowOffFilter()) {
	@Test
	fun holding() {
		assertMatches("[290] ⚡ [MVP+] Player is holding [Withered Dark Claymore ✪✪✪✪✪➎]")
	}

	@Test
	fun wearing() {
		assertMatches("[290] ⚡ [MVP+] Player is wearing [Ancient Storm's Chestplate ✪✪✪✪✪➎]")
	}

	@get:Test
	val isFriendsWith: Unit
		get() {
			assertMatches("[290] [MVP+] Player is friends with a [[Lvl 200] Golden Dragon]")
		}

	@Test
	fun has() {
		assertMatches("[290] ⚡ [MVP+] Player has [Withered Hyperion ✪✪✪✪✪]")
	}

	@Test
	fun noLevelOrEmblem() {
		assertMatches("[MVP+] Player is holding [Mithril Drill SX-R226]")
	}

	@Test
	fun noRank() {
		assertMatches("[290] ⚡ Player is holding [Oak Leaves]")
	}

	@Test
	fun noLevelOrEmblemOrRank() {
		assertMatches("Player is holding [Nether Star]")
	}
}
