package de.hysky.skyblocker.skyblock.auction

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.PopupScreen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class AuctionViewScreen(handler: AuctionHouseScreenHandler?, inventory: PlayerInventory?, title: Text?) : AbstractCustomHypixelGUI<AuctionHouseScreenHandler?>(handler, inventory, title) {
	var verticalLayout: DirectionalLayoutWidget = DirectionalLayoutWidget.vertical()

	val isBinAuction: Boolean
	private var priceWidget: TextWidget? = null
	private val clickToEditBidText: Text = Text.translatable("skyblocker.fancyAuctionHouse.editBid").setStyle(Style.EMPTY.withUnderline(true))

	private var infoTextWidget: TextWidget? = null
	@JvmField
    var minBid: String? = ""

	private var buyState: BuyState? = null
	private var priceText: MutableText = Text.literal("?")
	private var buyButton: ButtonWidget? = null
	private var priceTextWidget: TextWidget? = null

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			clickSlot(BACK_BUTTON_SLOT)
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun init() {
		super.init()
		verticalLayout.spacing(2).mainPositioner.alignHorizontalCenter()
		priceTextWidget = TextWidget(if (isBinAuction) Text.translatable("skyblocker.fancyAuctionHouse.price") else Text.translatable("skyblocker.fancyAuctionHouse.newBid"), textRenderer).alignCenter()
		verticalLayout.add(priceTextWidget)

		priceWidget = TextWidget(Text.literal("?"), textRenderer).alignCenter()
		priceWidget.setWidth(textRenderer.getWidth(clickToEditBidText))
		priceWidget.active = true
		verticalLayout.add(priceWidget)

		infoTextWidget = TextWidget(Text.literal("Can't Afford"), textRenderer).alignCenter()
		verticalLayout.add(infoTextWidget)

		buyButton = ButtonWidget.builder(if (isBinAuction) Text.translatable("skyblocker.fancyAuctionHouse.buy") else Text.translatable("skyblocker.fancyAuctionHouse.bid")) { button: ButtonWidget? ->
			if (buySlotID == -1) return@builder
			clickSlot(buySlotID)
		}.size(60, 15).build()
		verticalLayout.add(buyButton)
		verticalLayout.forEachChild { drawableElement: ClickableWidget? -> this.addDrawableChild(drawableElement) }
		updateLayout()

		val backButton = ButtonWidget.Builder(Text.literal("<")) { button: ButtonWidget? -> this.clickSlot(BACK_BUTTON_SLOT) }
			.position(x + backgroundWidth - 16, y + 4)
			.size(12, 12)
			.tooltip(Tooltip.of(Text.literal("or press ESC!")))
			.build()
		backButton.setTooltipDelay(Duration.ofSeconds(1))
		addDrawableChild(backButton)
	}

	private fun changeState(newState: BuyState) {
		if (newState == buyState) return
		buyState = newState
		when (buyState) {
			BuyState.CANT_AFFORD -> {
				infoTextWidget!!.message = Text.translatable("skyblocker.fancyAuctionHouse.cantAfford").withColor(Colors.RED)
				buyButton!!.active = false
			}

			BuyState.TOP_BID -> infoTextWidget!!.message = Text.translatable("skyblocker.fancyAuctionHouse.alreadyTopBid").withColor(Colors.LIGHT_YELLOW)
			BuyState.AFFORD -> infoTextWidget!!.message = Text.empty()
			BuyState.COLLECT_AUCTION -> {
				infoTextWidget!!.message = if (changeProfile) Text.translatable("skyblocker.fancyAuctionHouse.differentProfile") else if (wonAuction) Text.empty() else Text.translatable("skyblocker.fancyAuctionHouse.didntWin")
				//priceWidget.setMessage(Text.empty());
				priceWidget!!.active = false

				if (changeProfile) {
					buyButton!!.message = Text.translatable("skyblocker.fancyAuctionHouse.changeProfile").setStyle(Style.EMPTY.withColor(Formatting.AQUA))
				} else if (wonAuction) {
					buyButton!!.message = Text.translatable("skyblocker.fancyAuctionHouse.collectAuction")
				} else {
					buyButton!!.message = Text.translatable("skyblocker.fancyAuctionHouse.collectBid")
				}
				buyButton!!.width = textRenderer.getWidth(buyButton!!.message) + 4

				priceTextWidget!!.message = Text.translatable("skyblocker.fancyAuctionHouse.auctionEnded")
				priceTextWidget!!.width = textRenderer.getWidth(priceTextWidget!!.message)
			}

			BuyState.CANCELLABLE_AUCTION -> {
				buyButton!!.message = Text.translatable("skyblocker.fancyAuctionHouse.cancelAuction").setStyle(Style.EMPTY.withColor(Formatting.RED))
				buyButton!!.width = textRenderer.getWidth(buyButton!!.message) + 4

				buyButton!!.active = true
				buyButton!!.visible = true
			}

			BuyState.OWN_AUCTION -> {
				buyButton!!.visible = false
				priceWidget!!.active = false

				infoTextWidget!!.message = Text.translatable("skyblocker.fancyAuctionHouse.yourAuction")
			}
		}
		infoTextWidget!!.width = textRenderer.getWidth(infoTextWidget!!.message)
		updateLayout()
	}

	private fun updateLayout() {
		verticalLayout.refreshPositions()
		SimplePositioningWidget.setPos(verticalLayout, x, y + 36, backgroundWidth, 60)
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)

		if (isWaitingForServer) context.drawText(textRenderer, "Waiting...", 0, 0, Colors.WHITE, true)

		val matrices = context.matrices

		matrices.push()
		matrices.translate((x + 77).toFloat(), (y + 14).toFloat(), 0f)
		matrices.scale(1.375f, 1.375f, 1.375f)
		//matrices.translate(0, 0, 100f);
		val stack = handler!!.getSlot(13).stack
		context.drawItem(stack, 0, 0)
		context.drawItemInSlot(textRenderer, stack, 0, 0)
		matrices.pop()

		if (!isBinAuction && buyState != BuyState.COLLECT_AUCTION) {
			if (priceWidget!!.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && buyState != BuyState.CANT_AFFORD) {
				priceWidget!!.message = clickToEditBidText
			} else {
				priceWidget!!.message = priceText
			}
		}

		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
		super.drawMouseoverTooltip(context, x, y)
		if ((x > this.x + 75 && x < this.x + 75 + 26) && y > this.y + 13 && y < this.y + 13 + 26) {
			context.drawTooltip(this.textRenderer, this.getTooltipFromItem(handler!!.getSlot(13).stack), x, y)
		}
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!isBinAuction && priceWidget!!.isMouseOver(mouseX, mouseY)) {
			clickSlot(31)
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	public override fun onSlotChange(handler: AuctionHouseScreenHandler?, slotId: Int, stack: ItemStack?) {
		if (stack!!.isOf(Items.BLACK_STAINED_GLASS_PANE) || slotId == 13 || slotId >= handler!!.rows * 9) return
		checkNotNull(client)
		if (stack.isOf(Items.RED_TERRACOTTA)) { // Red terracotta shows up when you can cancel it
			changeState(BuyState.CANCELLABLE_AUCTION)
			buySlotID = slotId
		}
		if (priceParsed) return
		if (stack.isOf(Items.POISONOUS_POTATO)) {
			changeState(BuyState.CANT_AFFORD)
			getPriceFromTooltip(getLore(stack))
			buySlotID = slotId
		} else if (stack.isOf(Items.GOLD_NUGGET)) {
			changeState(BuyState.AFFORD)
			getPriceFromTooltip(getLore(stack))
			buySlotID = slotId
		} else if (stack.isOf(Items.GOLD_BLOCK)) {
			changeState(BuyState.TOP_BID)
			getPriceFromTooltip(getLore(stack))
			buySlotID = slotId
		} else if (stack.isOf(Items.NAME_TAG)) {
			getPriceFromTooltip(getLore(stack))
			changeProfile = true
			buySlotID = slotId
		}
		val lowerCase = stack.name.string.lowercase(Locale.getDefault())
		if (priceParsed && lowerCase.contains("collect auction")) {
			changeState(BuyState.COLLECT_AUCTION)
		}
	}

	private var buySlotID = -1
	private var priceParsed = false
	private var wonAuction = true
	private var changeProfile = false

	init {
		backgroundHeight = 187
		isBinAuction = getTitle().string.lowercase(Locale.getDefault()).contains("bin")
		playerInventoryTitleY = 93
		titleX = 5
		titleY = 4
	}

	private fun getPriceFromTooltip(tooltip: List<Text>) {
		if (priceParsed) return
		var minBid: String? = null
		var priceString: String? = null
		val stringAtomicReference = AtomicReference("")

		for (text in tooltip) {
			val string = text.string
			val thingToLookFor = if ((isBinAuction)) "price:" else "new bid:"
			val lowerCase = string.lowercase(Locale.getDefault())
			if (lowerCase.contains(thingToLookFor)) {
				val split = string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (split.size < 2) continue
				priceString = split[1].trim { it <= ' ' }
			} else if (lowerCase.contains("minimum bid:") && !isBinAuction) {
				val split = string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (split.size < 2) continue
				minBid = split[1].replace("coins", "").replace(",", "").trim { it <= ' ' }
			} else if (lowerCase.contains("you pay:")) {
				val split = string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (split.size < 2) continue
				if (buyState != BuyState.CANT_AFFORD && !isBinAuction) {
					infoTextWidget!!.message = Text.translatable("skyblocker.fancyAuctionHouse.youPay", split[1].trim { it <= ' ' })
					infoTextWidget!!.width = textRenderer.getWidth(infoTextWidget!!.message)
				}
			} else if (lowerCase.contains("top bid:")) { // Shows up when an auction ended and you lost
				wonAuction = false
			} else if (lowerCase.contains("correct profile")) { // When an auction ended but on a different profile
				changeProfile = true
				priceWidget!!.message = Text.empty()
			} else if (lowerCase.contains("own auction")) { // it's yours
				changeState(BuyState.OWN_AUCTION)
			}
			text.visit({ style: Style, asString: String ->
				// The regex removes [, ] and +. To ignore mvp++ rank and orange + in mvp+
				val res = if (style.color == TextColor.fromFormatting(Formatting.GOLD) && !asString.matches(".*[]\\[+].*".toRegex()) && !asString.contains("Collect")) asString else null
				Optional.ofNullable(res)
			}, Style.EMPTY).ifPresent { s: String -> stringAtomicReference.set(stringAtomicReference.get() + s) }
		}

		if (priceString == null) priceString = stringAtomicReference.get()
		if (minBid != null) this.minBid = minBid
		else this.minBid = priceString
		priceText = Text.literal(priceString).setStyle(Style.EMPTY.withFormatting(Formatting.BOLD).withColor(Formatting.GOLD))
		priceWidget!!.message = priceText
		val width = textRenderer.getWidth(priceText)
		if (width > priceWidget!!.width) priceWidget!!.width = width
		priceParsed = true
		updateLayout()
	}

	fun getConfirmPurchasePopup(title: Text?): PopupScreen {
		// This really shouldn't be possible to be null in its ACTUAL use case.
		return PopupScreen.Builder(this, title)
			.button(Text.translatable("text.skyblocker.confirm")) { popupScreen: PopupScreen? -> client!!.interactionManager!!.clickSlot(client!!.player!!.currentScreenHandler.syncId, 11, 0, SlotActionType.PICKUP, client!!.player) }
			.button(Text.translatable("gui.cancel")) { popupScreen: PopupScreen? -> client!!.interactionManager!!.clickSlot(client!!.player!!.currentScreenHandler.syncId, 15, 0, SlotActionType.PICKUP, client!!.player) }
			.message((if (isBinAuction) Text.translatable("skyblocker.fancyAuctionHouse.price") else Text.translatable("skyblocker.fancyAuctionHouse.newBid")).append(" ").append(priceText)).build()
	}

	private enum class BuyState {
		CANT_AFFORD,
		AFFORD,
		TOP_BID,
		COLLECT_AUCTION,
		CANCELLABLE_AUCTION,
		OWN_AUCTION
	}

	companion object {
		protected val BACKGROUND_TEXTURE: Identifier = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background_view.png")

		const val BACK_BUTTON_SLOT: Int = 49
	}
}
