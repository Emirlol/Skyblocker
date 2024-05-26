package de.hysky.skyblocker.skyblock.auction.widgets

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

// This is kinda excessive, but I thought it was a good idea
open class SliderWidget<E>(x: Int, y: Int, width: Int, height: Int, message: Text?, private val clickSlot: SlotClickHandler, protected var current: E) : ClickableWidget(x, y, width, height, message) where E : Enum<E>?, E : SliderWidget.OptionInfo? {
	private var button = 0
	private var slotId = -1

	var posProgress: Float

	/**
	 * @param x             x position
	 * @param y             y position
	 * @param width         width
	 * @param height        height
	 * @param message       probably useless, just put the widget name
	 * @param clickSlot     the parent AuctionsBrowser
	 * @param defaultOption the default option **should be the one at ordinal 0**
	 */
	init {
		posProgress = current!!.offset.toFloat()
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (posProgress < current!!.offset) {
			posProgress += delta * 5
			if (posProgress > current!!.offset) posProgress = current!!.offset.toFloat()
		} else if (posProgress > current!!.offset) {
			posProgress -= delta * 5
			if (posProgress < current!!.offset) posProgress = current!!.offset.toFloat()
		}


		context.matrices.push()
		context.matrices.translate(x.toFloat(), y.toFloat(), 0f)

		val x = if (current!!.isVertical) 0 else Math.round(posProgress)
		val y = if (current!!.isVertical) Math.round(posProgress) else 0

		val optionWidth = current!!.optionSize[0]
		val optionHeight = current!!.optionSize[1]

		context.drawTexture(current!!.backTexture, 0, 0, 0f, 0f, getWidth(), getHeight(), getWidth(), getHeight())
		context.drawTexture(current!!.optionTexture, x, y, 0f, 0f, optionWidth, optionHeight, optionWidth, optionHeight)
		if (isHovered) {
			context.drawTexture(current!!.hoverTexture, x, y, 0f, 0f, optionWidth, optionHeight, optionWidth, optionHeight)
		}
		context.matrices.pop()
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		if (slotId == -1) return
		clickSlot.click(slotId, button)
		super.onClick(mouseX, mouseY)
	}

	override fun isValidClickButton(button: Int): Boolean {
		this.button = button
		return super.isValidClickButton(button) || button == 1
	}

	fun setSlotId(slotId: Int) {
		this.slotId = slotId
	}

	fun setCurrent(current: E) {
		this.current = current
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
	}

	interface OptionInfo {
		val isVertical: Boolean

		/**
		 * @return The current option's position offset from the first option's position
		 */
		val offset: Int

		val optionSize: IntArray

		val optionTexture: Identifier

		val backTexture: Identifier

		val hoverTexture: Identifier
	}
}
