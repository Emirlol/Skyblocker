package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class RiftProfileWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.SIGN, "Profile:", Formatting.GREEN, 61)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Profile").formatted(Formatting.DARK_AQUA, Formatting.BOLD)
	}
}
