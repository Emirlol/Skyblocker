package de.hysky.skyblocker.skyblock.chat

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

class ChatRulesConfigScreen(private val parent: Screen) : Screen(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen")) {
	private var chatRulesConfigListWidget: ChatRulesConfigListWidget? = null

	override fun setTooltip(tooltip: Text) {
		super.setTooltip(tooltip)
	}

	override fun init() {
		super.init()
		chatRulesConfigListWidget = ChatRulesConfigListWidget(client, this, width, height - 96, 32, 25)
		addDrawableChild(chatRulesConfigListWidget)
		val gridWidget = GridWidget()
		gridWidget.mainPositioner.marginX(5).marginY(2)
		val adder = gridWidget.createAdder(3)
		adder.add(ButtonWidget.builder(ScreenTexts.CANCEL) { button: ButtonWidget? ->
			if (client != null) {
				close()
			}
		}.build())
		val buttonNew1 = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.new")) { buttonNew: ButtonWidget? -> chatRulesConfigListWidget!!.addRuleAfterSelected() }.build()
		adder.add(buttonNew1)
		val buttonDone = ButtonWidget.builder(ScreenTexts.DONE) { button: ButtonWidget? ->
			chatRulesConfigListWidget!!.saveRules()
			if (client != null) {
				close()
			}
		}.build()
		adder.add(buttonDone)
		gridWidget.refreshPositions()
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64)
		gridWidget.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, -0x1)
	}

	override fun close() {
		if (client != null && chatRulesConfigListWidget!!.hasChanges()) {
			client!!.setScreen(ConfirmScreen({ confirmedAction: Boolean ->
				if (confirmedAction) {
					client!!.setScreen(parent)
				} else {
					client!!.setScreen(this)
				}
			}, Text.translatable("text.skyblocker.quit_config"), Text.translatable("text.skyblocker.quit_config_sure"), Text.translatable("text.skyblocker.quit_discard"), ScreenTexts.CANCEL))
		} else {
			client!!.setScreen(parent)
		}
	}
}
