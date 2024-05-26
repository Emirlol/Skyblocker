package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)
class PowderWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MITHRIL, "Mithril:", Formatting.AQUA, 46)
		this.addSimpleIcoText(Ico.AMETHYST_SHARD, "Gemstone:", Formatting.DARK_PURPLE, 47)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Powders").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
