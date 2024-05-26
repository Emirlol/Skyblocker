package de.hysky.skyblocker.skyblock.quicknav

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(value = EnvType.CLIENT)
class QuickNavButton
/**
 * Constructs a new QuickNavButton with the given parameters.
 *
 * @param index   the index of the button.
 * @param toggled the toggled state of the button.
 * @param command the command to execute when the button is clicked.
 * @param icon    the icon to display on the button.
 */(private val index: Int, private var toggled: Boolean, private val command: String, private val icon: ItemStack) : ClickableWidget(0, 0, 26, 32, Text.empty()) {
	private val isTopTab: Boolean
		/**
		 * Checks if the current tab is a top tab based on its index.
		 *
		 * @return true if the index is less than 7, false otherwise.
		 */
		get() = index < 7

	fun toggled(): Boolean {
		return toggled
	}

	private fun updateCoordinates() {
		val screen = MinecraftClient.getInstance().currentScreen
		if (screen is HandledScreen<*>) {
			val x = (screen as HandledScreenAccessor).x
			val y = (screen as HandledScreenAccessor).y
			val h = (screen as HandledScreenAccessor).backgroundHeight
			this.x = x + this.index % 7 * 25
			this.y = if (this.index < 7) y - 28 else y + h - 4
		}
	}

	/**
	 * Handles click events. If the button is not currently toggled,
	 * it sets the toggled state to true and sends a message with the command after cooldown.
	 *
	 * @param mouseX the x-coordinate of the mouse click
	 * @param mouseY the y-coordinate of the mouse click
	 */
	override fun onClick(mouseX: Double, mouseY: Double) {
		if (!this.toggled) {
			this.toggled = true
			MessageScheduler.INSTANCE.sendMessageAfterCooldown(command)
			// TODO : add null check with log error
		}
	}

	/**
	 * Renders the button on screen. This includes both its texture and its icon.
	 * The method first updates the coordinates of the button,
	 * then calculates appropriate values for rendering based on its current state,
	 * and finally draws both the background and icon of the button on screen.
	 *
	 * @param context the context in which to render the button
	 * @param mouseX  the x-coordinate of the mouse cursor
	 * @param mouseY  the y-coordinate of the mouse cursor
	 */
	public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		this.updateCoordinates()
		RenderSystem.disableDepthTest()

		// Construct the texture identifier based on the index and toggled state
		val tabTexture = Identifier("container/creative_inventory/tab_" + (if (isTopTab) "top" else "bottom") + "_" + (if (toggled) "selected" else "unselected") + "_" + (index % 7 + 1))

		// Render the button texture
		context.drawGuiTexture(tabTexture, this.x, this.y, this.width, this.height)
		// Render the button icon
		val yOffset = if (this.index < 7) 1 else -1
		context.drawItem(this.icon, this.x + 5, this.y + 8 + yOffset)
		RenderSystem.enableDepthTest()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {}
}
