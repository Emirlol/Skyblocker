package de.hysky.skyblocker.skyblock.tabhud.widget.component

import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Component that consists of a line of text.
 */
class PlainTextComponent(private val text: Text?) : Component() {
	init {
		if (text == null) {
			this.text = Text.literal("No data").formatted(Formatting.GRAY)
		}

		this.width = Component.Companion.PAD_S + Component.Companion.txtRend.getWidth(this.text) // looks off without padding
		this.height = Component.Companion.txtRend.fontHeight
	}

	override fun render(context: DrawContext, x: Int, y: Int) {
		context.drawText(Component.Companion.txtRend, text, x + Component.Companion.PAD_S, y, -0x1, false)
	}
}
