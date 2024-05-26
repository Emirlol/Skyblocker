package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.StatusBarTracker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.min

internal class StatusBarTrackerTest {
	private var tracker: StatusBarTracker? = null

	@BeforeEach
	fun setUp() {
		tracker = StatusBarTracker()
	}

	fun assertStats(hp: Int, maxHp: Int, def: Int, mana: Int, maxMana: Int, overflowMana: Int) {
		var hp = hp
		var absorption = 0
		if (hp > maxHp) {
			absorption = min((hp - maxHp).toDouble(), maxHp.toDouble()).toInt()
			hp = maxHp
		}
		Assertions.assertEquals(StatusBarTracker.Resource(hp, maxHp, absorption), tracker!!.health)
		Assertions.assertEquals(def, tracker!!.defense)
		Assertions.assertEquals(StatusBarTracker.Resource(mana, maxMana, overflowMana), tracker!!.mana)
	}

	@Test
	fun normalStatusBar() {
		val res = tracker!!.update("§c934/1086❤     §a159§a❈ Defense     §b562/516✎ Mana", false)
		Assertions.assertNull(res)
		assertStats(934, 1086, 159, 562, 516, 0)
	}

	@Test
	fun overflowMana() {
		val res = tracker!!.update("§61605/1305❤     §a270§a❈ Defense     §b548/548✎ §3200ʬ", false)
		Assertions.assertNull(res)
		assertStats(1605, 1305, 270, 548, 548, 200)
	}

	@Test
	fun regeneration() {
		val res = tracker!!.update("§c2484/2484❤+§c120▄     §a642§a❈ Defense     §b2557/2611✎ Mana", false)
		Assertions.assertEquals("§c❤+§c120▄", res)
	}

	@Test
	fun instantTransmission() {
		val actionBar = "§c2259/2259❤     §b-20 Mana (§6Instant Transmission§b)     §b549/2676✎ Mana"
		Assertions.assertEquals("§b-20 Mana (§6Instant Transmission§b)", tracker!!.update(actionBar, false))
		Assertions.assertNull(tracker!!.update(actionBar, true))
	}

	@Test
	fun rapidFire() {
		val actionBar = "§c2509/2509❤     §b-48 Mana (§6Rapid-fire§b)     §b2739/2811✎ Mana"
		Assertions.assertEquals("§b-48 Mana (§6Rapid-fire§b)", tracker!!.update(actionBar, false))
		Assertions.assertNull(tracker!!.update(actionBar, true))
	}

	@Test
	fun zombieSword() {
		val actionBar = "§c2509/2509❤     §b-56 Mana (§6Instant Heal§b)     §b2674/2821✎ Mana    §e§lⓩⓩⓩⓩ§6§lⓄ"
		Assertions.assertEquals("§b-56 Mana (§6Instant Heal§b)     §e§lⓩⓩⓩⓩ§6§lⓄ", tracker!!.update(actionBar, false))
		Assertions.assertEquals("§e§lⓩⓩⓩⓩ§6§lⓄ", tracker!!.update(actionBar, true))
	}

	@Test
	fun campfire() {
		val res = tracker!!.update("§c17070/25565❤+§c170▃   §65,625 DPS   §c1 second     §b590/626✎ §3106ʬ", false)
		Assertions.assertEquals("§c❤+§c170▃   §65,625 DPS   §c1 second", res)
	}
}
