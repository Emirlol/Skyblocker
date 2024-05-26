package de.hysky.skyblocker.skyblock.tabhud.widget.component

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.max

/**
 * Component that consists of an icon and two lines of text
 */
class IcoFatTextComponent @JvmOverloads constructor(ico: ItemStack? = null, private val line1: Text? = null, private val line2: Text? = null) : Component() {
	private var ico: ItemStack?

	init {
		this.ico = if ((ico == null)) Ico.BARRIER else ico

		if (line1 == null || line2 == null) {
			this.ico = Ico.BARRIER
			this.line1 = Text.literal("No data").formatted(Formatting.GRAY)
			this.line2 = Text.literal("No data").formatted(Formatting.GRAY)
		}

		this.width = (Component.Companion.ICO_DIM + Component.Companion.PAD_L + max(Component.Companion.txtRend.getWidth(this.line1).toDouble(), Component.Companion.txtRend.getWidth(this.line2).toDouble())).toInt()
		this.height = Component.Companion.txtRend.fontHeight + Component.Companion.PAD_S + Component.Companion.txtRend.fontHeight
	}

	override fun render(context: DrawContext, x: Int, y: Int) {
		context.drawItem(ico, x, y + ICO_OFFS)
		context.drawText(Component.Companion.txtRend, line1, x + Component.Companion.ICO_DIM + Component.Companion.PAD_L, y, -0x1, false)
		context.drawText(Component.Companion.txtRend, line2, x + Component.Companion.ICO_DIM + Component.Companion.PAD_L, y + Component.Companion.txtRend.fontHeight + Component.Companion.PAD_S, -0x1, false)
	}

	companion object {
		private const val ICO_OFFS = 1
	}
}
