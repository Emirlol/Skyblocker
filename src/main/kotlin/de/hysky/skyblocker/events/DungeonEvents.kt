package de.hysky.skyblocker.events

import de.hysky.skyblocker.events.DungeonEvents.RoomMatched
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

object DungeonEvents {
    val PUZZLE_MATCHED: Event<RoomMatched> = EventFactory.createArrayBacked(RoomMatched::class.java) { callbacks: Array<RoomMatched> ->
		RoomMatched { room: Room ->
			for (callback in callbacks) {
				callback.onRoomMatched(room)
			}
		}
	}

    val ROOM_MATCHED: Event<RoomMatched> = EventFactory.createArrayBacked(RoomMatched::class.java) { callbacks: Array<RoomMatched> ->
		RoomMatched { room: Room ->
			for (callback in callbacks) {
				callback.onRoomMatched(room)
			}
			if (room.type == Room.Type.PUZZLE) {
				PUZZLE_MATCHED.invoker().onRoomMatched(room)
			}
		}
	}

	@Environment(EnvType.CLIENT)
	fun interface RoomMatched {
		fun onRoomMatched(room: Room)
	}
}
