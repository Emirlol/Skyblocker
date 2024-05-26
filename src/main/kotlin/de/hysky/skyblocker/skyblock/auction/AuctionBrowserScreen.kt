package de.hysky.skyblocker.skyblock.auction

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.auction.widgets.AuctionTypeWidget
import de.hysky.skyblocker.skyblock.auction.widgets.CategoryTabWidget
import de.hysky.skyblocker.skyblock.auction.widgets.RarityWidget
import de.hysky.skyblocker.skyblock.auction.widgets.SortWidget
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip.getInternalNameFromNBT
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip.getNeuName
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.NarrationSupplier
import net.minecraft.client.texture.Sprite
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Duration
import java.util.*
import java.util.function.Supplier
import kotlin.math.min

class AuctionBrowserScreen(handler: AuctionHouseScreenHandler?, inventory: PlayerInventory?) : AbstractCustomHypixelGUI<AuctionHouseScreenHandler?>(handler, inventory, Text.literal("Auctions Browser")) {
	private val isSlotHighlighted = Int2BooleanOpenHashMap(24)


	// WIDGETS
	private var sortWidget: SortWidget? = null
	private var auctionTypeWidget: AuctionTypeWidget? = null
	private var rarityWidget: RarityWidget? = null
	private var resetFiltersButton: ButtonWidget? = null
	private val categoryTabWidgets: MutableList<CategoryTabWidget> = ArrayList(6)
	private var search = ""

	override fun init() {
		super.init()
		sortWidget = SortWidget(x + 25, y + 81) { slotID: Int, button: Int -> this.clickSlot(slotID, button) }
		sortWidget!!.setSlotId(SORT_BUTTON_SLOT)
		addDrawableChild(sortWidget)
		auctionTypeWidget = AuctionTypeWidget(x + 134, y + 77) { slotID: Int, button: Int -> this.clickSlot(slotID, button) }
		auctionTypeWidget!!.setSlotId(AUCTION_TYPE_BUTTON_SLOT)
		addDrawableChild(auctionTypeWidget)
		rarityWidget = RarityWidget(x + 73, y + 80) { slotID: Int, button: Int -> this.clickSlot(slotID, button) }
		rarityWidget!!.setSlotId(RARITY_BUTTON_SLOT)
		addDrawableChild(rarityWidget)
		resetFiltersButton = ScaledTextButtonWidget(x + 10, y + 77, 12, 12, Text.literal("↻")) { buttonWidget: ButtonWidget -> this.onResetPressed(buttonWidget) }
		addDrawableChild(resetFiltersButton)
		resetFiltersButton.setTooltip(Tooltip.of(Text.literal("Reset Filters")))
		resetFiltersButton.setTooltipDelay(Duration.ofMillis(500))

		addDrawableChild(ButtonWidget.Builder(Text.literal("<")) { button: ButtonWidget? -> this.clickSlot(BACK_BUTTON_SLOT) }
			.position(x + 98, y + 4)
			.size(12, 12)
			.build())

		if (categoryTabWidgets.isEmpty()) for (i in 0..5) {
			val categoryTabWidget = CategoryTabWidget(ItemStack(Items.SPONGE)) { slotID: Int, button: Int -> this.clickSlot(slotID, button) }
			categoryTabWidgets.add(categoryTabWidget)
			addSelectableChild(categoryTabWidget) // This method only makes it clickable, does not add it to the drawables list
			// manually rendered in the render method to have it not render behind the durability bars
			categoryTabWidget.setPosition(x - 30, y + 3 + i * 28)
		}
		else for (i in categoryTabWidgets.indices) {
			val categoryTabWidget = categoryTabWidgets[i]
			categoryTabWidget.setPosition(x - 30, y + 3 + i * 28)
		}
	}

	private fun onResetPressed(buttonWidget: ButtonWidget) {
		buttonWidget.isFocused = false // Annoying.
		this.clickSlot(RESET_BUTTON_SLOT, 0)
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		for (categoryTabWidget in categoryTabWidgets) {
			categoryTabWidget.render(context, mouseX, mouseY, delta)
		}
		if (isWaitingForServer) {
			val waiting = "Waiting for server..."
			context.drawText(textRenderer, waiting, this.width - textRenderer.getWidth(waiting) - 5, this.height - textRenderer.fontHeight - 2, Colors.WHITE, true)
		}

		val matrices = context.matrices
		matrices.push()
		matrices.translate(x.toFloat(), y.toFloat(), 0f)
		// Search
		context.enableScissor(x + 7, y + 4, x + 97, y + 16)
		context.drawText(textRenderer, Text.literal(search).fillStyle(Style.EMPTY.withUnderline(onSearchField(mouseX, mouseY))), 9, 6, Colors.WHITE, true)
		context.disableScissor()

		// Scrollbar
		if (prevPageVisible) {
			if (onScrollbarTop(mouseX, mouseY)) context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get())
			else context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get(), 0.54f, 0.54f, 0.54f, 1f)
		}

		if (nextPageVisible) {
			if (onScrollbarBottom(mouseX, mouseY)) context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get())
			else context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get(), 0.54f, 0.54f, 0.54f, 1f)
		}
		context.drawText(textRenderer, String.format("%d/%d", currentPage, totalPages), 111, 6, Colors.GRAY, false)
		if (totalPages <= 1) context.drawGuiTexture(SCROLLER_TEXTURE, 156, 18, 12, 15)
		else context.drawGuiTexture(SCROLLER_TEXTURE, 156, (18 + (min(currentPage.toDouble(), totalPages.toDouble()) - 1).toFloat() / (totalPages - 1) * 37).toInt(), 12, 15)

		matrices.pop()

		this.drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun drawSlot(context: DrawContext, slot: Slot) {
		if (SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN && slot.hasStack() && isSlotHighlighted.getOrDefault(slot.id, false)) {
			context.drawBorder(slot.x, slot.y, 16, 16, Color(0, 255, 0, 100).rgb)
		}
		super.drawSlot(context, slot)
	}

	override fun onMouseClick(slot: Slot, slotId: Int, button: Int, actionType: SlotActionType) {
		if (slotId >= handler!!.rows * 9) return
		super.onMouseClick(slot, slotId, button, actionType)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (isWaitingForServer) return super.mouseClicked(mouseX, mouseY, button)
		if (onScrollbarTop(mouseX.toInt(), mouseY.toInt()) && prevPageVisible) {
			clickSlot(PREV_PAGE_BUTTON)
			return true
		}
		if (onScrollbarBottom(mouseX.toInt(), mouseY.toInt()) && nextPageVisible) {
			clickSlot(NEXT_PAGE_BUTTON)
			return true
		}

		if (onSearchField(mouseX.toInt(), mouseY.toInt())) {
			clickSlot(SEARCH_BUTTON_SLOT)
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	private fun onScrollbarTop(mouseX: Int, mouseY: Int): Boolean {
		val localX = mouseX - x
		val localY = mouseY - y
		return localX > 154 && localX < 169 && localY > 6 && localY < 44
	}

	private fun onScrollbarBottom(mouseX: Int, mouseY: Int): Boolean {
		val localX = mouseX - x
		val localY = mouseY - y
		return localX > 154 && localX < 169 && localY > 43 && localY < 80
	}

	private fun onSearchField(mouseX: Int, mouseY: Int): Boolean {
		val localX = mouseX - x
		val localY = mouseY - y
		return localX > 6 && localX < 97 && localY > 3 && localY < 16
	}

	public override fun onSlotChange(handler: AuctionHouseScreenHandler?, slotId: Int, stack: ItemStack?) {
		if (client == null || stack!!.isEmpty) return
		isWaitingForServer = false

		when (slotId) {
			PREV_PAGE_BUTTON -> {
				prevPageVisible = false
				if (stack.isOf(Items.ARROW)) {
					prevPageVisible = true
					parsePage(stack)
				}
			}

			NEXT_PAGE_BUTTON -> {
				nextPageVisible = false
				if (stack.isOf(Items.ARROW)) {
					nextPageVisible = true
					parsePage(stack)
				}
			}

			SORT_BUTTON_SLOT -> sortWidget!!.setCurrent(SortWidget.Option.get(getOrdinal(getLore(stack))))
			AUCTION_TYPE_BUTTON_SLOT -> auctionTypeWidget!!.setCurrent(AuctionTypeWidget.Option.get(getOrdinal(getLore(stack))))
			RARITY_BUTTON_SLOT -> {
				val tooltip = getLore(stack)
				val ordinal = getOrdinal(tooltip)
				val split = tooltip[ordinal + 1].string.substring(2)
				rarityWidget!!.setText(tooltip.subList(1, tooltip.size - 3), split)
			}

			RESET_BUTTON_SLOT -> {
				if (resetFiltersButton != null) resetFiltersButton!!.active = handler!!.getSlot(slotId).stack.isOf(Items.ANVIL)
			}

			SEARCH_BUTTON_SLOT -> {
				val tooltipSearch = getLore(stack)
				for (text in tooltipSearch) {
					val string = text.string
					if (string.contains("Filtered:")) {
						val splitSearch = string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
						search = if (splitSearch.size < 2) {
							""
						} else splitSearch[1].trim { it <= ' ' }
						break
					}
				}
			}

			else -> {
				if (slotId < this.handler!!.rows * 9 && slotId % 9 == 0) {
					val categoryTabWidget = categoryTabWidgets[slotId / 9]
					categoryTabWidget.setSlotId(slotId)
					categoryTabWidget.setIcon(handler!!.getSlot(slotId).stack)
					val tooltipDefault = getLore(handler.getSlot(slotId).stack)
					var j = tooltipDefault.size - 1
					while (j >= 0) {
						val lowerCase = tooltipDefault[j].string.lowercase(Locale.getDefault())
						if (lowerCase.contains("currently")) {
							categoryTabWidget.isToggled = true
							break
						} else if (lowerCase.contains("click")) {
							categoryTabWidget.isToggled = false
							break
						} else categoryTabWidget.isToggled = false
						j--
					}
				} else if (slotId > 9 && slotId < (handler!!.rows - 1) * 9 && slotId % 9 > 1 && slotId % 9 < 8) {
					if (!SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN) return
					val tooltip = getLore(stack)
					var k = tooltip.size - 1
					while (k >= 0) {
						val text = tooltip[k]
						val string = text.string
						if (string.lowercase(Locale.getDefault()).contains("buy it now:")) {
							val split = string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
							if (split.size < 2) {
								k--
								continue
							}
							val coins = split[1].replace(",", "").replace("coins", "").trim { it <= ' ' }
							try {
								val parsed = coins.toLong()
								val name = getInternalNameFromNBT(stack, false)
								val internalID = getInternalNameFromNBT(stack, true)
								var neuName = name
								if (name == null || internalID == null) break
								if (name.startsWith("ISSHINY_")) {
									neuName = internalID
								}
								val jsonElement = TooltipInfoType.THREE_DAY_AVERAGE.data!![getNeuName(internalID, neuName!!)]
								if (jsonElement == null) break
								else {
									isSlotHighlighted.put(slotId, jsonElement.asDouble > parsed)
								}
							} catch (e: Exception) {
								LOGGER.error("[Skyblocker Fancy Auction House] Failed to parse BIN price", e)
							}
						}
						k--
					}
				}
			}
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_UP && prevPageVisible) {
			clickSlot(PREV_PAGE_BUTTON)
			return true
		}
		if (keyCode == GLFW.GLFW_KEY_DOWN && nextPageVisible) {
			clickSlot(NEXT_PAGE_BUTTON)
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	var currentPage: Int = 0
	var totalPages: Int = 1
	private var prevPageVisible = false
	private var nextPageVisible = false

	init {
		this.backgroundHeight = 187
		this.playerInventoryTitleY = 92
		this.titleX = 999
	}

	private fun parsePage(stack: ItemStack?) {
		checkNotNull(client)
		try {
			val tooltip = getLore(stack!!)
			var str = tooltip.first.string.trim { it <= ' ' }
			str = str.substring(1, str.length - 1) // remove parentheses
			val parts = str.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // split the string
			currentPage = parts[0].replace(",", "").toInt() // parse current page
			totalPages = parts[1].replace(",", "").toInt() // parse total
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Fancy Auction House] Failed to parse page arrow", e)
		}
	}

	override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, left: Int, top: Int, button: Int): Boolean {
		return (mouseX < left.toDouble() - 32 || mouseY < top.toDouble() || mouseX >= (left + this.backgroundWidth).toDouble()) || mouseY >= (top + this.backgroundHeight).toDouble()
	}

	private class ScaledTextButtonWidget(x: Int, y: Int, width: Int, height: Int, message: Text?, onPress: PressAction?) : ButtonWidget(x, y, width, height, message, onPress, NarrationSupplier { obj: Supplier<MutableText?> -> obj.get() }) {
		// Code taken mostly from YACL by isxander. Love you <3
		override fun drawMessage(graphics: DrawContext, textRenderer: TextRenderer, color: Int) {
			val font = MinecraftClient.getInstance().textRenderer
			val pose = graphics.matrices
			val textScale = 2f

			pose.push()
			pose.translate(((this.x + this.width / 2f) - font.getWidth(message) * textScale / 2) + 1, y.toFloat() + (this.height - font.fontHeight * textScale) / 2f - 1, 0f)
			pose.scale(textScale, textScale, 1f)
			graphics.drawText(font, message, 0, 0, color or (MathHelper.ceil(this.alpha * 255.0f) shl 24), true)
			pose.pop()
		}
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(AuctionBrowserScreen::class.java)
		private val TEXTURE = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background.png")
		private val SCROLLER_TEXTURE = Identifier("container/creative_inventory/scroller")

		private val up_arrow_tex = Identifier(SkyblockerMod.NAMESPACE, "up_arrow_even") // Put them in their own fields to avoid object allocation on each frame
		private val down_arrow_tex = Identifier(SkyblockerMod.NAMESPACE, "down_arrow_even")
		val UP_ARROW: Supplier<Sprite> = Supplier { MinecraftClient.getInstance().guiAtlasManager.getSprite(up_arrow_tex) }
		val DOWN_ARROW: Supplier<Sprite> = Supplier { MinecraftClient.getInstance().guiAtlasManager.getSprite(down_arrow_tex) }


		// SLOTS
		const val RESET_BUTTON_SLOT: Int = 47
		const val SEARCH_BUTTON_SLOT: Int = 48
		const val BACK_BUTTON_SLOT: Int = 49
		const val SORT_BUTTON_SLOT: Int = 50
		const val RARITY_BUTTON_SLOT: Int = 51
		const val AUCTION_TYPE_BUTTON_SLOT: Int = 52

		const val PREV_PAGE_BUTTON: Int = 46
		const val NEXT_PAGE_BUTTON: Int = 53

		private fun getOrdinal(tooltip: List<Text>): Int {
			var ordinal = 0
			for (j in 0 until tooltip.size - 4) {
				if (j + 1 >= tooltip.size) break
				if (tooltip[j + 1].string.contains("▶")) {
					ordinal = j
					break
				}
			}
			return ordinal
		}
	}
}
