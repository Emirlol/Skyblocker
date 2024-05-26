package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Colors
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
class CommsWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		for (i in 50..53) {
			val m = PlayerListMgr.regexAt(i, COMM_PATTERN)
			// end of comms found?
			if (m == null) {
				if (i == 50) {
					this.addComponent(IcoTextComponent())
				}
				break
			}

			var pc: ProgressComponent

			val name = m.group("name")
			val progress = m.group("progress")

			if (progress == "DONE") {
				pc = ProgressComponent(Ico.BOOK, Text.of(name), Text.of(progress), 100f, Colors.pcntToCol(100f))
			} else {
				val pcnt = progress.substring(0, progress.length - 1).toFloat()
				pc = ProgressComponent(Ico.BOOK, Text.of(name), pcnt, Colors.pcntToCol(pcnt))
			}
			this.addComponent(pc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Commissions").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)

		// match a comm
		// group 1: comm name
		// group 2: comm progress (without "%" for comms that show a percentage)
		private val COMM_PATTERN: Pattern = Pattern.compile("(?<name>.*): (?<progress>.*)%?")
	}
}
