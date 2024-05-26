package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows a list of the owners of a home island while guesting
class IslandOwnersWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		for (i in 1..19) {
			val m = PlayerListMgr.regexAt(i, OWNER_PATTERN) ?: break

			var name: String?
			var lastseen: String
			var format: Formatting
			if (m.group("nameA") != null) {
				name = m.group("nameA")
				lastseen = m.group("lastseen")
				format = Formatting.GRAY
			} else if (m.group("nameB") != null) {
				name = m.group("nameB")
				lastseen = "Online"
				format = Formatting.WHITE
			} else {
				name = m.group("nameC")
				lastseen = "Online"
				format = Formatting.WHITE
			}

			val entry: Text = Text.literal(name)
				.append(
					Text.literal(" ($lastseen)")
						.formatted(format)
				)
			val ptc = PlainTextComponent(entry)
			this.addComponent(ptc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Owners").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// matches an owner
		// group 1: player name
		// group 2: last seen, if owner not online
		// ^(?<nameA>.*) \((?<lastseen>.*)\)$|^\[\d*\] (?:\[[A-Za-z]+\] )?(?<nameB>[A-Za-z0-9_]*)(?: .*)?$|^(?<nameC>.*)$
		private val OWNER_PATTERN: Pattern = Pattern
			.compile("^(?<nameA>.*) \\((?<lastseen>.*)\\)$|^\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<nameB>[A-Za-z0-9_]*)(?: .*)?$|^(?<nameC>.*)$")
	}
}
