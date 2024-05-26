package de.hysky.skyblocker.skyblock.auction

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.calculators.SignCalculator
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.*
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.function.Predicate

class EditBidPopup(auctionViewScreen: AuctionViewScreen?, private val signBlockEntity: SignBlockEntity, private val signFront: Boolean, private val minimumBid: String) : AbstractPopupScreen(Text.literal("Edit Bid"), auctionViewScreen!!) {
	private var layout: DirectionalLayoutWidget = DirectionalLayoutWidget.vertical()

	private var textFieldWidget: TextFieldWidget? = null

	private var packetSent = false

	override fun init() {
		super.init()
		layout = DirectionalLayoutWidget.vertical()
		layout.spacing(8).mainPositioner.alignHorizontalCenter()
		textFieldWidget = EnterConfirmTextFieldWidget(textRenderer, 120, 15, Text.empty()) { done(null) }
		textFieldWidget.setTextPredicate(Predicate { s: String -> this.isStringGood(s) })
		layout.add(TextWidget(Text.literal("- Set Bid -").fillStyle(Style.EMPTY.withBold(true)), textRenderer))
		layout.add(textFieldWidget)
		layout.add(TextWidget(Text.literal("Minimum Bid: $minimumBid"), textRenderer))
		val horizontal = DirectionalLayoutWidget.horizontal()
		val buttonWidget = ButtonWidget.builder(Text.literal("Set Minimum Bid")) { widget: ButtonWidget -> this.buttonMinimumBid(widget) }.width(80).build()
		buttonWidget.active = isStringGood(minimumBid)
		horizontal.add(buttonWidget)
		horizontal.add(ButtonWidget.builder(Text.literal("Done")) { widget: ButtonWidget? -> this.done(widget) }.width(80).build())
		layout.add(horizontal)
		layout.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
		layout.refreshPositions()
		SimplePositioningWidget.setPos(layout, this.navigationFocus)
		setInitialFocus(textFieldWidget)
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderBackground(context, mouseX, mouseY, delta)
		drawPopupBackground(context, layout.x, layout.y, layout.width, layout.height)
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled) {
			SignCalculator.renderCalculator(context, textFieldWidget!!.text, context.scaledWindowWidth / 2, textFieldWidget!!.y - 8)
		}
	}

	private fun isStringGood(s: String): Boolean {
		checkNotNull(this.client)
		return client!!.textRenderer.getWidth(minimumBid) <= signBlockEntity.maxTextWidth
	}

	private fun buttonMinimumBid(widget: ButtonWidget) {
		if (!isStringGood(minimumBid)) return
		sendPacket(minimumBid)
		this.close()
	}

	private fun done(widget: ButtonWidget?) {
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled) {
			if (!isStringGood(SignCalculator.getNewValue(false))) return
			sendPacket(SignCalculator.getNewValue(false))
		} else {
			if (!isStringGood(textFieldWidget!!.text.trim { it <= ' ' })) return
			sendPacket(textFieldWidget!!.text.trim { it <= ' ' })
		}
		this.close()
	}

	private fun sendPacket(string: String) {
		checkNotNull(MinecraftClient.getInstance().player)
		MinecraftClient.getInstance().player!!.networkHandler.sendPacket(
			UpdateSignC2SPacket(
				signBlockEntity.pos, signFront,
				string.replace("coins", ""),
				"",
				"",
				""
			)
		)
		packetSent = true
	}

	override fun close() {
		if (!packetSent) sendPacket("")
		checkNotNull(this.client)
		client!!.setScreen(null)
	}

	override fun removed() {
		if (!packetSent) sendPacket("")
		super.removed()
	}
}
