package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.filters.ImplosionFilter
import org.junit.jupiter.api.Test

internal class ImplosionFilterTest : ChatFilterTest<ImplosionFilter?>(ImplosionFilter()) {
	@Test
	fun oneEnemy() {
		assertMatches("Your Implosion hit 1 enemy for 636,116.8 damage.")
	}

	@Test
	fun multipleEnemies() {
		assertMatches("Your Implosion hit 7 enemies for 4,452,817.4 damage.")
	}
}