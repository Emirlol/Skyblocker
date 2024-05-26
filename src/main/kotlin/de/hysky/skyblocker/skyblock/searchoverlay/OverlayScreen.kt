package de.hysky.skyblocker.skyblock.searchoverlay

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

class OverlayScreen(title: Text?) : Screen(title) {
	private var searchField: TextFieldWidget? = null
	private var finishedButton: ButtonWidget? = null
	private var suggestionButtons: Array<ButtonWidget?>
	private var historyButtons: Array<ButtonWidget?>

	/**
	 * Creates the layout for the overlay screen.
	 */
	override fun init() {
		super.init()
		val rowWidth = (this.width * 0.4).toInt()
		val startX = (this.width * 0.5).toInt() - rowWidth / 2
		val startY = ((this.height * 0.5).toInt() - (rowHeight * (1 + SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions + 0.75 + SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength)) / 2).toInt()

		// Search field
		this.searchField = TextFieldWidget(textRenderer, startX, startY, rowWidth - rowHeight, rowHeight, Text.translatable("gui.recipebook.search_hint"))
		searchField!!.text = SearchOverManager.search
		searchField!!.setChangedListener { obj: String? -> SearchOverManager.updateSearch() }
		searchField!!.setMaxLength(30)

		// finish buttons
		finishedButton = ButtonWidget.builder(Text.empty().setStyle(Style.EMPTY.withColor(Formatting.GREEN))) { a: ButtonWidget? -> close() }
			.position(startX + rowWidth - rowHeight, startY)
			.size(rowHeight, rowHeight).build()

		// suggested item buttons
		var rowOffset = rowHeight
		val totalSuggestions = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions
		this.suggestionButtons = arrayOfNulls(totalSuggestions)
		for (i in 0 until totalSuggestions) {
			suggestionButtons[i] = ButtonWidget.builder(Text.literal(SearchOverManager.getSuggestion(i)).setStyle(Style.EMPTY)) { a: ButtonWidget ->
				SearchOverManager.updateSearch(a.message.string)
				close()
			}
				.position(startX, startY + rowOffset)
				.size(rowWidth, rowHeight).build()
			suggestionButtons[i].visible = false
			rowOffset += rowHeight
		}
		// history item buttons
		rowOffset += (rowHeight * 0.75).toInt()
		val historyLength = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength
		this.historyButtons = arrayOfNulls(historyLength)
		for (i in 0 until historyLength) {
			val text = SearchOverManager.getHistory(i)
			if (text != null) {
				historyButtons[i] = ButtonWidget.builder(Text.literal(text).setStyle(Style.EMPTY)) { a: ButtonWidget ->
					SearchOverManager.updateSearch(a.message.string)
					close()
				}
					.position(startX, startY + rowOffset)
					.size(rowWidth, rowHeight).build()
				rowOffset += rowHeight
			} else {
				break
			}
		}

		//add drawables in order to make tab navigation sensible
		addDrawableChild(searchField)
		for (suggestion in suggestionButtons) {
			addDrawableChild(suggestion)
		}
		for (historyOption in historyButtons) {
			if (historyOption != null) {
				addDrawableChild(historyOption)
			}
		}
		addDrawableChild(finishedButton)

		//focus the search box
		this.setInitialFocus(searchField)
	}

	/**
	 * Renders the search icon, label for the history and item Stacks for item names
	 */
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		val renderOffset = (rowHeight - 16) / 2
		context.drawGuiTexture(SEARCH_ICON_TEXTURE, finishedButton!!.x + renderOffset, finishedButton!!.y + renderOffset, 16, 16)
		if (historyButtons.size > 0 && historyButtons[0] != null) {
			context.drawText(textRenderer, Text.translatable("skyblocker.config.general.searchOverlay.historyLabel"), historyButtons[0]!!.x + renderOffset, historyButtons[0]!!.y - rowHeight / 2, -0x1, true)
		}

		//draw item stacks and tooltip to buttons
		for (i in suggestionButtons.indices) {
			drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getSuggestionId(i), suggestionButtons[i], renderOffset)
		}
		for (i in historyButtons.indices) {
			drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getHistoryId(i), historyButtons[i], renderOffset)
		}
	}

	/**
	 * Draws the item and tooltip for the given button
	 */
	private fun drawItemAndTooltip(context: DrawContext, mouseX: Int, mouseY: Int, id: String?, button: ButtonWidget?, renderOffset: Int) {
		if (id == null || id.isEmpty()) return
		val item = ItemRepository.getItemStack(id) ?: return
		context.drawItem(item, button!!.x + renderOffset, button.y + renderOffset)

		// Draw tooltip
		if (button.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
			context.drawItemTooltip(textRenderer, item, mouseX, mouseY)
		}
	}

	/**
	 * updates if the suggestions buttons should be visible based on if they have a value
	 */
	override fun tick() {
		super.tick()
		//update suggestion buttons text
		for (i in 0 until SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions) {
			val text = SearchOverManager.getSuggestion(i)
			if (!text!!.isEmpty()) {
				suggestionButtons[i]!!.visible = true

				val isNewText = text != suggestionButtons[i]!!.message.string
				if (!isNewText) continue

				suggestionButtons[i]!!.message = Text.literal(text).setStyle(Style.EMPTY)
			} else {
				suggestionButtons[i]!!.visible = false
			}
		}
	}

	/**
	 * When a key is pressed. If enter key pressed and search box selected close
	 */
	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_ENTER && searchField!!.isActive) {
			close()
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun shouldPause(): Boolean {
		return false
	}

	/**
	 * Closes the overlay screen and gets the manager to send a packet update about the sign
	 */
	override fun close() {
		checkNotNull(this.client)
		checkNotNull(client!!.player)
		SearchOverManager.pushSearch()
		super.close()
	}

	companion object {
		protected val SEARCH_ICON_TEXTURE: Identifier = Identifier("icon/search")
		private const val rowHeight = 20
	}
}
