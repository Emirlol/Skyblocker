package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Special version of the server info widget for the rift!
 *
 */
class RiftServerInfoWidget : Widget(TITLE, Formatting.LIGHT_PURPLE.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.LIGHT_PURPLE, 41)
		this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Server Info").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
	}
}
