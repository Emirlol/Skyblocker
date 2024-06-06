package de.hysky.skyblocker.skyblock.item.tooltip.adders

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class DungeonQualityTooltip(priority: Int) : TooltipAdder(priority) {
	override fun addToTooltip(lines: MutableList<Text>, focusedSlot: Slot) {
		if (!SkyblockerConfigManager.get().general.itemTooltip.dungeonQuality) return
		val customData = getCustomData(focusedSlot.stack)
		if (customData == null || !customData.contains("baseStatBoostPercentage")) return
		val baseStatBoostPercentage = customData.getInt("baseStatBoostPercentage")
		val maxQuality = baseStatBoostPercentage == 50
		if (maxQuality) {
			lines.add(Text.literal(String.format("%-17s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.RED).formatted(Formatting.BOLD))
		} else {
			lines.add(Text.literal(String.format("%-21s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.BLUE))
		}

		if (customData.contains("item_tier")) {     // sometimes it just isn't here?
			val itemTier = customData.getInt("item_tier")
			if (maxQuality) {
				lines.add(Text.literal(String.format("%-17s", "Floor Tier:") + itemTier + " (" + getItemTierFloor(itemTier) + ")").formatted(Formatting.RED).formatted(Formatting.BOLD))
			} else {
				lines.add(Text.literal(String.format("%-21s", "Floor Tier:") + itemTier + " (" + getItemTierFloor(itemTier) + ")").formatted(Formatting.BLUE))
			}
		}
	}

	fun getItemTierFloor(tier: Int): String {
		return when (tier) {
			0 -> "E"
			1 -> "F1"
			2 -> "F2"
			3 -> "F3"
			4 -> "F4/M1"
			5 -> "F5/M2"
			6 -> "F6/M3"
			7 -> "F7/M4"
			8 -> "M5"
			9 -> "M6"
			10 -> "M7"
			else -> "Unknown"
		}
	}
}
