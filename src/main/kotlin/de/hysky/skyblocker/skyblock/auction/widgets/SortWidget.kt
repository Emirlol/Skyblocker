package de.hysky.skyblocker.skyblock.auction.widgets

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SortWidget
/**
 * @param x         x position
 * @param y         y position
 * @param clickSlot the parent AuctionsBrowser
 */
	(x: Int, y: Int, clickSlot: SlotClickHandler) : SliderWidget<SortWidget.Option?>(x, y, 36, 9, Text.literal("Sort Widget"), clickSlot, Option.HIGH) {
	enum class Option(textureName: String) : OptionInfo {
		HIGH("high.png"),
		LOW("low.png"),
		SOON("soon.png"),
		RAND("rand.png");

		override val optionTexture: Identifier

		init {
			optionTexture = Identifier(SkyblockerMod.NAMESPACE, prefix + textureName)
		}

		override val isVertical: Boolean
			get() = false

		override val offset: Int
			get() = 5 * ordinal

		override val optionSize: IntArray
			get() = intArrayOf(21, 9)

		companion object {
			private const val prefix = "textures/gui/auctions_gui/sort_widget/"
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
