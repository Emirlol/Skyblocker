package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows a list of all people visiting the same private island as you
class IslandGuestsWidget : Widget(TITLE, Formatting.AQUA.colorValue) {
	override fun updateContent() {
		for (i in 21..39) {
			val str = PlayerListMgr.strAt(i)
			if (str == null) {
				if (i == 21) {
					this.addComponent(PlainTextComponent(Text.literal("No Visitors!").formatted(Formatting.GRAY)))
				}
				break
			}
			val m = PlayerListMgr.regexAt(i, GUEST_PATTERN)
			if (m == null) {
				this.addComponent(PlainTextComponent(Text.of("???")))
			} else {
				this.addComponent(PlainTextComponent(Text.of(m.group(1))))
			}
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Guests").formatted(
			Formatting.AQUA,
			Formatting.BOLD
		)

		// matches a player entry, removing their level and the hand icon
		// group 1: player name
		private val GUEST_PATTERN: Pattern = Pattern.compile("\\[\\d*\\] (.*) \\[.\\]")
	}
}
