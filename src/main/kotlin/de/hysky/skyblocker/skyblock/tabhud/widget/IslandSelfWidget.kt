package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows a list of the owners while on your home island
class IslandSelfWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		for (i in 1..19) {
			val m = PlayerListMgr.regexAt(i, OWNER_PATTERN) ?: break

			val entry = if ((m.group(1) != null)) Text.of(m.group(1)) else Text.of(m.group(2))
			this.addComponent(PlainTextComponent(entry))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Owners").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// matches an owner
		// group 1: player name, optionally offline time
		// ^\[\d*\] (?:\[[A-Za-z]+\] )?([A-Za-z0-9_() ]*)(?: .*)?$|^(.*)$
		private val OWNER_PATTERN: Pattern = Pattern
			.compile("^\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?([A-Za-z0-9_() ]*)(?: .*)?$|^(.*)$")
	}
}
