package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about all puzzeles in the dungeon (name and status)
class DungeonPuzzleWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		var pos = 48

		while (pos < 60) {
			val m = PlayerListMgr.regexAt(pos, PUZZLE_PATTERN) ?: break

			val statcol = when (m.group("status")) {
				"✦" -> Formatting.GOLD
				"✔" -> Formatting.GREEN
				"✖" -> Formatting.RED
				else -> Formatting.WHITE
			}

			val t: Text = Text.literal(m.group("name") + ": ")
				.append(Text.literal("[").formatted(Formatting.GRAY))
				.append(Text.literal(m.group("status")).formatted(statcol, Formatting.BOLD))
				.append(Text.literal("]").formatted(Formatting.GRAY))
			val itc = IcoTextComponent(Ico.SIGN, t)
			this.addComponent(itc)
			pos++
		}
		if (pos == 48) {
			this.addComponent(
				IcoTextComponent(Ico.BARRIER, Text.literal("No puzzles!").formatted(Formatting.GRAY))
			)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Puzzles").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// match a puzzle entry
		// group 1: name
		// group 2: status
		// " ?.*" to diescard the solver's name if present
		// the teleport maze has a trailing whitespace that messes with the regex
		private val PUZZLE_PATTERN: Pattern = Pattern.compile("(?<name>.*): \\[(?<status>.*)\\] ?.*")
	}
}
