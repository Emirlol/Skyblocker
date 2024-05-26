package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.*
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about a skill and some stats,
// as seen in the rightmost column of the default HUD
class SkillsWidget : Widget(TITLE, Formatting.YELLOW.colorValue) {
	override fun updateContent() {
		val m = PlayerListMgr.regexAt(66, SKILL_PATTERN)
		val progress: Component
		if (m == null) {
			progress = ProgressComponent()
		} else {
			val skill = m.group(1)
			val pcntStr = m.group(2)

			if (pcntStr != "MAX") {
				val pcnt = pcntStr.toFloat()
				progress = ProgressComponent(
					Ico.LANTERN, Text.of(skill),
					Text.of("$pcntStr%"), pcnt, Formatting.GOLD.colorValue!!
				)
			} else {
				progress = IcoFatTextComponent(
					Ico.LANTERN, Text.of(skill),
					Text.literal(pcntStr).formatted(Formatting.RED)
				)
			}
		}

		this.addComponent(progress)

		val speed: Text = Widget.Companion.simpleEntryText(67, "SPD", Formatting.WHITE)
		val spd = IcoTextComponent(Ico.SUGAR, speed)
		val strength: Text = Widget.Companion.simpleEntryText(68, "STR", Formatting.RED)
		val str = IcoTextComponent(Ico.SWORD, strength)
		val critDmg: Text = Widget.Companion.simpleEntryText(69, "CCH", Formatting.BLUE)
		val cdg = IcoTextComponent(Ico.SWORD, critDmg)
		val critCh: Text = Widget.Companion.simpleEntryText(70, "CDG", Formatting.BLUE)
		val cch = IcoTextComponent(Ico.SWORD, critCh)
		val aSpeed: Text = Widget.Companion.simpleEntryText(71, "ASP", Formatting.YELLOW)
		val asp = IcoTextComponent(Ico.HOE, aSpeed)

		val tc = TableComponent(2, 3, Formatting.YELLOW.colorValue!!)
		tc.addToCell(0, 0, spd)
		tc.addToCell(0, 1, str)
		tc.addToCell(0, 2, asp)
		tc.addToCell(1, 0, cdg)
		tc.addToCell(1, 1, cch)
		this.addComponent(tc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Skill Info").formatted(
			Formatting.YELLOW,
			Formatting.BOLD
		)

		// match the skill entry
		// group 1: skill name and level
		// group 2: progress to next level (without "%")
		private val SKILL_PATTERN: Pattern = Pattern.compile("\\S*: ([A-Za-z]* [0-9]*): ([0-9.MAX]*)%?")
	}
}
