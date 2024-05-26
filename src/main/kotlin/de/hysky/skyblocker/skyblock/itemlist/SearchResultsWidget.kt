package de.hysky.skyblocker.skyblock.itemlist

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.widget.ToggleButtonWidget
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.regex.Pattern

class SearchResultsWidget(private val client: MinecraftClient, private val parentX: Int, private val parentY: Int) : Drawable {
	private val searchResults: MutableList<ItemStack?> = ArrayList()
	private var recipeResults: List<SkyblockCraftingRecipe?>? = ArrayList()
	private var searchText: String? = null
	private val resultButtons: MutableList<ResultButtonWidget> = ArrayList()
	private val nextPageButton: ToggleButtonWidget
	private val prevPageButton: ToggleButtonWidget
	private var currentPage = 0
	private var pageCount = 0
	private var displayRecipes = false

	init {
		val gridX = parentX + 11
		val gridY = parentY + 31
		val rows = 4
		for (i in 0 until rows) for (j in 0 until COLS) {
			val x = gridX + j * 25
			val y = gridY + i * 25
			resultButtons.add(ResultButtonWidget(x, y))
		}
		this.nextPageButton = ToggleButtonWidget(parentX + 93, parentY + 137, 12, 17, false)
		nextPageButton.setTextures(PAGE_FORWARD_TEXTURES)
		this.prevPageButton = ToggleButtonWidget(parentX + 38, parentY + 137, 12, 17, true)
		prevPageButton.setTextures(PAGE_BACKWARD_TEXTURES)
	}

	fun closeRecipeView() {
		this.currentPage = 0
		this.pageCount = (searchResults.size - 1) / resultButtons.size + 1
		this.displayRecipes = false
		this.updateButtons()
	}

	fun updateSearchResult(searchText: String) {
		if (searchText != this.searchText) {
			this.searchText = searchText
			searchResults.clear()
			for (entry in ItemRepository.getItems()) {
				val name = entry!!.name.toString().lowercase()
				val lore = entry.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT)
				if (name.contains(this.searchText!!) || lore.lines().stream().map { obj: Text -> obj.string }.anyMatch { s: String -> s.contains(this.searchText!!) }) searchResults.add(entry)
			}
			this.currentPage = 0
			this.pageCount = (searchResults.size - 1) / resultButtons.size + 1
			this.displayRecipes = false
			this.updateButtons()
		}
	}

	private fun updateButtons() {
		if (this.displayRecipes) {
			val recipe = recipeResults!![currentPage]
			for (button in resultButtons) button.clearItemStack()
			resultButtons[5].setItemStack(recipe!!.grid.first)
			resultButtons[6].setItemStack(recipe.grid[1])
			resultButtons[7].setItemStack(recipe.grid[2])
			resultButtons[10].setItemStack(recipe.grid[3])
			resultButtons[11].setItemStack(recipe.grid[4])
			resultButtons[12].setItemStack(recipe.grid[5])
			resultButtons[15].setItemStack(recipe.grid[6])
			resultButtons[16].setItemStack(recipe.grid[7])
			resultButtons[17].setItemStack(recipe.grid[8])
			resultButtons[14].setItemStack(recipe.result)
		} else {
			for (i in resultButtons.indices) {
				val index = this.currentPage * resultButtons.size + i
				if (index < searchResults.size) {
					resultButtons[i].setItemStack(searchResults[index])
				} else {
					resultButtons[i].clearItemStack()
				}
			}
		}
		prevPageButton.active = this.currentPage > 0
		nextPageButton.active = this.currentPage < this.pageCount - 1
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val textRenderer = MinecraftClient.getInstance().textRenderer
		RenderSystem.disableDepthTest()
		if (this.displayRecipes) {
			//Craft text - usually a requirement for the recipe
			var craftText = recipeResults!![currentPage].getCraftText()
			if (textRenderer.getWidth(craftText) > MAX_TEXT_WIDTH) {
				drawTooltip(textRenderer, context, craftText, this.parentX + 11, this.parentY + 31, mouseX, mouseY)
				craftText = textRenderer.trimToWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS
			}
			context.drawTextWithShadow(textRenderer, craftText, this.parentX + 11, this.parentY + 31, -0x1)

			//Item name
			var resultText = recipeResults!![currentPage].getResult().name
			if (textRenderer.getWidth(Formatting.strip(resultText.string)) > MAX_TEXT_WIDTH) {
				drawTooltip(textRenderer, context, resultText, this.parentX + 11, this.parentY + 43, mouseX, mouseY)
				resultText = Text.literal(getLegacyFormatting(resultText.string) + textRenderer.trimToWidth(Formatting.strip(resultText.string), MAX_TEXT_WIDTH) + ELLIPSIS).setStyle(resultText.style)
			}
			context.drawTextWithShadow(textRenderer, resultText, this.parentX + 11, this.parentY + 43, -0x1)

			//Arrow pointing to result item from the recipe
			context.drawTextWithShadow(textRenderer, "▶", this.parentX + 96, this.parentY + 90, -0x55000001)
		}
		for (button in resultButtons) button.render(context, mouseX, mouseY, delta)
		if (this.pageCount > 1) {
			val string = (this.currentPage + 1).toString() + "/" + this.pageCount
			val dx = client.textRenderer.getWidth(string) / 2
			context.drawText(textRenderer, string, this.parentX - dx + 73, this.parentY + 141, -1, false)
		}
		if (prevPageButton.active) prevPageButton.render(context, mouseX, mouseY, delta)
		if (nextPageButton.active) nextPageButton.render(context, mouseX, mouseY, delta)
		RenderSystem.enableDepthTest()
	}

	/**
	 * Used for drawing tooltips over truncated text
	 */
	private fun drawTooltip(textRenderer: TextRenderer, context: DrawContext, text: Text, textX: Int, textY: Int, mouseX: Int, mouseY: Int) {
		RenderSystem.disableDepthTest()
		if (mouseX >= textX && mouseX <= textX + MAX_TEXT_WIDTH + 4 && mouseY >= textY && mouseY <= textY + 9) {
			context.drawTooltip(textRenderer, text, mouseX, mouseY)
		}
		RenderSystem.enableDepthTest()
	}

	/**
	 * @see .drawTooltip
	 */
	private fun drawTooltip(textRenderer: TextRenderer, context: DrawContext, text: String?, textX: Int, textY: Int, mouseX: Int, mouseY: Int) {
		drawTooltip(textRenderer, context, Text.of(text), textX, textY, mouseX, mouseY)
	}

	fun drawTooltip(context: DrawContext?, mouseX: Int, mouseY: Int) {
		RenderSystem.disableDepthTest()
		for (button in resultButtons) if (button.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) button.renderTooltip(context, mouseX, mouseY)
		RenderSystem.enableDepthTest()
	}

	fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
		for (button in resultButtons) if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
			val internalName = getItemId(button.itemStack!!)
			if (internalName.isEmpty()) {
				continue
			}
			val recipes = ItemRepository.getRecipes(internalName)
			if (!recipes!!.isEmpty()) {
				this.recipeResults = recipes
				this.currentPage = 0
				this.pageCount = recipes.size
				this.displayRecipes = true
				this.updateButtons()
			}
			return true
		}
		if (prevPageButton.mouseClicked(mouseX, mouseY, mouseButton)) {
			--this.currentPage
			this.updateButtons()
			return true
		}
		if (nextPageButton.mouseClicked(mouseX, mouseY, mouseButton)) {
			++this.currentPage
			this.updateButtons()
			return true
		}
		return false
	}

	companion object {
		private val PAGE_FORWARD_TEXTURES = ButtonTextures(Identifier("recipe_book/page_forward"), Identifier("recipe_book/page_forward_highlighted"))
		private val PAGE_BACKWARD_TEXTURES = ButtonTextures(Identifier("recipe_book/page_backward"), Identifier("recipe_book/page_backward_highlighted"))
		private const val COLS = 5
		private const val MAX_TEXT_WIDTH = 124
		private const val ELLIPSIS = "..."
		private val FORMATTING_CODE_PATTERN: Pattern = Pattern.compile("(?i)§[0-9A-FK-OR]")

		/**
		 * Retrieves the first occurrence of section symbol formatting in a string
		 *
		 * @param string The string to fetch section symbol formatting from
		 * @return The section symbol and its formatting code or `null` if a match isn't found or if the `string` is null
		 */
		private fun getLegacyFormatting(string: String?): String? {
			if (string == null) {
				return null
			}
			val matcher = FORMATTING_CODE_PATTERN.matcher(string)
			if (matcher.find()) {
				return matcher.group(0)
			}
			return null
		}
	}
}
