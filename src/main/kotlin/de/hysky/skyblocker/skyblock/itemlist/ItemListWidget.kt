package de.hysky.skyblocker.skyblock.itemlist

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.mixins.accessors.RecipeBookWidgetAccessor
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Environment(value = EnvType.CLIENT)
class ItemListWidget : RecipeBookWidget() {
	private var parentWidth = 0
	private var parentHeight = 0
	private var leftOffset = 0
	private var searchField: TextFieldWidget? = null
	private var results: SearchResultsWidget? = null

	fun updateSearchResult() {
		results!!.updateSearchResult((this as RecipeBookWidgetAccessor).searchText)
	}

	override fun initialize(parentWidth: Int, parentHeight: Int, client: MinecraftClient, narrow: Boolean, craftingScreenHandler: AbstractRecipeScreenHandler<*>?) {
		super.initialize(parentWidth, parentHeight, client, narrow, craftingScreenHandler)
		this.parentWidth = parentWidth
		this.parentHeight = parentHeight
		this.leftOffset = if (narrow) 0 else 86
		this.searchField = (this as RecipeBookWidgetAccessor).searchField
		val x = (this.parentWidth - 147) / 2 - this.leftOffset
		val y = (this.parentHeight - 166) / 2
		if (ItemRepository.filesImported()) {
			this.results = SearchResultsWidget(this.client, x, y)
			this.updateSearchResult()
		}
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (this.isOpen) {
			val matrices = context.matrices
			matrices.push()
			matrices.translate(0.0, 0.0, 100.0)
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
			this.searchField = (this as RecipeBookWidgetAccessor).searchField
			val i = (this.parentWidth - 147) / 2 - this.leftOffset
			val j = (this.parentHeight - 166) / 2
			context.drawTexture(TEXTURE, i, j, 1, 1, 147, 166)
			this.searchField = (this as RecipeBookWidgetAccessor).searchField

			if (!ItemRepository.filesImported() && !this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
				val hintText: Text = Text.literal("Loading...").formatted(Formatting.ITALIC).formatted(Formatting.GRAY)
				context.drawTextWithShadow(client.textRenderer, hintText, i + 25, j + 14, -1)
			} else if (!this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
				val hintText: Text = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY)
				context.drawTextWithShadow(client.textRenderer, hintText, i + 25, j + 14, -1)
			} else {
				this.searchField.render(context, mouseX, mouseY, delta)
			}
			if (ItemRepository.filesImported()) {
				if (results == null) {
					val x = (this.parentWidth - 147) / 2 - this.leftOffset
					val y = (this.parentHeight - 166) / 2
					this.results = SearchResultsWidget(this.client, x, y)
				}
				this.updateSearchResult()
				results!!.render(context, mouseX, mouseY, delta)
			}
			matrices.pop()
		}
	}

	override fun drawTooltip(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
		if (this.isOpen && ItemRepository.filesImported() && (results != null)) {
			results!!.drawTooltip(context, mouseX, mouseY)
		}
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (this.isOpen && (client.player != null) && !client.player!!.isSpectator && ItemRepository.filesImported() && (this.searchField != null) && (results != null)) {
			if (this.searchField!!.mouseClicked(mouseX, mouseY, button)) {
				results!!.closeRecipeView()
				this.searchField!!.isFocused = true
				return true
			} else {
				this.searchField!!.isFocused = false
				return results!!.mouseClicked(mouseX, mouseY, button)
			}
		} else return false
	}
}