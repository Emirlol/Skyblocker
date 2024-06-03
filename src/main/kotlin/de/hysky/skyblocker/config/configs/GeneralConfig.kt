package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes.AnimatedDye
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims.ArmorTrimId
import dev.isxander.yacl3.config.v2.api.SerialEntry
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class GeneralConfig {

	@SerialEntry
	var enableTips: Boolean = true

	@SerialEntry
	var acceptReparty: Boolean = true

	@SerialEntry
	var shortcuts: Shortcuts = Shortcuts()

	@SerialEntry
	var quiverWarning: QuiverWarning = QuiverWarning()

	@SerialEntry
	var itemList: ItemList = ItemList()

	@SerialEntry
	var itemTooltip: ItemTooltip = ItemTooltip()

	@SerialEntry
	var itemInfoDisplay: ItemInfoDisplay = ItemInfoDisplay()

	@SerialEntry
	var itemProtection: ItemProtection = ItemProtection()

	@SerialEntry
	var wikiLookup: WikiLookup = WikiLookup()

	@SerialEntry
	var specialEffects: SpecialEffects = SpecialEffects()

	@SerialEntry
	var hitbox: Hitbox = Hitbox()

	@SerialEntry
	var lockedSlots: List<Int> = ArrayList()

	//maybe put this 5 somewhere else
	@SerialEntry
	var protectedItems: ObjectOpenHashSet<String> = ObjectOpenHashSet()

	@SerialEntry
	var customItemNames: Object2ObjectOpenHashMap<String, Text> = Object2ObjectOpenHashMap()

	@SerialEntry
	var customDyeColors: Object2IntOpenHashMap<String> = Object2IntOpenHashMap()

	@SerialEntry
	var customArmorTrims: Object2ObjectOpenHashMap<String, ArmorTrimId> = Object2ObjectOpenHashMap()

	@SerialEntry
	var customAnimatedDyes: Object2ObjectOpenHashMap<String, AnimatedDye> = Object2ObjectOpenHashMap()

	class Shortcuts {

		@SerialEntry
		var enableShortcuts: Boolean = true

		@SerialEntry
		var enableCommandShortcuts: Boolean = true

		@SerialEntry
		var enableCommandArgShortcuts: Boolean = true
	}

	class QuiverWarning {

		@SerialEntry
		var enableQuiverWarning: Boolean = true

		@SerialEntry
		var enableQuiverWarningInDungeons: Boolean = true

		@SerialEntry
		var enableQuiverWarningAfterDungeon: Boolean = true
	}

	class ItemList {

		@SerialEntry
		var enableItemList: Boolean = true
	}

	class ItemTooltip {

		@SerialEntry
		var enableNPCPrice: Boolean = true

		@SerialEntry
		var enableMotesPrice: Boolean = true

		@SerialEntry
		var enableAvgBIN: Boolean = true

		@SerialEntry
		var avg: Average = Average.THREE_DAY

		@SerialEntry
		var enableLowestBIN: Boolean = true

		@SerialEntry
		var enableBazaarPrice: Boolean = true

		@SerialEntry
		var enableObtainedDate: Boolean = true

		@SerialEntry
		var enableMuseumInfo: Boolean = true

		@SerialEntry
		var enableExoticTooltip: Boolean = true

		@SerialEntry
		var enableAccessoriesHelper: Boolean = true

		@SerialEntry
		var dungeonQuality: Boolean = true
	}

	enum class Average {
		ONE_DAY, THREE_DAY, BOTH;

		override fun toString(): String {
			return I18n.translate("skyblocker.config.general.itemTooltip.avg.$name")
		}
	}

	class ItemInfoDisplay {

		@SerialEntry
		var attributeShardInfo: Boolean = true

		@SerialEntry
		var itemRarityBackgrounds: Boolean = false

		@SerialEntry
		var itemRarityBackgroundStyle: RarityBackgroundStyle = RarityBackgroundStyle.CIRCULAR

		@SerialEntry
		var itemRarityBackgroundsOpacity: Float = 1f
	}

	enum class RarityBackgroundStyle(val tex: Identifier) {
		CIRCULAR(Identifier(SkyblockerMod.NAMESPACE, "item_rarity_background_circular")),
		SQUARE(Identifier(SkyblockerMod.NAMESPACE, "item_rarity_background_square"));

		override fun toString(): String {
			return when (this) {
				CIRCULAR -> "Circular"
				SQUARE -> "Square"
			}
		}
	}

	class ItemProtection {

		@SerialEntry
		var slotLockStyle: SlotLockStyle = SlotLockStyle.FANCY
	}

	enum class SlotLockStyle(val tex: Identifier) {
		CLASSIC(Identifier(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png")),
		FANCY(Identifier(SkyblockerMod.NAMESPACE, "textures/gui/fancy_slot_lock.png"));

		override fun toString(): String {
			return when (this) {
				CLASSIC -> "Classic"
				FANCY -> "FANCY"
			}
		}
	}

	class WikiLookup {

		@SerialEntry
		var enableWikiLookup: Boolean = true

		@SerialEntry
		var officialWiki: Boolean = true
	}

	class SpecialEffects {

		@SerialEntry
		var rareDungeonDropEffects: Boolean = true
	}

	class Hitbox {

		@SerialEntry
		var oldFarmlandHitbox: Boolean = false

		@SerialEntry
		var oldLeverHitbox: Boolean = false
	}
}
