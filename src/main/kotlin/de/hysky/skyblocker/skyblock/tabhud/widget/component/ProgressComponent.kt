package de.hysky.skyblocker.skyblock.tabhud.widget.component

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.max

/**
 * Component that consists of an icon, some text and a progress bar.
 * The progress bar either shows the fill percentage or custom text.
 * NOTICE: pcnt is 0-100, not 0-1!
 */
class ProgressComponent @JvmOverloads constructor(ico: ItemStack? = null, d: Text? = null, b: Text? = null, pcnt: Float = 100f, color: Int = 0) : Component() {
	private var ico: ItemStack? = null
	private var desc: Text? = null
	private var bar: Text? = null
	private var pcnt = 0f
	private var color = 0
	private val barW: Int

	init {
		if (d == null || b == null) {
			this.ico = Ico.BARRIER
			this.desc = Text.literal("No data").formatted(Formatting.GRAY)
			this.bar = Text.literal("---").formatted(Formatting.GRAY)
			this.pcnt = 100f
			this.color = -0x1000000 or Formatting.DARK_GRAY.colorValue!!
		} else {
			this.ico = if ((ico == null)) Ico.BARRIER else ico
			this.desc = d
			this.bar = b
			this.pcnt = Math.clamp(pcnt, 0f, 100f)
			this.color = -0x1000000 or color
		}

		this.barW = BAR_WIDTH
		this.width = (Component.Companion.ICO_DIM + Component.Companion.PAD_L + max(barW.toDouble(), Component.Companion.txtRend.getWidth(this.desc).toDouble())).toInt()
		this.height = Component.Companion.txtRend.fontHeight + Component.Companion.PAD_S + 2 + Component.Companion.txtRend.fontHeight + 2
	}

	constructor(ico: ItemStack?, text: Text?, pcnt: Float, color: Int) : this(ico, text, Text.of("$pcnt%"), pcnt, color)

	override fun render(context: DrawContext, x: Int, y: Int) {
		context.drawItem(ico, x, y + ICO_OFFS)
		context.drawText(Component.Companion.txtRend, desc, x + Component.Companion.ICO_DIM + Component.Companion.PAD_L, y, -0x1, false)

		val barX: Int = x + Component.Companion.ICO_DIM + Component.Companion.PAD_L
		val barY: Int = y + Component.Companion.txtRend.fontHeight + Component.Companion.PAD_S
		val endOffsX = ((this.barW * (this.pcnt / 100f)).toInt())
		context.fill(barX + endOffsX, barY, barX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR)
		context.fill(
			barX, barY, barX + endOffsX, barY + BAR_HEIGHT,
			this.color
		)
		context.drawTextWithShadow(Component.Companion.txtRend, bar, barX + 3, barY + 2, -0x1)
	}

	companion object {
		private const val BAR_WIDTH = 100
		private val BAR_HEIGHT: Int = Component.Companion.txtRend.fontHeight + 3
		private const val ICO_OFFS = 4
		private const val COL_BG_BAR = -0xfefeff0
	}
}
