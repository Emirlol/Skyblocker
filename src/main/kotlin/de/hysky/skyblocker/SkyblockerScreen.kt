package de.hysky.skyblocker

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.Tips
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmLinkScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.*
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.OrderedText
import net.minecraft.text.StringVisitable
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Language
import java.time.LocalDate

class SkyblockerScreen private constructor() : Screen(TITLE) {
	private val layout = ThreePartsLayoutWidget(this)

	override fun init() {
		layout.addHeader(IconTextWidget(this.getTitle(), this.textRenderer, ICON))

		val gridWidget = layout.addBody(GridWidget()).setSpacing(SPACING)
		gridWidget.mainPositioner.alignHorizontalCenter()
		val adder = gridWidget.createAdder(2)

		adder.add(ButtonWidget.builder(CONFIGURATION_TEXT) { this.openConfig() }.width(BUTTON_WIDTH).build(), 2)
		adder.add(ButtonWidget.builder(SOURCE_TEXT, ConfirmLinkScreen.opening(this, "https://github.com/SkyblockerMod/Skyblocker")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(REPORT_BUGS_TEXT, ConfirmLinkScreen.opening(this, "https://github.com/SkyblockerMod/Skyblocker/issues")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(WEBSITE_TEXT, ConfirmLinkScreen.opening(this, "https://hysky.de/")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(TRANSLATE_TEXT, ConfirmLinkScreen.opening(this, "https://translate.hysky.de/")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(MODRINTH_TEXT, ConfirmLinkScreen.opening(this, "https://modrinth.com/mod/skyblocker-liap")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(DISCORD_TEXT, ConfirmLinkScreen.opening(this, "https://discord.gg/aNNJHQykck")).width(HALF_BUTTON_WIDTH).build())
		adder.add(ButtonWidget.builder(ScreenTexts.DONE) { this.close() }.width(BUTTON_WIDTH).build(), 2)

		val tip = MultilineTextWidget(Text.translatable("skyblocker.tips.tip", Tips.nextTipInternal()), this.textRenderer)
			.setCentered(true)
			.setMaxWidth(this.width * 10 / 7)

		layout.addFooter(tip)
		layout.refreshPositions()
		layout.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
	}

	override fun initTabNavigation() {
		layout.refreshPositions()
	}

	private fun openConfig() {
		client!!.setScreen(SkyblockerConfigManager.createGUI(this))
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		this.renderBackground(context, mouseX, mouseY, delta)
		super.render(context, mouseX, mouseY, delta)
	}

	private class IconTextWidget(message: Text, textRenderer: TextRenderer, private val icon: Identifier) : TextWidget(message, textRenderer) {
		override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			val width = this.getWidth()
			val textWidth = textRenderer.getWidth(message)
			val horizontalAlignment = 0.5f // default
			//17 = (32 + 2) / 2 • 32 + 2 is the width of the icon + spacing between icon and text
			val x = this.x + 17 + Math.round(horizontalAlignment * (width - textWidth).toFloat())
			val y = this.y + (this.getHeight() - textRenderer.fontHeight) / 2
			val orderedText = if (textWidth > width) this.trim(message, width) else message.asOrderedText()

			val iconX = x - 34
			val iconY = y - 13

			context.drawTextWithShadow(textRenderer, orderedText, x, y, this.textColor)
			context.drawTexture(this.icon, iconX, iconY, 0f, 0f, 32, 32, 32, 32)
		}

		private fun trim(text: Text, width: Int): OrderedText {
			val stringVisitable = textRenderer.trimToWidth(text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS))
			return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS))
		}
	}

	companion object {
		private const val SPACING = 8
		private const val BUTTON_WIDTH = 210
		private const val HALF_BUTTON_WIDTH = 101 //Same as (210 - 8) / 2
		private val TITLE = Text.literal("Skyblocker " + SkyblockerMod.VERSION)
		private val ICON: Identifier = LocalDate.now().let { date -> Identifier(SkyblockerMod.NAMESPACE, if (date.monthValue == 4 && date.dayOfMonth == 1) "icons.png" else "icon.png")}
		private val CONFIGURATION_TEXT = Text.translatable("text.skyblocker.config")
		private val SOURCE_TEXT = Text.translatable("text.skyblocker.source")
		private val REPORT_BUGS_TEXT = Text.translatable("menu.reportBugs")
		private val WEBSITE_TEXT = Text.translatable("text.skyblocker.website")
		private val TRANSLATE_TEXT = Text.translatable("text.skyblocker.translate")
		private val MODRINTH_TEXT = Text.translatable("text.skyblocker.modrinth")
		private val DISCORD_TEXT = Text.translatable("text.skyblocker.discord")

		init {
			ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
				dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
					.executes(Scheduler.queueOpenScreenCommand { SkyblockerScreen() })
				)
			})
		}
	}
}
