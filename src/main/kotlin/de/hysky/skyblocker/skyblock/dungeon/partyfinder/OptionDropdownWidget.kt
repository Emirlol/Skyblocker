package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import kotlin.math.min

class OptionDropdownWidget(protected val screen: PartyFinderScreen, name: Text, selectedOption: Option?, x: Int, y: Int, width: Int, height: Int, private val slotId: Int) : ElementListWidget<OptionDropdownWidget.Option?>(screen.client, width, height, y, 15) {
	private var backButtonId = -1
	private val name: Text
	private var selectedOption: Option?
	private var isOpen = false

	private var animationProgress = 0f

	init {
		setX(x)
		setRenderHeader(true, 25)
		this.name = name
		this.selectedOption = selectedOption
	}

	override fun clickedHeader(x: Int, y: Int): Boolean {
		if (!(x >= 0 && y >= 10 && x < getWidth() && y < 26)) return false
		if (screen.isWaitingForServer) return false
		if (isOpen) {
			if (backButtonId != -1) screen.clickAndWaitForServer(backButtonId)
		} else {
			screen.clickAndWaitForServer(slotId)
			screen.partyFinderButton!!.active = false
		}
		animationProgress = 0f
		return true
	}

	override fun getRowLeft(): Int {
		return x + 2
	}

	override fun getScrollbarX(): Int {
		return rowLeft + rowWidth
	}

	override fun getRowWidth(): Int {
		return getWidth() - 6
	}

	fun setSelectedOption(entry: Option) {
		selectedOption = entry
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!screen.settingsContainer.canInteract(this)) return false
		if (isOpen && !isMouseOver(mouseX, mouseY) && backButtonId != -1) {
			screen.clickAndWaitForServer(backButtonId)
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (screen.settingsContainer.canInteract(this)) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		return false
	}

	override fun renderHeader(context: DrawContext, x: Int, y: Int) {
		context.drawText(MinecraftClient.getInstance().textRenderer, name, x, y + 1, -0x2f2f30, false)
		val offset = 10
		context.fill(x - 2, y + offset, x - 3 + getWidth(), y + 15 + offset, -0xf0f10)
		context.fill(x - 1, y + 1 + offset, x - 3 + getWidth() - 1, y + 14 + offset, -0x1000000)
		if (selectedOption != null) {
			context.drawText(MinecraftClient.getInstance().textRenderer, selectedOption!!.message, x + 2, y + 3 + offset, -0x1, true)
		} else context.drawText(MinecraftClient.getInstance().textRenderer, "???", x + 2, y + 3 + offset, -0x1, true)
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val matrices = context.matrices
		if (isOpen) {
			matrices.push()
			matrices.translate(0f, 0f, 100f)
		}
		if (animationProgress < 1) animationProgress += delta * 0.5f
		else if (animationProgress != 1f) animationProgress = 1f
		if (PartyFinderScreen.Companion.DEBUG) {
			context.drawText(MinecraftClient.getInstance().textRenderer, slotId.toString(), x, y - 10, -0x10000, true)
			context.drawText(MinecraftClient.getInstance().textRenderer, backButtonId.toString(), x + 50, y - 10, -0x10000, true)
		}

		val height1 = min(getHeight().toDouble(), (entryCount * itemHeight + 4).toDouble()).toInt()
		val idk = if (isOpen) (height1 * animationProgress).toInt() else (height1 * (1 - animationProgress)).toInt()
		context.fill(x, y + headerHeight, x + getWidth() - 1, y + idk + headerHeight, -0x1f1f20)
		context.fill(x + 1, y + headerHeight + 1, x + getWidth() - 2, y + idk + headerHeight - 1, -0x1000000)

		super.renderWidget(context, mouseX, mouseY, delta)
		if (isOpen) {
			matrices.pop()
		}
	}

	override fun drawHeaderAndFooterSeparators(context: DrawContext) {
	}

	override fun drawMenuListBackground(context: DrawContext) {
	}

	fun open(entries: List<Option>?, backButtonId: Int) {
		isOpen = true
		this.replaceEntries(entries)
		animationProgress = 0f
		this.backButtonId = backButtonId
	}

	fun close() {
		isOpen = false
		this.clearEntries()
	}

	inner class Option(val message: String, private val icon: ItemStack?, private val optionSlotId: Int) : Entry<Option?>() {
		override fun selectableChildren(): List<Selectable> {
			returnemptyList()
		}

		override fun children(): List<Element> {
			returnemptyList()
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			/*if (hovered) {
                context.fill(x, y, x + entryWidth, y + 13, 0xFFF0F0F0);
                context.fill(x+1, y+1, x + entryWidth-1, y + 12, 0xFF000000);
            } else context.fill(x, y, x + entryWidth, y + 13, 0xFF000000);*/
			val matrices = context.matrices
			matrices.push()
			val iconY = y + 1
			matrices.translate(x.toFloat(), iconY.toFloat(), 0f)
			matrices.scale(0.8f, 0.8f, 1f)
			matrices.translate(-x.toFloat(), -iconY.toFloat(), 0f)
			context.drawItem(icon, x, iconY)
			matrices.pop()
			if (PartyFinderScreen.Companion.DEBUG) context.drawText(MinecraftClient.getInstance().textRenderer, optionSlotId.toString(), x + 8, y, -0x10000, true)
			context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(message).fillStyle(Style.EMPTY.withUnderline(hovered)), x + 14, y + 3, -0x1, false)
		}

		override fun equals(o: Any?): Boolean {
			if (this === o) return true
			if (o == null || javaClass != o.javaClass) return false

			val that = o as Option

			return message == that.message
		}

		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (screen.isWaitingForServer) return false
			if (button == 0) {
				screen.clickAndWaitForServer(this.optionSlotId)
				setSelectedOption(this)
			}
			return true
		}

		override fun hashCode(): Int {
			return message.hashCode()
		}
	}
}
