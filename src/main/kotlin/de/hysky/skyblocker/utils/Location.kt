package de.hysky.skyblocker.utils

/**
 * All Skyblock locations
 */
enum class Location(
	/**
	 * location id from [Hypixel API](https://api.hypixel.net/v2/resources/games)
	 */
	private val mode: String
) {
	PRIVATE_ISLAND("dynamic"),
	GARDEN("garden"),
	HUB("hub"),
	THE_FARMING_ISLAND("farming_1"),
	THE_PARK("foraging_1"),
	SPIDERS_DEN("combat_1"),
	BLAZING_FORTRESS("combat_2"),
	THE_END("combat_3"),
	CRIMSON_ISLE("crimson_isle"),
	GOLD_MINE("mining_1"),
	DEEP_CAVERNS("mining_2"),
	DWARVEN_MINES("mining_3"),
	DUNGEON_HUB("dungeon_hub"),
	WINTER_ISLAND("winter"),
	THE_RIFT("rift"),
	DARK_AUCTION("dark_auction"),
	CRYSTAL_HOLLOWS("crystal_hollows"),
	DUNGEON("dungeon"),
	KUUDRAS_HOLLOW("kuudra"),
	GLACITE_MINESHAFT("mineshaft"),
	MODERN_FORAGING_ISLAND("placeholder"); //Not used yet

	companion object {
		/**
		 * @param mode location id from [Hypixel API](https://api.hypixel.net/v2/resources/games)
		 * @return location object
		 */
		fun from(mode: String) = entries.firstOrNull { mode == it.mode }
	}
}
