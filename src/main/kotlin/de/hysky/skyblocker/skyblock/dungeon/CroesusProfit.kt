package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.yellow
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.util.*

object CroesusProfit : ContainerSolver(".*Catacombs - Floor.*") {
	private val ESSENCE_PATTERN = Regex("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)")
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.config.dungeons.dungeonChestProfit.croesusProfit

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		val highlights: MutableList<ColorHighlight> = ArrayList()
		var bestChest: ItemStack? = null
		var secondBestChest: ItemStack? = null
		var bestValue = 0L
		var secondBestValue = 0L // If negative value of chest - it is out of the question
		val dungeonKeyPriceData = getItemPrice("DUNGEON_CHEST_KEY") * 2 // lesser ones don't worth the hassle

		for (slot in slots) {
			if (slot.stack.name.string.contains("Chest")) {
				val value = valueChest(slot.stack)
				if (value > bestValue) {
					secondBestChest = bestChest
					secondBestValue = bestValue
					bestChest = slot.stack
					bestValue = value
				} else if (value > secondBestValue) {
					secondBestChest = slot.stack
					secondBestValue = value
				}
			}
		}

		for (slot in slots) {
			if (slot.stack != null) {
				if (slot.stack == bestChest) {
					highlights.add(green(slot.id))
				} else if (slot.stack == secondBestChest && secondBestValue > dungeonKeyPriceData) {
					highlights.add(yellow(slot.id))
				}
			}
		}
		return highlights
	}


	private fun valueChest(chest: ItemStack): Long {
		var chestValue = 0L
		var chestPrice = 0
		val chestItems: MutableList<String> = ArrayList()

		var processingContents = false
		for (line in getLore(chest)) {
			val lineString = line.string
			when {
				lineString.contains("Contents") -> {
					processingContents = true
					continue
				}
				lineString.isEmpty() -> processingContents = false
				lineString.contains("Coins") && !processingContents -> chestPrice = lineString.replace(",".toRegex(), "").replace("\\D".toRegex(), "").toInt()
			}

			if (processingContents) {
				if (lineString.contains("Essence")) {
					val matcher = ESSENCE_PATTERN.matchEntire(lineString)
					if (matcher != null) {    // add to chest value result of multiplying price of essence on it's amount
						chestValue += getItemPrice(("ESSENCE_" + matcher.groups["type"]!!.value).uppercase(Locale.getDefault())) * matcher.groups["amount"]!!.value.toInt()
					}
				} else {
					if (lineString.contains("Spirit")) {    // TODO: make code like this to detect recombed gear (it can drop with 1% chance, according to wiki, tho I never saw any?)
						chestValue += if (line.style.toString().contains("color=dark_purple")) getItemPrice("Spirit Epic") else getItemPrice(lineString)
					} else {
						chestItems.add(lineString)
					}
				}
			}
		}
		for (item in chestItems) {
			chestValue += getItemPrice(item)
		}
		return chestValue - chestPrice
	}


	private fun getItemPrice(itemDisplayName: String): Long {
		val bazaarPrices = TooltipInfoType.BAZAAR.data
		val lbinPrices = TooltipInfoType.LOWEST_BINS.data
		val itemValue = 0L
		val id = dungeonDropsNameToId[itemDisplayName]

		if (bazaarPrices == null || lbinPrices == null) return 0

		return if (bazaarPrices.has(id)) {
			val item = bazaarPrices[id].asJsonObject
			val isPriceNull = item["sellPrice"].isJsonNull
			if (isPriceNull) 0L else item["sellPrice"].asLong
		} else if (lbinPrices.has(id)) {
			lbinPrices[id].asLong
		} else itemValue
	}

	// I did a thing :(
	private val dungeonDropsNameToId = hashMapOf(
		"Enchanted Book (Ultimate Jerry I)" to "ENCHANTMENT_ULTIMATE_JERRY_1", // ultimate books start
		"Enchanted Book (Ultimate Jerry II)" to "ENCHANTMENT_ULTIMATE_JERRY_2",
		"Enchanted Book (Ultimate Jerry III)" to "ENCHANTMENT_ULTIMATE_JERRY_3",
		"Enchanted Book (Bank I)" to "ENCHANTMENT_ULTIMATE_BANK_1",
		"Enchanted Book (Bank II)" to "ENCHANTMENT_ULTIMATE_BANK_2",
		"Enchanted Book (Bank III)" to "ENCHANTMENT_ULTIMATE_BANK_3",
		"Enchanted Book (Combo I)" to "ENCHANTMENT_ULTIMATE_COMBO_1",
		"Enchanted Book (Combo II)" to "ENCHANTMENT_ULTIMATE_COMBO_2",
		"Enchanted Book (No Pain No Gain I)" to "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1",
		"Enchanted Book (No Pain No Gain II)" to "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2",
		"Enchanted Book (Ultimate Wise I)" to "ENCHANTMENT_ULTIMATE_WISE_1",
		"Enchanted Book (Ultimate Wise II)" to "ENCHANTMENT_ULTIMATE_WISE_2",
		"Enchanted Book (Wisdom I)" to "ENCHANTMENT_ULTIMATE_WISDOM_1",
		"Enchanted Book (Wisdom II)" to "ENCHANTMENT_ULTIMATE_WISDOM_2",
		"Enchanted Book (Last Stand I)" to "ENCHANTMENT_ULTIMATE_LAST_STAND_1",
		"Enchanted Book (Last Stand II)" to "ENCHANTMENT_ULTIMATE_LAST_STAND_2",
		"Enchanted Book (Rend I)" to "ENCHANTMENT_ULTIMATE_REND_1",
		"Enchanted Book (Rend II)" to "ENCHANTMENT_ULTIMATE_REND_2",
		"Enchanted Book (Legion I)" to "ENCHANTMENT_ULTIMATE_LEGION_1",
		"Enchanted Book (Swarm I)" to "ENCHANTMENT_ULTIMATE_SWARM_1",
		"Enchanted Book (One For All I)" to "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1",
		"Enchanted Book (Soul Eater I)" to "ENCHANTMENT_ULTIMATE_SOUL_EATER_1", // ultimate books end
		"Enchanted Book (Infinite Quiver VI)" to "ENCHANTMENT_INFINITE_QUIVER_6", // enchanted books start
		"Enchanted Book (Infinite Quiver VII)" to "ENCHANTMENT_INFINITE_QUIVER_7",
		"Enchanted Book (Feather Falling VI)" to "ENCHANTMENT_FEATHER_FALLING_6",
		"Enchanted Book (Feather Falling VII)" to "ENCHANTMENT_FEATHER_FALLING_7",
		"Enchanted Book (Rejuvenate I)" to "ENCHANTMENT_REJUVENATE_1",
		"Enchanted Book (Rejuvenate II)" to "ENCHANTMENT_REJUVENATE_2",
		"Enchanted Book (Rejuvenate III)" to "ENCHANTMENT_REJUVENATE_3",
		"Enchanted Book (Overload)" to "ENCHANTMENT_OVERLOAD_1",
		"Enchanted Book (Lethality VI)" to "ENCHANTMENT_LETHALITY_6",
		"Enchanted Book (Thunderlord VII)" to "ENCHANTMENT_THUNDERLORD_7", // enchanted books end

		"Hot Potato Book" to "HOT_POTATO_BOOK", // HPB, FPB, Recomb (universal drops)
		"Fuming Potato Book" to "FUMING_POTATO_BOOK",
		"Recombobulator 3000" to "RECOMBOBULATOR_3000",
		"Necromancer's Brooch" to "NECROMANCER_BROOCH",
		"ESSENCE_WITHER" to "ESSENCE_WITHER", // Essences. Really stupid way of doing this
		"ESSENCE_UNDEAD" to "ESSENCE_UNDEAD",
		"ESSENCE_DRAGON" to "ESSENCE_DRAGON",
		"ESSENCE_SPIDER" to "ESSENCE_SPIDER",
		"ESSENCE_ICE" to "ESSENCE_ICE",
		"ESSENCE_DIAMOND" to "ESSENCE_DIAMOND",
		"ESSENCE_GOLD" to "ESSENCE_GOLD",
		"ESSENCE_CRIMSON" to "ESSENCE_CRIMSON",
		"DUNGEON_CHEST_KEY" to "DUNGEON_CHEST_KEY",

		"Bonzo's Staff" to "BONZO_STAFF", // F1 M1
		"Master Skull - Tier 1" to "MASTER_SKULL_TIER_1",
		"Bonzo's Mask" to "BONZO_MASK",
		"Balloon Snake" to "BALLOON_SNAKE",
		"Red Nose" to "RED_NOSE",

		"Red Scarf" to "RED_SCARF", // F2 M2
		"Adaptive Blade" to "STONE_BLADE",
		"Master Skull - Tier 2" to "MASTER_SKULL_TIER_2",
		"Adaptive Belt" to "ADAPTIVE_BELT",
		"Scarf's Studies" to "SCARF_STUDIES",

		"First Master Star" to "FIRST_MASTER_STAR", // F3 M3
		"Adaptive Helmet" to "ADAPTIVE_HELMET",
		"Adaptive Chestplate" to "ADAPTIVE_CHESTPLATE",
		"Adaptive Leggings" to "ADAPTIVE_LEGGINGS",
		"Adaptive Boots" to "ADAPTIVE_BOOTS",
		"Master Skull - Tier 3" to "MASTER_SKULL_TIER_3",
		"Suspicious Vial" to "SUSPICIOUS_VIAL",

		"Spirit Sword" to "SPIRIT_SWORD", // F4 M4
		"Spirit Shortbow" to "ITEM_SPIRIT_BOW",
		"Spirit Boots" to "THORNS_BOOTS",
		"Spirit" to "LVL_1_LEGENDARY_SPIRIT", // Spirit pet (Legendary)
		"Spirit Epic" to "LVL_1_EPIC_SPIRIT",

		"Second Master Star" to "SECOND_MASTER_STAR",
		"Spirit Wing" to "SPIRIT_WING",
		"Spirit Bone" to "SPIRIT_BONE",
		"Spirit Stone" to "SPIRIT_DECOY",

		"Shadow Fury" to "SHADOW_FURY", // F5 M5
		"Last Breath" to "LAST_BREATH",
		"Third Master Star" to "THIRD_MASTER_STAR",
		"Warped Stone" to "AOTE_STONE",
		"Livid Dagger" to "LIVID_DAGGER",
		"Shadow Assassin Helmet" to "SHADOW_ASSASSIN_HELMET",
		"Shadow Assassin Chestplate" to "SHADOW_ASSASSIN_CHESTPLATE",
		"Shadow Assassin Leggings" to "SHADOW_ASSASSIN_LEGGINGS",
		"Shadow Assassin Boots" to "SHADOW_ASSASSIN_BOOTS",
		"Shadow Assassin Cloak" to "SHADOW_ASSASSIN_CLOAK",
		"Master Skull - Tier 4" to "MASTER_SKULL_TIER_4",
		"Dark Orb" to "DARK_ORB",

		"Precursor Eye" to "PRECURSOR_EYE", // F6 M6
		"Giant's Sword" to "GIANTS_SWORD",
		"Necromancer Lord Helmet" to "NECROMANCER_LORD_HELMET",
		"Necromancer Lord Chestplate" to "NECROMANCER_LORD_CHESTPLATE",
		"Necromancer Lord Leggings" to "NECROMANCER_LORD_LEGGINGS",
		"Necromancer Lord Boots" to "NECROMANCER_LORD_BOOTS",
		"Fourth Master Star" to "FOURTH_MASTER_STAR",
		"Summoning Ring" to "SUMMONING_RING",
		"Fel Skull" to "FEL_SKULL",
		"Necromancer Sword" to "NECROMANCER_SWORD",
		"Soulweaver Gloves" to "SOULWEAVER_GLOVES",
		"Sadan's Brooch" to "SADAN_BROOCH",
		"Giant Tooth" to "GIANT_TOOTH",

		"Precursor Gear" to "PRECURSOR_GEAR", // F7 M7
		"Necron Dye" to "DYE_NECRON",
		"Storm the Fish" to "STORM_THE_FISH",
		"Maxor the Fish" to "MAXOR_THE_FISH",
		"Goldor the Fish" to "GOLDOR_THE_FISH",
		"Dark Claymore" to "DARK_CLAYMORE",
		"Necron's Handle" to "NECRON_HANDLE",
		"Master Skull - Tier 5" to "MASTER_SKULL_TIER_5",
		"Shadow Warp" to "SHADOW_WARP_SCROLL",
		"Wither Shield" to "WITHER_SHIELD_SCROLL",
		"Implosion" to "IMPLOSION_SCROLL",
		"Fifth Master Star" to "FIFTH_MASTER_STAR",
		"Auto Recombobulator" to "AUTO_RECOMBOBULATOR",
		"Wither Helmet" to "WITHER_HELMET",
		"Wither Chestplate" to "WITHER_CHESTPLATE",
		"Wither Leggings" to "WITHER_LEGGINGS",
		"Wither Boots" to "WITHER_BOOTS",
		"Wither Catalyst" to "WITHER_CATALYST",
		"Wither Cloak Sword" to "WITHER_CLOAK",
		"Wither Blood" to "WITHER_BLOOD",

		"Shiny Wither Helmet" to "SHINY_WITHER_HELMET", // M7 shiny drops
		"Shiny Wither Chestplate" to "SHINY_WITHER_CHESTPLATE",
		"Shiny Wither Leggings" to "SHINY_WITHER_LEGGINGS",
		"Shiny Wither Boots" to "SHINY_WITHER_BOOTS",
		"Shiny Necron's Handle" to "SHINY_NECRON_HANDLE", // cool thing

		"Dungeon Disc" to "DUNGEON_DISC_1",
		"Clown Disc" to "DUNGEON_DISC_2",
		"Watcher Disc" to "DUNGEON_DISC_3",
		"Old Disc" to "DUNGEON_DISC_4",
		"Necron Disc" to "DUNGEON_DISC_5",
	)
}

