package de.hysky.skyblocker.skyblock.item.tooltip

import de.hysky.skyblocker.utils.Constants
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.StringIdentifiable

object ExoticTooltip {
	fun getExpectedHex(id: String?): String? {
		val color = TooltipInfoType.COLOR.data!![id].asString
		if (color != null) {
			val RGBValues = color.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			return String.format("%02X%02X%02X", RGBValues[0].toInt(), RGBValues[1].toInt(), RGBValues[2].toInt())
		} else {
			ItemTooltip.LOGGER.warn("[Skyblocker Exotics] No expected color data found for id {}", id)
			return null
		}
	}

	fun isException(id: String, hex: String?): Boolean {
		if (id.startsWith("LEATHER") || id == "GHOST_BOOTS" || Constants.SEYMOUR_IDS.contains(id)) {
			return true
		}
		if (id.startsWith("RANCHER")) {
			return Constants.RANCHERS.contains(hex)
		}
		if (id.contains("ADAPTIVE_CHESTPLATE")) {
			return Constants.ADAPTIVE_CHEST.contains(hex)
		} else if (id.contains("ADAPTIVE")) {
			return Constants.ADAPTIVE.contains(hex)
		}
		if (id.startsWith("REAPER")) {
			return Constants.REAPER.contains(hex)
		}
		if (id.startsWith("FAIRY")) {
			return Constants.FAIRY_HEXES.contains(hex)
		}
		if (id.startsWith("CRYSTAL")) {
			return Constants.CRYSTAL_HEXES.contains(hex)
		}
		if (id.contains("SPOOK")) {
			return Constants.SPOOK.contains(hex)
		}
		return false
	}

	fun checkDyeType(hex: String?): DyeType {
		if (Constants.CRYSTAL_HEXES.contains(hex)) {
			return DyeType.CRYSTAL
		}
		if (Constants.FAIRY_HEXES.contains(hex)) {
			return DyeType.FAIRY
		}
		if (Constants.OG_FAIRY_HEXES.contains(hex)) {
			return DyeType.OG_FAIRY
		}
		if (Constants.SPOOK.contains(hex)) {
			return DyeType.SPOOK
		}
		if (Constants.GLITCHED.contains(hex)) {
			return DyeType.GLITCHED
		}
		return DyeType.EXOTIC
	}

	fun intendedDyed(customData: NbtCompound): Boolean {
		return customData.contains("dye_item")
	}

	enum class DyeType(override val name: String, private val formatting: Formatting) : StringIdentifiable {
		CRYSTAL("crystal", Formatting.AQUA),
		FAIRY("fairy", Formatting.LIGHT_PURPLE),
		OG_FAIRY("og_fairy", Formatting.DARK_PURPLE),
		SPOOK("spook", Formatting.RED),
		GLITCHED("glitched", Formatting.BLUE),
		EXOTIC("exotic", Formatting.GOLD);

		override fun asString(): String {
			return name
		}

		val translatedText: MutableText
			get() = Text.translatable("skyblocker.exotic.$name").formatted(formatting)
	}
}
