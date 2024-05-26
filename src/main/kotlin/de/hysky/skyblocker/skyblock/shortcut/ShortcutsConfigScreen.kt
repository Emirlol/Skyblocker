package de.hysky.skyblocker.skyblock.shortcut

import de.hysky.skyblocker.skyblock.shortcut.ShortcutsConfigListWidget.ShortcutEntry
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

class ShortcutsConfigScreen @JvmOverloads constructor(private val parent: Screen? = null) : Screen(Text.translatable("skyblocker.shortcuts.config")) {
	private var shortcutsConfigListWidget: ShortcutsConfigListWidget? = null
	private var buttonDelete: ButtonWidget? = null
	private var buttonNew: ButtonWidget? = null
	private var buttonDone: ButtonWidget? = null
	private var initialized = false
	private var scrollAmount = 0.0

	override fun setTooltip(tooltip: Text) {
		super.setTooltip(tooltip)
	}

	override fun init() {
		super.init()
		if (initialized) {
			shortcutsConfigListWidget!!.setDimensions(width, height - 96)
			shortcutsConfigListWidget!!.updatePositions()
		} else {
			shortcutsConfigListWidget = ShortcutsConfigListWidget(client, this, width, height - 96, 32, 25)
			initialized = true
		}
		addDrawableChild(shortcutsConfigListWidget)
		val gridWidget = GridWidget()
		gridWidget.mainPositioner.marginX(5).marginY(2)
		val adder = gridWidget.createAdder(2)
		buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.delete")) { button: ButtonWidget? ->
			if (client != null && shortcutsConfigListWidget!!.selectedOrNull is ShortcutEntry) {
				scrollAmount = shortcutsConfigListWidget!!.scrollAmount
				client!!.setScreen(ConfirmScreen({ confirmedAction: Boolean -> this.deleteEntry(confirmedAction) }, Text.translatable("skyblocker.shortcuts.deleteQuestion"), Text.stringifiedTranslatable("skyblocker.shortcuts.deleteWarning", shortcutEntry), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL))
			}
		}.build()
		adder.add(buttonDelete)
		buttonNew = ButtonWidget.builder(Text.translatable("skyblocker.shortcuts.new")) { buttonNew: ButtonWidget? -> shortcutsConfigListWidget!!.addShortcutAfterSelected() }.build()
		adder.add(buttonNew)
		adder.add(ButtonWidget.builder(ScreenTexts.CANCEL) { button: ButtonWidget? ->
			if (client != null) {
				close()
			}
		}.build())
		buttonDone = ButtonWidget.builder(ScreenTexts.DONE) { button: ButtonWidget? ->
			shortcutsConfigListWidget!!.saveShortcuts()
			if (client != null) {
				close()
			}
		}.tooltip(Tooltip.of(Text.translatable("skyblocker.shortcuts.commandSuggestionTooltip"))).build()
		adder.add(buttonDone)
		gridWidget.refreshPositions()
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64)
		gridWidget.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
		updateButtons()
	}

	private fun deleteEntry(confirmedAction: Boolean) {
		if (client != null) {
			if (confirmedAction && shortcutsConfigListWidget!!.selectedOrNull is ShortcutEntry) {
				shortcutsConfigListWidget!!.removeEntry(shortcutEntry)
			}
			client!!.setScreen(this) // Re-inits the screen and keeps the old instance of ShortcutsConfigListWidget
			shortcutsConfigListWidget!!.scrollAmount = scrollAmount
		}
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF)
	}

	override fun close() {
		if (client != null) {
			if (shortcutsConfigListWidget!!.hasChanges()) {
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

	fun updateButtons() {
		buttonDelete!!.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget!!.selectedOrNull is ShortcutEntry
		buttonNew!!.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget!!.category.isPresent
		buttonDone!!.active = Shortcuts.isShortcutsLoaded()
	}
}
