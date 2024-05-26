package de.hysky.skyblocker.skyblock.tabhud.widget.component

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext

/**
 * Abstract base class for a component that may be added to a Widget.
 */
abstract class Component {
	// these should always be the content dimensions without any padding.
	var width: Int = 0
	var height: Int = 0

	abstract fun render(context: DrawContext, x: Int, y: Int)

	companion object {
		const val ICO_DIM: Int = 16
		const val PAD_S: Int = 2
		const val PAD_L: Int = 4

		val txtRend: TextRenderer = MinecraftClient.getInstance().textRenderer
	}
}
