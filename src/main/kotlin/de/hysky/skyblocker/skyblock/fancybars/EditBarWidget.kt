package de.hysky.skyblocker.skyblock.fancybars

import de.hysky.skyblocker.skyblock.fancybars.StatusBar.IconPosition
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.max

class EditBarWidget(x: Int, y: Int, parent: Screen?) : ContainerWidget(x, y, 100, 66, Text.literal("Edit bar")) {
	private val iconOption: EnumCyclingOption<IconPosition?>
	private val booleanOption: BooleanOption

	private val color1: ColorOption
	private val color2: ColorOption
	private val textColor: ColorOption
	private val nameWidget: TextWidget

	private var contentsWidth = 0

	override fun children(): List<Element?> {
		return java.util.List.of(iconOption, booleanOption, color1, color2, textColor)
	}

	var insideMouseX: Int = 0
	var insideMouseY: Int = 0

	init {
		val textRenderer = MinecraftClient.getInstance().textRenderer

		nameWidget = TextWidget(Text.empty(), textRenderer)

		var translatable = Text.translatable("skyblocker.bars.config.icon")
		contentsWidth = max(contentsWidth.toDouble(), (textRenderer.getWidth(translatable) + textRenderer.getWidth("RIGHT") + 10).toDouble()).toInt()
		iconOption = EnumCyclingOption(0, 11, getWidth(), translatable, IconPosition::class.java)

		translatable = Text.translatable("skyblocker.bars.config.showValue")
		contentsWidth = max(contentsWidth.toDouble(), (textRenderer.getWidth(translatable) + 9 + 10).toDouble()).toInt()
		booleanOption = BooleanOption(0, 22, getWidth(), translatable)

		// COLO(u)RS
		translatable = Text.translatable("skyblocker.bars.config.mainColor")
		contentsWidth = max(contentsWidth.toDouble(), (textRenderer.getWidth(translatable) + 9 + 10).toDouble()).toInt()
		color1 = ColorOption(0, 33, getWidth(), translatable, parent)

		translatable = Text.translatable("skyblocker.bars.config.overflowColor")
		contentsWidth = max(contentsWidth.toDouble(), (textRenderer.getWidth(translatable) + 9 + 10).toDouble()).toInt()
		color2 = ColorOption(0, 44, getWidth(), translatable, parent)

		translatable = Text.translatable("skyblocker.bars.config.textColor")
		contentsWidth = max(contentsWidth.toDouble(), (textRenderer.getWidth(translatable) + 9 + 10).toDouble()).toInt()
		textColor = ColorOption(0, 55, getWidth(), translatable, parent)

		setWidth(contentsWidth)
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (isHovered) {
			insideMouseX = mouseX
			insideMouseY = mouseY
		} else {
			val i = mouseX - insideMouseX
			val j = mouseY - insideMouseY
			if (i * i + j * j > 30 * 30) visible = false
		}
		TooltipBackgroundRenderer.render(context, x, y, getWidth(), getHeight(), 0)
		val matrices = context.matrices
		matrices.push()
		matrices.translate(x.toFloat(), y.toFloat(), 0f)
		nameWidget.render(context, mouseX, mouseY, delta)
		iconOption.renderWidget(context, mouseX - x, mouseY - y, delta)
		booleanOption.renderWidget(context, mouseX - x, mouseY - y, delta)
		color1.renderWidget(context, mouseX - x, mouseY - y, delta)
		color2.renderWidget(context, mouseX - x, mouseY - y, delta)
		textColor.renderWidget(context, mouseX - x, mouseY - y, delta)
		matrices.pop()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!visible) return false
		if (!isHovered) visible = false
		return super.mouseClicked(mouseX - x, mouseY - y, button)
	}

	fun setStatusBar(statusBar: StatusBar?) {
		iconOption.setCurrent(statusBar.getIconPosition())
		iconOption.setOnChange { iconPosition: IconPosition? -> statusBar.setIconPosition(iconPosition) }
		booleanOption.setCurrent(statusBar!!.showText())
		booleanOption.setOnChange { showText: Boolean -> statusBar.setShowText(showText) }

		color1.setCurrent(statusBar.colors[0].rgb)
		color1.setOnChange { color: Color? -> statusBar.colors[0] = color }

		color2.active = statusBar.hasOverflow()
		if (color2.active) {
			color2.setCurrent(statusBar.colors[1].rgb)
			color2.setOnChange { color: Color? -> statusBar.colors[1] = color }
		}

		if (statusBar.textColor != null) {
			textColor.setCurrent(statusBar.textColor.getRGB())
		}
		textColor.setOnChange { textColor: Color? -> statusBar.textColor = textColor }

		val formatted = statusBar.name.copy().formatted(Formatting.BOLD)
		nameWidget.message = formatted
		setWidth(max(MinecraftClient.getInstance().textRenderer.getWidth(formatted).toDouble(), contentsWidth.toDouble()).toInt())
	}

	override fun setWidth(width: Int) {
		super.setWidth(width)
		iconOption.width = width
		booleanOption.width = width
		color1.width = width
		color2.width = width
		textColor.width = width
		nameWidget.width = width
	}

	class EnumCyclingOption<T : Enum<T>?>(x: Int, y: Int, width: Int, message: Text?, enumClass: Class<T>) : ClickableWidget(x, y, width, 11, message) {
		private var current: T
		private val values: Array<T> = enumClass.enumConstants
		private var onChange: Consumer<T>? = null

		init {
			current = values[0]
		}

		public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
				context.fill(x, y, right, bottom, 0x20FFFFFF)
			}
			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawText(textRenderer, message, x + 1, y + 1, -1, true)
			val string = current.toString()
			context.drawText(textRenderer, string, right - textRenderer.getWidth(string) - 1, y + 1, -1, true)
		}

		fun setCurrent(current: T) {
			this.current = current
		}

		override fun onClick(mouseX: Double, mouseY: Double) {
			current = values[(current!!.ordinal + 1) % values.size]
			if (onChange != null) onChange!!.accept(current)
			super.onClick(mouseX, mouseY)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		}

		fun setOnChange(onChange: Consumer<T>?) {
			this.onChange = onChange
		}
	}

	class BooleanOption(x: Int, y: Int, width: Int, message: Text?) : ClickableWidget(x, y, width, 11, message) {
		private var current = false
		private var onChange: Consumer<Boolean>? = null

		public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
				context.fill(x, y, right, bottom, 0x20FFFFFF)
			}
			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawText(textRenderer, message, x + 1, y + 1, -1, true)
			context.drawBorder(right - 10, y + 1, 9, 9, -1)
			if (current) context.fill(right - 8, y + 3, right - 3, y + 8, -1)
		}

		override fun onClick(mouseX: Double, mouseY: Double) {
			current = !current
			if (onChange != null) onChange!!.accept(current)
			super.onClick(mouseX, mouseY)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		}

		fun setCurrent(current: Boolean) {
			this.current = current
		}

		fun setOnChange(onChange: Consumer<Boolean>?) {
			this.onChange = onChange
		}
	}

	class ColorOption(x: Int, y: Int, width: Int, message: Text?, private val parent: Screen?) : ClickableWidget(x, y, width, 11, message) {
		fun setCurrent(current: Int) {
			this.current = current
		}

		private var current = 0
		private var onChange: Consumer<Color?>? = null

		public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
				context.fill(x, y, right, bottom, 0x20FFFFFF)
			}
			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawText(textRenderer, message, x + 1, y + 1, if (active) -1 else Colors.GRAY, true)
			context.drawBorder(right - 10, y + 1, 9, 9, if (active) -1 else Colors.GRAY)
			context.fill(right - 8, y + 3, right - 3, y + 8, if (active) current else Colors.GRAY)
		}

		override fun onClick(mouseX: Double, mouseY: Double) {
			super.onClick(mouseX, mouseY)
			MinecraftClient.getInstance().setScreen(EditBarColorPopup(Text.literal("Edit ").append(message), parent) { color: Color -> this.set(color) })
		}

		private fun set(color: Color) {
			current = color.rgb
			if (onChange != null) onChange!!.accept(color)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		}

		fun setOnChange(onChange: Consumer<Color?>?) {
			this.onChange = onChange
		}
	}
}
