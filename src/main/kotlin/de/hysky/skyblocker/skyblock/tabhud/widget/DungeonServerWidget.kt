package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows broad info about the current dungeon
// opened/completed rooms, % of secrets found and time taken
class DungeonServerWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.NTAG, "Name:", Formatting.AQUA, 41)
		this.addSimpleIcoText(Ico.SIGN, "Rooms Visited:", Formatting.DARK_PURPLE, 42)
		this.addSimpleIcoText(Ico.SIGN, "Rooms Completed:", Formatting.LIGHT_PURPLE, 43)

		val m = PlayerListMgr.regexAt(44, SECRET_PATTERN)
		if (m == null) {
			this.addComponent(ProgressComponent())
		} else {
			val scp = ProgressComponent(
				Ico.CHEST, Text.of("Secrets found:"),
				m.group("secnum").toFloat(),
				Formatting.DARK_PURPLE.colorValue!!
			)
			this.addComponent(scp)
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GOLD, 45)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Dungeon Info").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// match the secrets text
		// group 1: % of secrets found (without "%")
		private val SECRET_PATTERN: Pattern = Pattern.compile("Secrets Found: (?<secnum>.*)%")
	}
}
