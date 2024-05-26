package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dungeon.puzzle.Trivia
import org.junit.jupiter.api.Test

internal class TriviaTest : ChatPatternListenerTest<Trivia?>(Trivia()) {
	@Test
	fun anyQuestion1() {
		assertGroup("                      What is the first question?", 1, "What is the first question?")
	}

	@Test
	fun anyQestion2() {
		assertGroup("      How many questions are there?", 1, "How many questions are there?")
	}

	@Test
	fun answer1() {
		assertGroup("     ⓐ Answer 1", 3, "Answer 1")
	}

	@Test
	fun answer2() {
		assertGroup("     ⓑ Answer 2", 3, "Answer 2")
	}

	@Test
	fun answer3() {
		assertGroup("     ⓒ Answer 3", 3, "Answer 3")
	}
}