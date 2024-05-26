package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.dwarven.CrystalsHud
import org.joml.Vector2i
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CrystalsHudTest {
	@Test
	fun testLocationTransformation() {
		Assertions.assertEquals(CrystalsHud.transformLocation(202.0, 202.0), Vector2i(0, 0))
		Assertions.assertEquals(CrystalsHud.transformLocation(823.0, 823.0), Vector2i(62, 62))

		Assertions.assertEquals(CrystalsHud.transformLocation(512.5, 512.5), Vector2i(31, 31))

		Assertions.assertEquals(CrystalsHud.transformLocation(-50.0, -50.0), Vector2i(0, 0))
		Assertions.assertEquals(CrystalsHud.transformLocation(1000.0, 1000.0), Vector2i(62, 62))
	}
}
