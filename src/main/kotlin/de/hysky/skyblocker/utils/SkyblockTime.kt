package de.hysky.skyblocker.utils

import de.hysky.skyblocker.utils.scheduler.Scheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler.scheduleCyclic
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.floor

object SkyblockTime {
	private const val SKYBLOCK_EPOCH = 1560275700000L
	val skyblockYear: AtomicInteger = AtomicInteger(0)
	val skyblockSeason: AtomicReference<Season> = AtomicReference(Season.SPRING)
	val skyblockMonth: AtomicReference<Month> = AtomicReference(Month.EARLY_SPRING)
	val skyblockDay: AtomicInteger = AtomicInteger(0)

	init {
		updateTime()
		//ScheduleCyclic already runs the task upon scheduling, so there's no need to call updateTime() here
		Scheduler.schedule((1200000 - (skyblockMillis % 1200000)).toInt() / 50) {
			scheduleCyclic(1200 * 24) { updateTime() }
		}
	}

	private val skyblockMillis: Long
		get() = System.currentTimeMillis() - SKYBLOCK_EPOCH

	private fun getSkyblockYear() = (floor(skyblockMillis / 446400000.0) + 1).toInt()

	private fun getSkyblockMonth() = (floor(skyblockMillis / 37200000.0) % 12).toInt()

	private fun getSkyblockDay() = (floor(skyblockMillis / 1200000.0) % 31 + 1).toInt()

	private fun updateTime() {
		skyblockYear.set(getSkyblockYear())
		skyblockSeason.set(Season.entries[getSkyblockMonth() / 3])
		skyblockMonth.set(Month.entries[getSkyblockMonth()])
		skyblockDay.set(getSkyblockDay())
		TextHandler.info("[Time] Skyblock time updated to Year ${skyblockYear.get()}, Season ${skyblockSeason.get()}, Month ${skyblockMonth.get()}, Day ${skyblockDay.get()}")
	}

	enum class Season {
		SPRING, SUMMER, FALL, WINTER
	}

	enum class Month {
		EARLY_SPRING, SPRING, LATE_SPRING,
		EARLY_SUMMER, SUMMER, LATE_SUMMER,
		EARLY_FALL, FALL, LATE_FALL,
		EARLY_WINTER, WINTER, LATE_WINTER
	}
}
