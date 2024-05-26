package de.hysky.skyblocker.skyblock.tabhud.util

import de.hysky.skyblocker.utils.Utils.isOnSkyblock

/**
 * Uses data from the player list to determine the area the player is in.
 */
object PlayerLocator {
	val playerLocation: Location
		get() {
			if (!isOnSkyblock) {
				return Location.UNKNOWN
			}

			val areaDescriptor = PlayerListMgr.strAt(41)

			if (areaDescriptor == null || areaDescriptor.length < 6) {
				return Location.UNKNOWN
			}

			if (areaDescriptor.startsWith("Dungeon")) {
				return Location.DUNGEON
			}

			return when (areaDescriptor.substring(6)) {
				"Private Island" -> {
					val islandType = PlayerListMgr.strAt(44)
					if (islandType == null) {
						Location.UNKNOWN
					} else if (islandType.endsWith("Guest")) {
						Location.GUEST_ISLAND
					} else {
						Location.HOME_ISLAND
					}
				}

				"Crimson Isle" -> Location.CRIMSON_ISLE
				"Dungeon Hub" -> Location.DUNGEON_HUB
				"The Farming Islands" -> Location.FARMING_ISLAND
				"The Park" -> Location.PARK
				"Dwarven Mines" -> Location.DWARVEN_MINES
				"Crystal Hollows" -> Location.CRYSTAL_HOLLOWS
				"The End" -> Location.END
				"Gold Mine" -> Location.GOLD_MINE
				"Deep Caverns" -> Location.DEEP_CAVERNS
				"Hub" -> Location.HUB
				"Spider's Den" -> Location.SPIDER_DEN
				"Jerry's Workshop" -> Location.JERRY
				"Garden" -> Location.GARDEN
				"Instanced" -> Location.INSTANCED
				"The Rift" -> Location.THE_RIFT
				"Dark Auction" -> Location.DARK_AUCTION
				else -> Location.UNKNOWN
			}
		}

	enum class Location // as used internally by the mod, e.g. in the json
		(val internal: String) {
		DUNGEON("dungeon"),
		GUEST_ISLAND("guest_island"),
		HOME_ISLAND("home_island"),
		CRIMSON_ISLE("crimson_isle"),
		DUNGEON_HUB("dungeon_hub"),
		FARMING_ISLAND("farming_island"),
		PARK("park"),
		DWARVEN_MINES("dwarven_mines"),
		CRYSTAL_HOLLOWS("crystal_hollows"),
		END("end"),
		GOLD_MINE("gold_mine"),
		DEEP_CAVERNS("deep_caverns"),
		HUB("hub"),
		SPIDER_DEN("spider_den"),
		JERRY("jerry_workshop"),
		GARDEN("garden"),
		INSTANCED("kuudra"),
		THE_RIFT("rift"),
		DARK_AUCTION("dark_auction"),
		GLACITE_MINESHAFT("mineshaft"),
		UNKNOWN("unknown")
	}
}
