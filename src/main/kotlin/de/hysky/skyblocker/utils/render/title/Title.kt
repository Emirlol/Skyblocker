package de.hysky.skyblocker.utils.render.title

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Represents a title used for [TitleContainer].
 *
 * @see TitleContainer
 */
class Title
/**
 * Constructs a new title with the given [MutableText].
 * Use [Text.literal] or [Text.translatable] to create a [MutableText]
 *
 * @param text the mutable text
 */(@JvmField var text: MutableText) {
	var x: Float = -1f
	var y: Float = -1f

	/**
	 * Constructs a new title with the given translation key and formatting to be applied.
	 *
	 * @param textKey    the translation key
	 * @param formatting the formatting to be applied to the text
	 */
	constructor(textKey: String?, formatting: Formatting?) : this(Text.translatable(textKey).formatted(formatting))

	val isDefaultPos: Boolean
		get() = x == -1f && y == -1f

	fun resetPos() {
		this.x = -1f
		this.y = -1f
	}
}
