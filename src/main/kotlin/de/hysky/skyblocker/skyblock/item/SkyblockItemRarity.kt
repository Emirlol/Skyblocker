package de.hysky.skyblocker.skyblock.item

import net.minecraft.util.Formatting

enum class SkyblockItemRarity(formatting: Formatting) {
	ADMIN(Formatting.DARK_RED),
	ULTIMATE(Formatting.DARK_RED),
	VERY_SPECIAL(Formatting.RED),
	SPECIAL(Formatting.RED),
	DIVINE(Formatting.AQUA),
	MYTHIC(Formatting.LIGHT_PURPLE),
	LEGENDARY(Formatting.GOLD),
	EPIC(Formatting.DARK_PURPLE),
	RARE(Formatting.BLUE),
	UNCOMMON(Formatting.GREEN),
	COMMON(Formatting.WHITE);

	@JvmField
	val color: Int = formatting.colorValue!!

	val r: Float = ((color shr 16) and 0xFF) / 255f
	val g: Float = ((color shr 8) and 0xFF) / 255f
	val b: Float = (color and 0xFF) / 255f
}
