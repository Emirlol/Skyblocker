package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket
import net.minecraft.text.Text
import java.util.*

class RangedValueWidget(private val screen: PartyFinderScreen, private val name: Text, x: Int, y: Int, width: Int, private val slotId: Int) : ContainerWidget(x, y, width, 45, Text.empty()) {
	private var minSlotId = -1
	private var maxSlotId = -1
	private var backSlotId = -1

	private var min = -1
	private var max = -1

	private var state = State.CLOSED

	private val input = ModifiedTextFieldWidget(MinecraftClient.getInstance().textRenderer, x, y + 25, width - 15, 15, Text.empty())
	private val okButton: ButtonWidget

	init {
		input.isVisible = false
		input.setMaxLength(3)
		input.setChangedListener { string: String -> this.updateConfirmButton(string) }
		this.okButton = ButtonWidget.builder(Text.literal("✔")) { a: ButtonWidget? -> sendPacket() }
			.dimensions(x + width - 15, y + 25, 15, 15)
			.build()
		okButton.visible = false
	}

	override fun children(): List<Element> {
		return java.util.List.of(this.input, this.okButton)
	}

	fun updateConfirmButton(string: String) {
		try {
			val i = string.trim { it <= ' ' }.toInt()
			if (i < 0 || i > 999) { // Too beeg or too smol
				okButton.active = false
				input.setGood(false)
			} else if (state == State.MODIFYING_MIN && i > max) { // If editing min and bigger than max
				okButton.active = false
				input.setGood(false)
			} else { // If editing max and smaller than min
				val active1 = state != State.MODIFYING_MAX || i >= min
				okButton.active = active1
				input.setGood(active1)
			}
		} catch (e: NumberFormatException) {
			okButton.active = false
			input.setGood(false)
		}
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		context.drawText(MinecraftClient.getInstance().textRenderer, name, x, y, -0x2f2f30, false)
		val textOffset = 10
		val matrices = context.matrices
		if (!visible) return
		if (state != State.CLOSED) {
			matrices.push()
			matrices.translate(0f, 0f, 100f)
		}
		val textRenderer = screen.client.textRenderer
		if (PartyFinderScreen.Companion.DEBUG) {
			context.drawText(textRenderer, slotId.toString(), x, y - 10, -0x10000, true)
			context.drawText(textRenderer, minSlotId.toString(), x + 20, y - 10, -0x10000, true)
			context.drawText(textRenderer, maxSlotId.toString(), x + 40, y - 10, -0x10000, true)
			context.drawText(textRenderer, backSlotId.toString(), x + 60, y - 10, -0x10000, true)
		}
		input.render(context, mouseX, mouseY, delta)
		okButton.render(context, mouseX, mouseY, delta)
		if (Objects.requireNonNull<State>(this.state) == State.CLOSED) {
			context.fill(x, y + textOffset, x + width, y + 15 + textOffset, -0x1)
			context.fill(x + 1, y + 1 + textOffset, x + width - 1, y + 14 + textOffset, -0x1000000)
			context.drawText(textRenderer, "$min - $max", x + 3, y + 3 + textOffset, -0x1, false)
		} else {
			context.fill(x, y + textOffset, x + width, y + 15 + textOffset, -0x1)
			context.fill(x + 1, y + 1 + textOffset, x + width - 1, y + 14 + textOffset, -0x1000000)
			context.drawCenteredTextWithShadow(textRenderer, "-", x + (width shr 1), y + 3 + textOffset, -0x1)
			val selectedColor = -0x100
			val unselectedColor = -0x2f2f30

			val mouseOverMin = mouseOverMinButton(mouseX, mouseY)
			val mouseOverMax = mouseOverMaxButton(mouseX, mouseY)

			// Minimum
			val minStartX = x + 1
			val minEndX = x + (width shr 1) - 6
			context.fill(minStartX, y + 1 + textOffset, minEndX, y + 14 + textOffset, if (state == State.MODIFYING_MIN) selectedColor else (if (mouseOverMin) -0x1 else unselectedColor))
			context.fill(minStartX + 1, y + 2 + textOffset, minEndX - 1, y + 13 + textOffset, -0x1000000)

			context.drawCenteredTextWithShadow(textRenderer, min.toString(), (minStartX + minEndX) shr 1, y + 3 + textOffset, -0x1)

			// Maximum
			val maxStartX = x + (width shr 1) + 5
			val maxEndX = x + width - 1
			context.fill(maxStartX, y + 1 + textOffset, maxEndX, y + 14 + textOffset, if (state == State.MODIFYING_MAX) selectedColor else (if (mouseOverMax) -0x1 else unselectedColor))
			context.fill(maxStartX + 1, y + 2 + textOffset, maxEndX - 1, y + 13 + textOffset, -0x1000000)

			context.drawCenteredTextWithShadow(textRenderer, max.toString(), (maxStartX + maxEndX) shr 1, y + 3 + textOffset, -0x1)
		}
		if (state != State.CLOSED) {
			matrices.pop()
		}
	}

	private fun mouseOverMinButton(mouseX: Int, mouseY: Int): Boolean {
		return isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && mouseX < x + (width shr 1) - 5 && mouseY < y + 25 && mouseY > y + 10
	}

	private fun mouseOverMaxButton(mouseX: Int, mouseY: Int): Boolean {
		return isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && mouseX > x + (width shr 1) + 5 && mouseY < y + 25 && mouseY > y + 10
	}

	fun setState(state: State) {
		this.state = state
		when (state) {
			State.CLOSED, State.OPEN -> {
				input.isVisible = false
				input.isFocused = false
				okButton.visible = false
				okButton.isFocused = false
			}

			State.MODIFYING_MAX, State.MODIFYING_MIN -> {
				input.isVisible = true
				input.isFocused = true
				input.text = ""
				input.setCursor(0, false)
				okButton.visible = true
			}
		}
	}

	fun setStateAndSlots(state: State, minSlotId: Int, maxSlotId: Int, backSlotId: Int) {
		setState(state)
		this.minSlotId = minSlotId
		this.maxSlotId = maxSlotId
		this.backSlotId = backSlotId
	}

	fun setMinAndMax(min: Int, max: Int) {
		this.min = min
		this.max = max
	}

	private fun sendPacket() {
		val sign = screen.sign
		val inputTrimmed = input.text.trim { it <= ' ' }
		if (state == State.MODIFYING_MIN) {
			try {
				min = inputTrimmed.toInt()
			} catch (ignored: NumberFormatException) {
			}
		} else if (state == State.MODIFYING_MAX) {
			try {
				max = inputTrimmed.toInt()
			} catch (ignored: NumberFormatException) {
			}
		}
		if (sign != null) {
			val messages = sign.getText(screen.isSignFront).getMessages(screen.client.shouldFilterText())
			screen.client.player.networkHandler.sendPacket(
				UpdateSignC2SPacket(
					sign.pos, screen.isSignFront,
					inputTrimmed,
					messages[1].string,
					messages[2].string,
					messages[3].string
				)
			)
		}
		screen.justOpenedSign = false
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (screen.isWaitingForServer || !screen.settingsContainer.canInteract(this)) return false
		if (!visible) return false
		if (!isMouseOver(mouseX, mouseY)) {
			if (state == State.OPEN && backSlotId != -1) {
				screen.clickAndWaitForServer(backSlotId)
				return true
			} else return false
		}
		when (state) {
			State.CLOSED -> {
				if (mouseY > y + 25) return false
				screen.clickAndWaitForServer(slotId)
				return true
			}

			State.OPEN -> {
				if (mouseOverMinButton(mouseX.toInt(), mouseY.toInt())) {
					if (minSlotId == -1) return false
					screen.clickAndWaitForServer(minSlotId)
				} else if (mouseOverMaxButton(mouseX.toInt(), mouseY.toInt())) {
					if (maxSlotId == -1) return false
					screen.clickAndWaitForServer(maxSlotId)
				} else return !(mouseY > y + 25)
				return true
			}

			else -> {
				return super.mouseClicked(mouseX, mouseY, button)
			}
		}
	}

	override fun setX(x: Int) {
		super.setX(x)
		input.x = getX()
		okButton.x = getX() + getWidth() - 15
	}

	override fun setY(y: Int) {
		super.setY(y)
		input.y = getY() + 25
		okButton.y = getY() + 25
	}

	enum class State {
		CLOSED,
		OPEN,
		MODIFYING_MIN,
		MODIFYING_MAX
	}

	protected inner class ModifiedTextFieldWidget(textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, text: Text?) : TextFieldWidget(textRenderer, x, y, width, height, text) {
		private var isGood = false

		override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
			if (!this.isNarratable || !this.isFocused) return false
			if (keyCode == 257 && isGood) {
				sendPacket()
				return true
			}
			return super.keyPressed(keyCode, scanCode, modifiers)
		}

		fun setGood(good: Boolean) {
			isGood = good
		}
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {}
}
