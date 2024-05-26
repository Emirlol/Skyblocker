package de.hysky.skyblocker.skyblock.auction.widgets

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

class RarityWidget(x: Int, y: Int, private val onClick: SlotClickHandler) : ClickableWidget(x, y, 48, 11, Text.literal("rarity selector thing, hi mom")) {
	private var slotId = -1

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val matrices = context.matrices
		matrices.push()
		matrices.translate(x.toFloat(), y.toFloat(), 0f)
		val onLeftArrow = isOnLeftArrow(mouseX.toDouble())
		val onRightArrow = isOnRightArrow(mouseX.toDouble())
		context.drawTexture(TEXTURE, 0, 0, 0f, 0f, 48, 11, 48, 11)
		if (onLeftArrow) context.drawTexture(HOVER_TEXTURE, 0, 0, 0f, 0f, 6, 11, 6, 11)
		if (onRightArrow) context.drawTexture(HOVER_TEXTURE, 42, 0, 0f, 0f, 6, 11, 6, 11)

		// Text
		val textRenderer = MinecraftClient.getInstance().textRenderer
		val textWidth = textRenderer.getWidth(current)
		if (textWidth > 34) {
			val scale = 34f / textWidth
			matrices.push()
			matrices.translate(0.0, 5.5, 0.0)
			matrices.scale(scale, scale, 1f)
			context.drawCenteredTextWithShadow(textRenderer, current, (24 / scale).toInt(), -textRenderer.fontHeight / 2, color)
			matrices.pop()
		} else {
			context.drawCenteredTextWithShadow(textRenderer, current, 24, 2, color)
		}

		matrices.pop()
		if (!onLeftArrow && !onRightArrow && isHovered) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
	}

	private fun isOnRightArrow(mouseX: Double): Boolean {
		return isHovered && mouseX - x > 40
	}

	private fun isOnLeftArrow(mouseX: Double): Boolean {
		return isHovered && mouseX - x < 7
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
	}

	fun setSlotId(slotId: Int) {
		this.slotId = slotId
	}

	private var tooltip = listOf<Text>()
	private var current = "?"
	private var color = -0x151516

	fun setText(tooltip: List<Text>, current: String) {
		this.tooltip = tooltip
		this.current = current
		for ((key, value) in ItemRarityBackgrounds.LORE_RARITIES) {
			if (current.uppercase(Locale.getDefault()).contains(key!!)) {
				this.color = value.color or -0x1000000
				return
			}
		}
		this.color = Formatting.GRAY.colorValue!! or -0x1000000
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		if (slotId == -1) return
		if (isOnLeftArrow(mouseX)) {
			onClick.click(slotId, 1)
		} else if (isOnRightArrow(mouseX)) {
			onClick.click(slotId, 0)
		}
	}

	companion object {
		private val HOVER_TEXTURE = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/rarity_widget/hover.png")
		private val TEXTURE = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/rarity_widget/background.png")
	}
}
