package de.hysky.skyblocker.skyblock.auction.widgets

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AuctionTypeWidget
/**
 * @param x         x position
 * @param y         y position
 * @param slotClick IDK figure it out
 */
	(x: Int, y: Int, slotClick: SlotClickHandler) : SliderWidget<AuctionTypeWidget.Option?>(x, y, 17, 17, Text.literal("Auction Type Widget"), slotClick, Option.ALL) {
	enum class Option(textureName: String) : OptionInfo {
		ALL("all.png"),
		BIN("bin.png"),
		AUC("auctions.png");

		override val optionTexture: Identifier

		init {
			optionTexture = Identifier(SkyblockerMod.NAMESPACE, prefix + textureName)
		}

		override val isVertical: Boolean
			get() = true

		override val offset: Int
			get() = 4 * ordinal

		override val optionSize: IntArray
			get() = intArrayOf(17, 9)

		companion object {
			private const val prefix = "textures/gui/auctions_gui/auction_type_widget/"
			val hoverTexture: Identifier = Identifier(SkyblockerMod.NAMESPACE, prefix + "hover.png")
				get() = Companion.field
			val backTexture: Identifier = Identifier(SkyblockerMod.NAMESPACE, prefix + "back.png")
				get() = Companion.field

			private val values = entries.toTypedArray()

			@JvmStatic
            fun get(ordinal: Int): Option {
				return values[Math.clamp(ordinal.toLong(), 0, values.size - 1)]
			}
		}
	}
}
