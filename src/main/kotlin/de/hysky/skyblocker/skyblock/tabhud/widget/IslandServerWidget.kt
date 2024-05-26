package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


// this widget shows info about your home island
class IslandServerWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41)
		this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42)
		this.addSimpleIcoText(Ico.EMERALD, "Crystals:", Formatting.DARK_PURPLE, 43)
		this.addSimpleIcoText(Ico.CHEST, "Stash:", Formatting.GREEN, 44)
		this.addSimpleIcoText(Ico.COMMAND, "Minions:", Formatting.BLUE, 45)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Island Info").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
