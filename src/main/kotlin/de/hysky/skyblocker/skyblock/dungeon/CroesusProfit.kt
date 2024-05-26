package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.yellow
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.item.ItemStack
import net.minecraft.util.Util
import java.util.*
import java.util.regex.Pattern

class CroesusProfit : ContainerSolver(".*Catacombs - Floor.*") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		var bestChest: ItemStack? = null
		var secondBestChest: ItemStack? = null
		var bestValue: Long = 0
		var secondBestValue: Long = 0 // If negative value of chest - it is out of the question
		val dungeonKeyPriceData = getItemPrice("DUNGEON_CHEST_KEY") * 2 // lesser ones don't worth the hassle

		for ((_, stack) in slots!!.int2ObjectEntrySet()) {
			if (stack!!.name.string.contains("Chest")) {
				val value = valueChest(stack)
				if (value > bestValue) {
					secondBestChest = bestChest
					secondBestValue = bestValue
					bestChest = stack
					bestValue = value
				} else if (value > secondBestValue) {
					secondBestChest = stack
					secondBestValue = value
				}
			}
		}

		for (entry in slots.int2ObjectEntrySet()) {
			val stack = entry.value
			if (stack != null) {
				if (stack == bestChest) {
					highlights.add(green(entry.intKey))
				} else if (stack == secondBestChest && secondBestValue > dungeonKeyPriceData) {
					highlights.add(yellow(entry.intKey))
				}
			}
		}
		return highlights
	}


	private fun valueChest(chest: ItemStack): Long {
		var chestValue: Long = 0
		var chestPrice = 0
		val chestItems: MutableList<String> = ArrayList()

		var processingContents = false
		for (line in getLore(chest)) {
			val lineString = line.string
			if (lineString.contains("Contents")) {
				processingContents = true
				continue
			} else if (lineString.isEmpty()) {
				processingContents = false
			} else if (lineString.contains("Coins") && !processingContents) {
				chestPrice = lineString.replace(",".toRegex(), "").replace("\\D".toRegex(), "").toInt()
			}

			if (processingContents) {
				if (lineString.contains("Essence")) {
					val matcher = ESSENCE_PATTERN.matcher(lineString)
					if (matcher.matches()) {    // add to chest value result of multiplying price of essence on it's amount
						chestValue += getItemPrice(("ESSENCE_" + matcher.group("type")).uppercase(Locale.getDefault())) * matcher.group("amount").toInt()
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
		val itemValue: Long = 0
		val id = dungeonDropsNameToId[itemDisplayName]

		if (bazaarPrices == null || lbinPrices == null) return 0

		if (bazaarPrices.has(id)) {
			val item = bazaarPrices[id].asJsonObject
			val isPriceNull = item["sellPrice"].isJsonNull
			return (if (isPriceNull) 0L else item["sellPrice"].asLong)
		} else if (lbinPrices.has(id)) {
			return lbinPrices[id].asLong
		}
		return itemValue
	}


	// I did a thing :(
	private val dungeonDropsNameToId: Map<String, String> = Util.make(HashMap()) { map: HashMap<String, String> ->
		map["Enchanted Book (Ultimate Jerry I)"] = "ENCHANTMENT_ULTIMATE_JERRY_1" // ultimate books start
		map["Enchanted Book (Ultimate Jerry II)"] = "ENCHANTMENT_ULTIMATE_JERRY_2"
		map["Enchanted Book (Ultimate Jerry III)"] = "ENCHANTMENT_ULTIMATE_JERRY_3"
		map["Enchanted Book (Bank I)"] = "ENCHANTMENT_ULTIMATE_BANK_1"
		map["Enchanted Book (Bank II)"] = "ENCHANTMENT_ULTIMATE_BANK_2"
		map["Enchanted Book (Bank III)"] = "ENCHANTMENT_ULTIMATE_BANK_3"
		map["Enchanted Book (Combo I)"] = "ENCHANTMENT_ULTIMATE_COMBO_1"
		map["Enchanted Book (Combo II)"] = "ENCHANTMENT_ULTIMATE_COMBO_2"
		map["Enchanted Book (No Pain No Gain I)"] = "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1"
		map["Enchanted Book (No Pain No Gain II)"] = "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2"
		map["Enchanted Book (Ultimate Wise I)"] = "ENCHANTMENT_ULTIMATE_WISE_1"
		map["Enchanted Book (Ultimate Wise II)"] = "ENCHANTMENT_ULTIMATE_WISE_2"
		map["Enchanted Book (Wisdom I)"] = "ENCHANTMENT_ULTIMATE_WISDOM_1"
		map["Enchanted Book (Wisdom II)"] = "ENCHANTMENT_ULTIMATE_WISDOM_2"
		map["Enchanted Book (Last Stand I)"] = "ENCHANTMENT_ULTIMATE_LAST_STAND_1"
		map["Enchanted Book (Last Stand II)"] = "ENCHANTMENT_ULTIMATE_LAST_STAND_2"
		map["Enchanted Book (Rend I)"] = "ENCHANTMENT_ULTIMATE_REND_1"
		map["Enchanted Book (Rend II)"] = "ENCHANTMENT_ULTIMATE_REND_2"
		map["Enchanted Book (Legion I)"] = "ENCHANTMENT_ULTIMATE_LEGION_1"
		map["Enchanted Book (Swarm I)"] = "ENCHANTMENT_ULTIMATE_SWARM_1"
		map["Enchanted Book (One For All I)"] = "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1"
		map["Enchanted Book (Soul Eater I)"] = "ENCHANTMENT_ULTIMATE_SOUL_EATER_1" // ultimate books end
		map["Enchanted Book (Infinite Quiver VI)"] = "ENCHANTMENT_INFINITE_QUIVER_6" // enchanted books start
		map["Enchanted Book (Infinite Quiver VII)"] = "ENCHANTMENT_INFINITE_QUIVER_7"
		map["Enchanted Book (Feather Falling VI)"] = "ENCHANTMENT_FEATHER_FALLING_6"
		map["Enchanted Book (Feather Falling VII)"] = "ENCHANTMENT_FEATHER_FALLING_7"
		map["Enchanted Book (Rejuvenate I)"] = "ENCHANTMENT_REJUVENATE_1"
		map["Enchanted Book (Rejuvenate II)"] = "ENCHANTMENT_REJUVENATE_2"
		map["Enchanted Book (Rejuvenate III)"] = "ENCHANTMENT_REJUVENATE_3"
		map["Enchanted Book (Overload)"] = "ENCHANTMENT_OVERLOAD_1"
		map["Enchanted Book (Lethality VI)"] = "ENCHANTMENT_LETHALITY_6"
		map["Enchanted Book (Thunderlord VII)"] = "ENCHANTMENT_THUNDERLORD_7" // enchanted books end

		map["Hot Potato Book"] = "HOT_POTATO_BOOK" // HPB, FPB, Recomb (universal drops)
		map["Fuming Potato Book"] = "FUMING_POTATO_BOOK"
		map["Recombobulator 3000"] = "RECOMBOBULATOR_3000"
		map["Necromancer's Brooch"] = "NECROMANCER_BROOCH"
		map["ESSENCE_WITHER"] = "ESSENCE_WITHER" // Essences. Really stupid way of doing this
		map["ESSENCE_UNDEAD"] = "ESSENCE_UNDEAD"
		map["ESSENCE_DRAGON"] = "ESSENCE_DRAGON"
		map["ESSENCE_SPIDER"] = "ESSENCE_SPIDER"
		map["ESSENCE_ICE"] = "ESSENCE_ICE"
		map["ESSENCE_DIAMOND"] = "ESSENCE_DIAMOND"
		map["ESSENCE_GOLD"] = "ESSENCE_GOLD"
		map["ESSENCE_CRIMSON"] = "ESSENCE_CRIMSON"
		map["DUNGEON_CHEST_KEY"] = "DUNGEON_CHEST_KEY"

		map["Bonzo's Staff"] = "BONZO_STAFF" // F1 M1
		map["Master Skull - Tier 1"] = "MASTER_SKULL_TIER_1"
		map["Bonzo's Mask"] = "BONZO_MASK"
		map["Balloon Snake"] = "BALLOON_SNAKE"
		map["Red Nose"] = "RED_NOSE"

		map["Red Scarf"] = "RED_SCARF" // F2 M2
		map["Adaptive Blade"] = "STONE_BLADE"
		map["Master Skull - Tier 2"] = "MASTER_SKULL_TIER_2"
		map["Adaptive Belt"] = "ADAPTIVE_BELT"
		map["Scarf's Studies"] = "SCARF_STUDIES"

		map["First Master Star"] = "FIRST_MASTER_STAR" // F3 M3
		map["Adaptive Helmet"] = "ADAPTIVE_HELMET"
		map["Adaptive Chestplate"] = "ADAPTIVE_CHESTPLATE"
		map["Adaptive Leggings"] = "ADAPTIVE_LEGGINGS"
		map["Adaptive Boots"] = "ADAPTIVE_BOOTS"
		map["Master Skull - Tier 3"] = "MASTER_SKULL_TIER_3"
		map["Suspicious Vial"] = "SUSPICIOUS_VIAL"

		map["Spirit Sword"] = "SPIRIT_SWORD" // F4 M4
		map["Spirit Shortbow"] = "ITEM_SPIRIT_BOW"
		map["Spirit Boots"] = "THORNS_BOOTS"
		map["Spirit"] = "LVL_1_LEGENDARY_SPIRIT" // Spirit pet (Legendary)
		map["Spirit Epic"] = "LVL_1_EPIC_SPIRIT"

		map["Second Master Star"] = "SECOND_MASTER_STAR"
		map["Spirit Wing"] = "SPIRIT_WING"
		map["Spirit Bone"] = "SPIRIT_BONE"
		map["Spirit Stone"] = "SPIRIT_DECOY"

		map["Shadow Fury"] = "SHADOW_FURY" // F5 M5
		map["Last Breath"] = "LAST_BREATH"
		map["Third Master Star"] = "THIRD_MASTER_STAR"
		map["Warped Stone"] = "AOTE_STONE"
		map["Livid Dagger"] = "LIVID_DAGGER"
		map["Shadow Assassin Helmet"] = "SHADOW_ASSASSIN_HELMET"
		map["Shadow Assassin Chestplate"] = "SHADOW_ASSASSIN_CHESTPLATE"
		map["Shadow Assassin Leggings"] = "SHADOW_ASSASSIN_LEGGINGS"
		map["Shadow Assassin Boots"] = "SHADOW_ASSASSIN_BOOTS"
		map["Shadow Assassin Cloak"] = "SHADOW_ASSASSIN_CLOAK"
		map["Master Skull - Tier 4"] = "MASTER_SKULL_TIER_4"
		map["Dark Orb"] = "DARK_ORB"

		map["Precursor Eye"] = "PRECURSOR_EYE" // F6 M6
		map["Giant's Sword"] = "GIANTS_SWORD"
		map["Necromancer Lord Helmet"] = "NECROMANCER_LORD_HELMET"
		map["Necromancer Lord Chestplate"] = "NECROMANCER_LORD_CHESTPLATE"
		map["Necromancer Lord Leggings"] = "NECROMANCER_LORD_LEGGINGS"
		map["Necromancer Lord Boots"] = "NECROMANCER_LORD_BOOTS"
		map["Fourth Master Star"] = "FOURTH_MASTER_STAR"
		map["Summoning Ring"] = "SUMMONING_RING"
		map["Fel Skull"] = "FEL_SKULL"
		map["Necromancer Sword"] = "NECROMANCER_SWORD"
		map["Soulweaver Gloves"] = "SOULWEAVER_GLOVES"
		map["Sadan's Brooch"] = "SADAN_BROOCH"
		map["Giant Tooth"] = "GIANT_TOOTH"

		map["Precursor Gear"] = "PRECURSOR_GEAR" // F7 M7
		map["Necron Dye"] = "DYE_NECRON"
		map["Storm the Fish"] = "STORM_THE_FISH"
		map["Maxor the Fish"] = "MAXOR_THE_FISH"
		map["Goldor the Fish"] = "GOLDOR_THE_FISH"
		map["Dark Claymore"] = "DARK_CLAYMORE"
		map["Necron's Handle"] = "NECRON_HANDLE"
		map["Master Skull - Tier 5"] = "MASTER_SKULL_TIER_5"
		map["Shadow Warp"] = "SHADOW_WARP_SCROLL"
		map["Wither Shield"] = "WITHER_SHIELD_SCROLL"
		map["Implosion"] = "IMPLOSION_SCROLL"
		map["Fifth Master Star"] = "FIFTH_MASTER_STAR"
		map["Auto Recombobulator"] = "AUTO_RECOMBOBULATOR"
		map["Wither Helmet"] = "WITHER_HELMET"
		map["Wither Chestplate"] = "WITHER_CHESTPLATE"
		map["Wither Leggings"] = "WITHER_LEGGINGS"
		map["Wither Boots"] = "WITHER_BOOTS"
		map["Wither Catalyst"] = "WITHER_CATALYST"
		map["Wither Cloak Sword"] = "WITHER_CLOAK"
		map["Wither Blood"] = "WITHER_BLOOD"

		map["Shiny Wither Helmet"] = "SHINY_WITHER_HELMET" // M7 shiny drops
		map["Shiny Wither Chestplate"] = "SHINY_WITHER_CHESTPLATE"
		map["Shiny Wither Leggings"] = "SHINY_WITHER_LEGGINGS"
		map["Shiny Wither Boots"] = "SHINY_WITHER_BOOTS"
		map["Shiny Necron's Handle"] = "SHINY_NECRON_HANDLE" // cool thing

		map["Dungeon Disc"] = "DUNGEON_DISC_1"
		map["Clown Disc"] = "DUNGEON_DISC_2"
		map["Watcher Disc"] = "DUNGEON_DISC_3"
		map["Old Disc"] = "DUNGEON_DISC_4"
		map["Necron Disc"] = "DUNGEON_DISC_5"
	}

	companion object {
		private val ESSENCE_PATTERN: Pattern = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)")
	}
}

