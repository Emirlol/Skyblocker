package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows the status or results of the current election
class ElectionWidget : Widget(TITLE, Formatting.YELLOW.colorValue) {
	override fun updateContent() {
		val status = PlayerListMgr.strAt(76)
		if (status == null) {
			this.addComponent(IcoTextComponent())
			this.addComponent(IcoTextComponent())
			this.addComponent(IcoTextComponent())
			this.addComponent(IcoTextComponent())
			return
		}

		if (status.contains("Over!")) {
			// election is over
			val over = IcoTextComponent(Ico.BARRIER, EL_OVER)
			this.addComponent(over)

			val win = PlayerListMgr.strAt(77)
			if (win == null || !win.contains(": ")) {
				this.addComponent(IcoTextComponent())
			} else {
				val winnername = win.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
				val winnertext: Text = Widget.Companion.simpleEntryText(winnername, "Winner: ", Formatting.GREEN)
				val winner = IcoTextComponent(MAYOR_DATA[winnername], winnertext)
				this.addComponent(winner)
			}

			this.addSimpleIcoText(Ico.PLAYER, "Participants:", Formatting.AQUA, 78)
			this.addSimpleIcoText(Ico.SIGN, "Year:", Formatting.LIGHT_PURPLE, 79)
		} else {
			// election is going on
			this.addSimpleIcoText(Ico.CLOCK, "End in:", Formatting.GOLD, 76)

			for (i in 77..79) {
				val m = PlayerListMgr.regexAt(i, VOTE_PATTERN)
				if (m == null) {
					this.addComponent(ProgressComponent())
				} else {
					val mayorname = m.group("mayor")
					val pcntstr = m.group("pcnt")
					val pcnt = pcntstr.toFloat()
					val candidate: Text = Text.literal(mayorname).formatted(COLS[i - 77])
					val pc = ProgressComponent(
						MAYOR_DATA[mayorname], candidate, pcnt,
						COLS[i - 77].colorValue!!
					)
					this.addComponent(pc)
				}
			}
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Election Info").formatted(
			Formatting.YELLOW,
			Formatting.BOLD
		)

		private val MAYOR_DATA = HashMap<String, ItemStack>()

		private val EL_OVER: Text = Text.literal("Election ")
			.append(Text.literal("over!").formatted(Formatting.RED))

		// pattern matching a candidate while people are voting
		// group 1: name
		// group 2: % of votes
		private val VOTE_PATTERN: Pattern = Pattern.compile("(?<mayor>\\S*): \\|+ \\((?<pcnt>\\d*)%\\)")

		init {
			MAYOR_DATA["Aatrox"] = Ico.DIASWORD
			MAYOR_DATA["Cole"] = Ico.PICKAXE
			MAYOR_DATA["Diana"] = Ico.BONE
			MAYOR_DATA["Diaz"] = Ico.GOLD
			MAYOR_DATA["Finnegan"] = Ico.HOE
			MAYOR_DATA["Foxy"] = Ico.SUGAR
			MAYOR_DATA["Paul"] = Ico.COMPASS
			MAYOR_DATA["Scorpius"] = Ico.MOREGOLD
			MAYOR_DATA["Jerry"] = Ico.VILLAGER
			MAYOR_DATA["Derpy"] = Ico.DBUSH
			MAYOR_DATA["Marina"] = Ico.FISH_ROD
		}

		private val COLS = arrayOf(Formatting.GOLD, Formatting.RED, Formatting.LIGHT_PURPLE)
	}
}
