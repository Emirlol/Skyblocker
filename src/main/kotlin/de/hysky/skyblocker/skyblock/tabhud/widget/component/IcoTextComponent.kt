package de.hysky.skyblocker.skyblock.tabhud.widget.component

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Component that consists of an icon and a line of text.
 */
class IcoTextComponent @JvmOverloads constructor(ico: ItemStack? = null, private val text: Text? = null) : Component() {
	private var ico: ItemStack?

	init {
		this.ico = if ((ico == null)) Ico.BARRIER else ico

		if (text == null) {
			this.ico = Ico.BARRIER
			this.text = Text.literal("No data").formatted(Formatting.GRAY)
		}

		this.width = Component.Companion.ICO_DIM + Component.Companion.PAD_L + Component.Companion.txtRend.getWidth(this.text)
		this.height = Component.Companion.ICO_DIM
	}

	override fun render(context: DrawContext, x: Int, y: Int) {
		context.drawItem(ico, x, y)
		context.drawText(Component.Companion.txtRend, text, x + Component.Companion.ICO_DIM + Component.Companion.PAD_L, y + 5, -0x1, false)
	}
}
