package de.hysky.skyblocker

import de.hysky.skyblocker.utils.scheduler.Scheduler
import org.apache.commons.lang3.mutable.MutableInt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchedulerTest {
	private val currentTick = MutableInt(0)
	private val cycleCount1 = MutableInt(0)
	private val cycleCount2 = MutableInt(0)
	private val cycleCount3 = MutableInt(0)
	private val cycleCount4 = MutableInt(0)
	private val cycleCount5 = MutableInt(0)
	private val cycleCount6 = MutableInt(0)
	private val cycleCount7 = MutableInt(0)
	private val cycleCount8 = MutableInt(0)

	@Test
	fun testSchedule() {
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(0, currentTick.toInt()) }, 0)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(1, currentTick.toInt()) }, 1)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(2, currentTick.toInt()) }, 2)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(10, currentTick.toInt()) }, 10)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(20, currentTick.toInt()) }, 20)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(50, currentTick.toInt()) }, 50)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(100, currentTick.toInt()) }, 100)
		Scheduler.INSTANCE.schedule({ Assertions.assertEquals(123, currentTick.toInt()) }, 123)
		Scheduler.INSTANCE.scheduleCyclic({}, 1)
		Scheduler.INSTANCE.scheduleCyclic({}, 1)
		Scheduler.INSTANCE.scheduleCyclic({}, 1)
		Scheduler.INSTANCE.scheduleCyclic({}, 1)
		Scheduler.INSTANCE.scheduleCyclic({
			Assertions.assertEquals(cycleCount1.toInt(), currentTick.toInt())
			cycleCount1.increment()
		}, 1)
		Scheduler.INSTANCE.scheduleCyclic({
			Assertions.assertEquals(0, currentTick.toInt() % 10)
			Assertions.assertEquals(cycleCount2.toInt(), currentTick.toInt() / 10)
			cycleCount2.increment()
		}, 10)
		Scheduler.INSTANCE.scheduleCyclic({
			Assertions.assertEquals(0, currentTick.toInt() % 55)
			Assertions.assertEquals(cycleCount3.toInt(), currentTick.toInt() / 55)
			cycleCount3.increment()
		}, 55)
		Scheduler.INSTANCE.schedule({
			Scheduler.INSTANCE.scheduleCyclic({
				Assertions.assertEquals(7, currentTick.toInt() % 10)
				Assertions.assertEquals(cycleCount4.toInt(), currentTick.toInt() / 10)
				cycleCount4.increment()
			}, 10)
		}, 7)
		Scheduler.INSTANCE.schedule({
			Scheduler.INSTANCE.scheduleCyclic({
				Assertions.assertEquals(0, currentTick.toInt() % 75)
				Assertions.assertEquals(cycleCount5.toInt(), currentTick.toInt() / 75)
				cycleCount5.increment()
			}, 75)
		}, 0)
		Scheduler.INSTANCE.schedule({
			Scheduler.INSTANCE.scheduleCyclic({
				Assertions.assertEquals(1, currentTick.toInt() % 99)
				Assertions.assertEquals(cycleCount6.toInt(), currentTick.toInt() / 99)
				cycleCount6.increment()
			}, 99)
		}, 1)
		Scheduler.INSTANCE.scheduleCyclic({
			Scheduler.INSTANCE.schedule({
				Assertions.assertEquals(5, currentTick.toInt() % 10)
				Assertions.assertEquals(cycleCount7.toInt(), currentTick.toInt() / 10)
				cycleCount7.increment()
			}, 5)
		}, 10)
		Scheduler.INSTANCE.scheduleCyclic({
			Scheduler.INSTANCE.schedule({
				Assertions.assertEquals(10, currentTick.toInt() % 55)
				Assertions.assertEquals(cycleCount8.toInt(), currentTick.toInt() / 55)
				cycleCount8.increment()
			}, 10)
		}, 55)
		while (currentTick.toInt() < 100000) {
			tick()
		}
		Assertions.assertEquals(100000, cycleCount1.toInt())
		Assertions.assertEquals(10000, cycleCount2.toInt())
		Assertions.assertEquals(1819, cycleCount3.toInt())
		Assertions.assertEquals(10000, cycleCount4.toInt())
		Assertions.assertEquals(1334, cycleCount5.toInt())
		Assertions.assertEquals(1011, cycleCount6.toInt())
		Assertions.assertEquals(10000, cycleCount7.toInt())
		Assertions.assertEquals(1818, cycleCount8.toInt())
	}

	private fun tick() {
		Scheduler.INSTANCE.tick()
		currentTick.increment()
	}
}
