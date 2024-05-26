package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class AdvertisementWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		var added = false
		for (i in 73..79) {
			val text = PlayerListMgr.textAt(i)
			if (text != null) {
				this.addComponent(PlainTextComponent(text))
				added = true
			}
		}

		if (!added) {
			this.addComponent(PlainTextComponent(Text.literal("No Advertisements").formatted(Formatting.GRAY)))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Advertisement").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
