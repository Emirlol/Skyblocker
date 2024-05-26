package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


// this widget shows info about the private island you're visiting
class GuestServerWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41)
		this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42)
		this.addSimpleIcoText(Ico.SIGN, "Owner:", Formatting.GREEN, 43)
		this.addSimpleIcoText(Ico.SIGN, "Status:", Formatting.BLUE, 44)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Island Info").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
