package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dwarven.Puzzler
import org.junit.jupiter.api.Test

internal class PuzzlerTest : ChatPatternListenerTest<Puzzler?>(Puzzler()) {
	@Test
	fun puzzler() {
		assertGroup("[NPC] Puzzler: ◀▲◀▲▲▶▶◀▲▼", 1, "◀▲◀▲▲▶▶◀▲▼")
	}
}