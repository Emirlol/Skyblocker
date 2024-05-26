package de.hysky.skyblocker.skyblock.fancybars

import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.*
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color
import java.util.function.Consumer

class EditBarColorPopup(title: Text?, backgroundScreen: Screen?, private val setColor: Consumer<Color>) : AbstractPopupScreen(title, backgroundScreen!!) {
	private var layout: DirectionalLayoutWidget = DirectionalLayoutWidget.vertical()
	private var colorSelector: BasicColorSelector? = null

	override fun init() {
		super.init()
		layout = DirectionalLayoutWidget.vertical()
		layout.spacing(8).mainPositioner.alignHorizontalCenter()
		layout.add(TextWidget(title.copy().fillStyle(Style.EMPTY.withBold(true)), MinecraftClient.getInstance().textRenderer))
		colorSelector = BasicColorSelector(0, 0, 150) { done(null) }
		layout.add(colorSelector)

		val horizontal = DirectionalLayoutWidget.horizontal()
		val buttonWidget = ButtonWidget.builder(Text.literal("Cancel")) { button: ButtonWidget? -> close() }.width(80).build()
		horizontal.add(buttonWidget)
		horizontal.add(ButtonWidget.builder(Text.literal("Done")) { `object`: ButtonWidget? -> this.done(`object`) }.width(80).build())

		layout.add(horizontal)
		layout.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
		layout.refreshPositions()
		SimplePositioningWidget.setPos(layout, this.navigationFocus)
	}

	private fun done(`object`: Any?) {
		if (colorSelector!!.validColor) setColor.accept(Color(colorSelector.getColor()))
		close()
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderBackground(context, mouseX, mouseY, delta)
		drawPopupBackground(context, layout.x, layout.y, layout.width, layout.height)
	}

	private class BasicColorSelector(x: Int, y: Int, width: Int, onEnter: Runnable?) : ContainerWidget(x, y, width, 15, Text.literal("edit color")) {
		private val textFieldWidget = EnterConfirmTextFieldWidget(MinecraftClient.getInstance().textRenderer, getX() + 16, getY(), width - 16, 15, Text.empty(), onEnter!!)

		override fun children(): List<Element?> {
			return java.util.List.of(textFieldWidget)
		}

		var color: Int = -0x1000000
			private set
		var validColor: Boolean = false

		init {
			textFieldWidget.setChangedListener { text: String -> this.onTextChange(text) }
			textFieldWidget.setTextPredicate { s: String -> s.length <= 6 }
		}

		private fun onTextChange(text: String) {
			try {
				color = text.toInt(16) or -0x1000000
				validColor = true
			} catch (e: NumberFormatException) {
				color = 0
				validColor = false
			}
		}

		override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			context.drawBorder(x, y, 15, 15, if (validColor) -1 else -0x230000)
			context.fill(x + 1, y + 1, x + 14, y + 14, color)
			textFieldWidget.renderWidget(context, mouseX, mouseY, delta)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		}

		override fun setX(x: Int) {
			super.setX(x)
			textFieldWidget.x = getX() + 16
		}

		override fun setY(y: Int) {
			super.setY(y)
			textFieldWidget.y = getY()
		}
	}
}
