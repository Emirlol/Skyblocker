package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class GardenVisitorsWidget : Widget(TITLE, Formatting.DARK_GREEN.colorValue) {
	override fun updateContent() {
		val offset = if ((PlayerListMgr.strAt(46) != null)) 1 else 0

		if (PlayerListMgr.textAt(54 + offset) == null) {
			this.addComponent(PlainTextComponent(Text.literal("No visitors!").formatted(Formatting.GRAY)))
			return
		}

		for (i in 54 + offset until 59 + offset) {
			val text = PlayerListMgr.strAt(i)
			if (text != null) this.addComponent(PlainTextComponent(Text.literal(text)))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Visitors").formatted(Formatting.DARK_GREEN, Formatting.BOLD)
	}
}
