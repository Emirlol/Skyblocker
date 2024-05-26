package de.hysky.skyblocker.skyblock.tabhud.widget

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.max

/**
 * Abstract base class for a Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
abstract class Widget(title: MutableText, colorValue: Int?) {
	private val components = ArrayList<Component>()
	var width: Int = 0
	var height: Int = 0
	@JvmField
	var x: Int = 0
	@JvmField
	var y: Int = 0
	private val color = -0x1000000 or colorValue
	private val title: Text = title

	fun addComponent(c: Component) {
		components.add(c)
	}

	fun update() {
		components.clear()
		this.updateContent()
		this.pack()
	}

	abstract fun updateContent()

	/**
	 * Shorthand function for simple components.
	 * If the entry at idx has the format "<textA>: <textB>", an IcoTextComponent is
	 * added as such:
	 * [ico] [string] [textB.formatted(fmt)]
	</textB></textA> */
	fun addSimpleIcoText(ico: ItemStack?, string: String?, fmt: Formatting?, idx: Int) {
		val txt = simpleEntryText(idx, string, fmt)
		this.addComponent(IcoTextComponent(ico, txt))
	}

	fun addSimpleIcoText(ico: ItemStack?, string: String?, fmt: Formatting?, content: String?) {
		val txt = simpleEntryText(content, string, fmt)
		this.addComponent(IcoTextComponent(ico, txt))
	}

	/**
	 * Calculate the size of this widget.
	 * **Must be called before returning from the widget constructor and after all
	 * components are added!**
	 */
	private fun pack() {
		height = 0
		width = 0
		for (c in components) {
			height += c.height + Component.Companion.PAD_L
			width = max(width.toDouble(), (c.width + Component.Companion.PAD_S).toDouble()).toInt()
		}

		height -= Component.Companion.PAD_L / 2 // less padding after lowest/last component
		height += BORDER_SZE_N + BORDER_SZE_S - 2
		width += BORDER_SZE_E + BORDER_SZE_W

		// min width is dependent on title
		width = max(width.toDouble(), (BORDER_SZE_W + BORDER_SZE_E + txtRend.getWidth(title) + 4 + 4 + 1).toDouble()).toInt()
	}

	fun setDimensions(size: Int) {
		setDimensions(size, size)
	}

	fun setDimensions(width: Int, height: Int) {
		this.width = width
		this.height = height
	}

	/**
	 * Draw this widget, possibly with a background
	 */
	/**
	 * Draw this widget with a background
	 */
	@JvmOverloads
	fun render(context: DrawContext, hasBG: Boolean = true) {
		val ms = context.matrices

		// not sure if this is the way to go, but it fixes Z-layer issues
		// like blocks being rendered behind the BG and the hotbar clipping into things
		RenderSystem.enableDepthTest()
		ms.push()

		val scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f
		ms.scale(scale, scale, 1f)

		// move above other UI elements
		ms.translate(0f, 0f, 200f)
		if (hasBG) {
			context.fill(x + 1, y, x + width - 1, y + height, COL_BG_BOX)
			context.fill(x, y + 1, x + 1, y + height - 1, COL_BG_BOX)
			context.fill(x + width - 1, y + 1, x + width, y + height - 1, COL_BG_BOX)
		}
		// move above background (if exists)
		ms.translate(0f, 0f, 100f)

		val strHeightHalf = txtRend.fontHeight / 2
		val strAreaWidth = txtRend.getWidth(title) + 4

		context.drawText(txtRend, title, x + 8, y + 2, this.color, false)

		this.drawHLine(context, x + 2, y + 1 + strHeightHalf, 4)
		this.drawHLine(context, x + 2 + strAreaWidth + 4, y + 1 + strHeightHalf, width - 4 - 4 - strAreaWidth)
		this.drawHLine(context, x + 2, y + height - 2, width - 4)

		this.drawVLine(context, x + 1, y + 2 + strHeightHalf, height - 4 - strHeightHalf)
		this.drawVLine(context, x + width - 2, y + 2 + strHeightHalf, height - 4 - strHeightHalf)

		var yOffs = y + BORDER_SZE_N

		for (c in components) {
			c.render(context, x + BORDER_SZE_W, yOffs)
			yOffs += c.height + Component.Companion.PAD_L
		}
		// pop manipulations above
		ms.pop()
		RenderSystem.disableDepthTest()
	}

	private fun drawHLine(context: DrawContext, xpos: Int, ypos: Int, width: Int) {
		context.fill(xpos, ypos, xpos + width, ypos + 1, this.color)
	}

	private fun drawVLine(context: DrawContext, xpos: Int, ypos: Int, height: Int) {
		context.fill(xpos, ypos, xpos + 1, ypos + height, this.color)
	}

	companion object {
		private val txtRend: TextRenderer = MinecraftClient.getInstance().textRenderer

		val BORDER_SZE_N: Int = txtRend.fontHeight + 4
		const val BORDER_SZE_S: Int = 4
		const val BORDER_SZE_W: Int = 4
		const val BORDER_SZE_E: Int = 4
		const val COL_BG_BOX: Int = -0x3ff3f3f4

		/**
		 * If the entry at idx has the format "[textA]: [textB]", the following is
		 * returned:
		 * [entryName] [textB.formatted(contentFmt)]
		 */
		fun simpleEntryText(idx: Int, entryName: String?, contentFmt: Formatting?): Text? {
			var src = PlayerListMgr.strAt(idx) ?: return null

			val cidx = src.indexOf(':')
			if (cidx == -1) {
				return null
			}

			src = src.substring(src.indexOf(':') + 1)
			return simpleEntryText(src, entryName, contentFmt)
		}

		/**
		 * @return [entryName] [entryContent.formatted(contentFmt)]
		 */
		fun simpleEntryText(entryContent: String?, entryName: String?, contentFmt: Formatting?): Text {
			return Text.literal(entryName).append(Text.literal(entryContent).formatted(contentFmt))
		}

		/**
		 * @return the entry at idx as unformatted Text
		 */
		fun plainEntryText(idx: Int): Text? {
			val str = PlayerListMgr.strAt(idx) ?: return null
			return Text.of(str)
		}
	}
}
