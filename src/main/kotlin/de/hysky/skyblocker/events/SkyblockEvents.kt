package de.hysky.skyblocker.events

import de.hysky.skyblocker.events.SkyblockEvents.SkyblockJoin
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockLeave
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockLocationChange
import de.hysky.skyblocker.utils.Location
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

@Environment(EnvType.CLIENT)
object SkyblockEvents {
	val JOIN: Event<SkyblockJoin> = EventFactory.createArrayBacked(SkyblockJoin::class.java) {
		SkyblockJoin {
			for (callback in it) {
				callback.onSkyblockJoin()
			}
		}
	}

	val LEAVE: Event<SkyblockLeave> = EventFactory.createArrayBacked(SkyblockLeave::class.java) {
		SkyblockLeave {
			for (callback in it) {
				callback.onSkyblockLeave()
			}
		}
	}

	val LOCATION_CHANGE: Event<SkyblockLocationChange> = EventFactory.createArrayBacked(SkyblockLocationChange::class.java) {
		SkyblockLocationChange { location: Location ->
			for (callback in it) {
				callback.onSkyblockLocationChange(location)
			}
		}
	}

	@Environment(EnvType.CLIENT)
	fun interface SkyblockJoin {
		fun onSkyblockJoin()
	}

	@Environment(EnvType.CLIENT)
	fun interface SkyblockLeave {
		fun onSkyblockLeave()
	}

	@Environment(EnvType.CLIENT)
	fun interface SkyblockLocationChange {
		fun onSkyblockLocationChange(location: Location)
	}
}
