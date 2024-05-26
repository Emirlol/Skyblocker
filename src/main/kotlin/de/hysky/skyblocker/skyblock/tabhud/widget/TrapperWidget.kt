package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows how meny pelts you have (farming island)
class TrapperWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.LEATHER, "Pelts:", Formatting.AQUA, 46)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Trapper").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
