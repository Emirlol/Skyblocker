package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ShenWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addComponent(PlainTextComponent(Text.literal(PlayerListMgr.strAt(70))))
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Shen's Countdown").formatted(Formatting.DARK_AQUA, Formatting.BOLD)
	}
}
