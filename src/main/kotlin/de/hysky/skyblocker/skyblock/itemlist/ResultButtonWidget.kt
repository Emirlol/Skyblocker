package de.hysky.skyblocker.skyblock.itemlist

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ResultButtonWidget(x: Int, y: Int) : ClickableWidget(x, y, 25, 25, Text.of("")) {
	var itemStack: ItemStack? = null

	fun setItemStack(itemStack: ItemStack?) {
		this.active = itemStack!!.item != Items.AIR
		this.visible = true
		this.itemStack = itemStack
	}

	fun clearItemStack() {
		this.visible = false
		this.itemStack = null
	}

	public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val client = MinecraftClient.getInstance()
		// this.drawTexture(matrices, this.x, this.y, 29, 206, this.width, this.height);
		context.drawGuiTexture(BACKGROUND_TEXTURE, this.x, this.y, this.getWidth(), this.getHeight())
		// client.getItemRenderer().renderInGui(this.itemStack, this.x + 4, this.y + 4);
		context.drawItem(this.itemStack, this.x + 4, this.y + 4)
		// client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, itemStack, this.x + 4, this.y + 4);
		context.drawItemInSlot(client.textRenderer, itemStack, this.x + 4, this.y + 4)
	}

	fun renderTooltip(context: DrawContext?, mouseX: Int, mouseY: Int) {
		val client = MinecraftClient.getInstance()
		val tooltip = Screen.getTooltipFromItem(client, this.itemStack)
		val orderedTooltip: MutableList<OrderedText> = ArrayList()

		for (i in tooltip.indices) {
			orderedTooltip.add(tooltip[i].asOrderedText())
		}

		client.currentScreen!!.setTooltip(orderedTooltip)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		// TODO Auto-generated method stub
	}

	companion object {
		private val BACKGROUND_TEXTURE = Identifier("recipe_book/slot_craftable")
	}
}
