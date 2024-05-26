package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dungeon.puzzle.ThreeWeirdos
import org.junit.jupiter.api.Test

internal class ThreeWeirdosTest {
	@Test
	fun testBaxter() {
		ChatPatternListenerTest.assertGroup(ThreeWeirdos.PATTERN.matcher("[NPC] Baxter: My chest doesn't have the reward. We are all telling the truth."), 1, "Baxter")
	}

	@Test
	fun testHope() {
		ChatPatternListenerTest.assertGroup(ThreeWeirdos.PATTERN.matcher("[NPC] Hope: The reward isn't in any of our chests."), 1, "Hope")
	}
}