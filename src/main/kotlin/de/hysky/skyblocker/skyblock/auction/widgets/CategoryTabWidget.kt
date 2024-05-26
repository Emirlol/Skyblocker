package de.hysky.skyblocker.skyblock.auction.widgets

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.widget.ToggleButtonWidget
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class CategoryTabWidget(icon: ItemStack, private val slotClick: SlotClickHandler) : ToggleButtonWidget(0, 0, 35, 27, false) {
	fun setIcon(icon: ItemStack) {
		this.icon = icon.copy()
	}

	private var icon: ItemStack
	private var slotId = -1

	init {
		this.icon = icon.copy() // copy prevents item disappearing on click
		setTextures(TEXTURES)
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (textures == null) return
		val identifier = textures!![true, toggled]
		var x = x
		if (toggled) x -= 2
		context.drawGuiTexture(identifier, x, this.y, this.width, this.height)
		context.drawItem(icon, x + 9, y + 5)

		if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
			context.matrices.push()
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, icon.getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC), mouseX, mouseY)
			context.matrices.pop()
		}
	}

	fun setSlotId(slotId: Int) {
		this.slotId = slotId
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		if (this.toggled || slotId == -1) return
		slotClick.click(slotId)
		this.isToggled = true
	}

	companion object {
		private val TEXTURES = ButtonTextures(Identifier("recipe_book/tab"), Identifier("recipe_book/tab_selected"))
	}
}
