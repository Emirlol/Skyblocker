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
	@kotlin.jvm.JvmField
	@SerialEntry
	var enableTips: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var acceptReparty: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var shortcuts: Shortcuts = Shortcuts()

	@kotlin.jvm.JvmField
	@SerialEntry
	var quiverWarning: QuiverWarning = QuiverWarning()

	@kotlin.jvm.JvmField
	@SerialEntry
	var itemList: ItemList = ItemList()

	@kotlin.jvm.JvmField
	@SerialEntry
	var itemTooltip: ItemTooltip = ItemTooltip()

	@kotlin.jvm.JvmField
	@SerialEntry
	var itemInfoDisplay: ItemInfoDisplay = ItemInfoDisplay()

	@kotlin.jvm.JvmField
	@SerialEntry
	var itemProtection: ItemProtection = ItemProtection()

	@kotlin.jvm.JvmField
	@SerialEntry
	var wikiLookup: WikiLookup = WikiLookup()

	@kotlin.jvm.JvmField
	@SerialEntry
	var specialEffects: SpecialEffects = SpecialEffects()

	@kotlin.jvm.JvmField
	@SerialEntry
	var hitbox: Hitbox = Hitbox()

	@SerialEntry
	var lockedSlots: List<Int> = ArrayList()

	//maybe put this 5 somewhere else
	@SerialEntry
	var protectedItems: ObjectOpenHashSet<String> = ObjectOpenHashSet()

	@kotlin.jvm.JvmField
	@SerialEntry
	var customItemNames: Object2ObjectOpenHashMap<String, Text> = Object2ObjectOpenHashMap()

	@kotlin.jvm.JvmField
	@SerialEntry
	var customDyeColors: Object2IntOpenHashMap<String> = Object2IntOpenHashMap()

	@kotlin.jvm.JvmField
	@SerialEntry
	var customArmorTrims: Object2ObjectOpenHashMap<String, ArmorTrimId> = Object2ObjectOpenHashMap()

	@kotlin.jvm.JvmField
	@SerialEntry
	var customAnimatedDyes: Object2ObjectOpenHashMap<String, AnimatedDye> = Object2ObjectOpenHashMap()

	class Shortcuts {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableShortcuts: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableCommandShortcuts: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableCommandArgShortcuts: Boolean = true
	}


	class QuiverWarning {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableQuiverWarning: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableQuiverWarningInDungeons: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableQuiverWarningAfterDungeon: Boolean = true
	}

	class ItemList {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableItemList: Boolean = true
	}

	class ItemTooltip {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableNPCPrice: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableMotesPrice: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableAvgBIN: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var avg: Average = Average.THREE_DAY

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableLowestBIN: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableBazaarPrice: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableObtainedDate: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableMuseumInfo: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableExoticTooltip: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var enableAccessoriesHelper: Boolean = true

		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
		@SerialEntry
		var attributeShardInfo: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var itemRarityBackgrounds: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var itemRarityBackgroundStyle: RarityBackgroundStyle = RarityBackgroundStyle.CIRCULAR

		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
		@SerialEntry
		var slotLockStyle: SlotLockStyle = SlotLockStyle.FANCY
	}

	enum class SlotLockStyle(@kotlin.jvm.JvmField val tex: Identifier) {
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
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableWikiLookup: Boolean = true

		@kotlin.jvm.JvmField
		@SerialEntry
		var officialWiki: Boolean = true
	}

	class SpecialEffects {
		@kotlin.jvm.JvmField
		@SerialEntry
		var rareDungeonDropEffects: Boolean = true
	}

	class Hitbox {
		@kotlin.jvm.JvmField
		@SerialEntry
		var oldFarmlandHitbox: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var oldLeverHitbox: Boolean = false
	}
}
