package de.hysky.skyblocker.skyblock.item

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

object AttributeShards {
	private val ID_2_SHORT_NAME = Object2ObjectOpenHashMap<String, String>()

	init {
		//Weapons
		ID_2_SHORT_NAME["arachno"] = "A"
		ID_2_SHORT_NAME["attack_speed"] = "AS"
		ID_2_SHORT_NAME["blazing"] = "BL"
		ID_2_SHORT_NAME["combo"] = "C"
		ID_2_SHORT_NAME["elite"] = "E"
		ID_2_SHORT_NAME["ender"] = "EN"
		ID_2_SHORT_NAME["ignition"] = "I"
		ID_2_SHORT_NAME["life_recovery"] = "LR"
		ID_2_SHORT_NAME["mana_steal"] = "MS"
		ID_2_SHORT_NAME["midas_touch"] = "MT"
		ID_2_SHORT_NAME["undead"] = "U"

		//Swords & Bows
		ID_2_SHORT_NAME["warrior"] = "W"
		ID_2_SHORT_NAME["deadeye"] = "DE"

		//Armor or Equipment
		ID_2_SHORT_NAME["arachno_resistance"] = "AR"
		ID_2_SHORT_NAME["blazing_resistance"] = "BR"
		ID_2_SHORT_NAME["breeze"] = "B"
		ID_2_SHORT_NAME["dominance"] = "D"
		ID_2_SHORT_NAME["ender_resistance"] = "ER"
		ID_2_SHORT_NAME["experience"] = "XP"
		ID_2_SHORT_NAME["fortitude"] = "F"
		ID_2_SHORT_NAME["life_regeneration"] = "HR" //Health regeneration
		ID_2_SHORT_NAME["lifeline"] = "L"
		ID_2_SHORT_NAME["magic_find"] = "MF"
		ID_2_SHORT_NAME["mana_pool"] = "MP"
		ID_2_SHORT_NAME["mana_regeneration"] = "MR"
		ID_2_SHORT_NAME["mending"] = "V" //Vitality
		ID_2_SHORT_NAME["speed"] = "S"
		ID_2_SHORT_NAME["undead_resistance"] = "UR"
		ID_2_SHORT_NAME["veteran"] = "V"

		//Fishing Gear
		ID_2_SHORT_NAME["blazing_fortune"] = "BF"
		ID_2_SHORT_NAME["fishing_experience"] = "FE"
		ID_2_SHORT_NAME["infection"] = "IF"
		ID_2_SHORT_NAME["double_hook"] = "DH"
		ID_2_SHORT_NAME["fisherman"] = "FM"
		ID_2_SHORT_NAME["fishing_speed"] = "FS"
		ID_2_SHORT_NAME["hunter"] = "H"
		ID_2_SHORT_NAME["trophy_hunter"] = "TH"
	}

	@JvmStatic
	fun getShortName(id: String): String {
		return ID_2_SHORT_NAME.getOrDefault(id, "")
	}
}
