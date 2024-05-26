package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows your faction status (crimson isle)
class ReputationWidget : Widget(TITLE, Formatting.AQUA.colorValue) {
	override fun updateContent() {
		val fracstr = PlayerListMgr.strAt(45)

		var spaceidx: Int
		val faction: IcoTextComponent
		if (fracstr == null || (fracstr.indexOf(' ').also { spaceidx = it }) == -1) {
			faction = IcoTextComponent()
		} else {
			val fname = fracstr.substring(0, spaceidx)
			faction = if (fname == "Mage") {
				IcoTextComponent(Ico.POTION, Text.literal(fname).formatted(Formatting.DARK_AQUA))
			} else {
				IcoTextComponent(Ico.SWORD, Text.literal(fname).formatted(Formatting.RED))
			}
		}
		this.addComponent(faction)

		val rep: Text = Widget.Companion.plainEntryText(46)
		val prog = PlayerListMgr.regexAt(47, PROGRESS_PATTERN)
		val state = PlayerListMgr.regexAt(48, STATE_PATTERN)

		if (prog == null || state == null) {
			this.addComponent(ProgressComponent())
		} else {
			val pcnt = prog.group("prog").toFloat()
			val reputationText: Text = if (state.group("from") == "Max") Text.literal("Max Reputation") else Text.literal(state.group("from") + " -> " + state.group("to"))
			val pc = ProgressComponent(
				Ico.LANTERN,
				reputationText, rep, pcnt,
				Formatting.AQUA.colorValue!!
			)
			this.addComponent(pc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Faction Status").formatted(
			Formatting.AQUA,
			Formatting.BOLD
		)

		// matches your faction alignment progress
		// group 1: percentage to next alignment level
		private val PROGRESS_PATTERN: Pattern = Pattern.compile("\\|+ \\((?<prog>[0-9.]*)%\\)")

		// matches alignment level names
		// group 1: left level name
		// group 2: right level name
		private val STATE_PATTERN: Pattern = Pattern.compile("(?<from>\\S*) *(?<to>\\S*)")
	}
}
