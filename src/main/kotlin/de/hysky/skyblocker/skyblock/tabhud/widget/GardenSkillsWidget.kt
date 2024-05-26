package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about your skills while in the garden
class GardenSkillsWidget : Widget(TITLE, Formatting.YELLOW.colorValue) {
	override fun updateContent() {
		val spc: ProgressComponent
		val skillMatcher = PlayerListMgr.regexAt(66, SKILL_PATTERN)
		if (skillMatcher == null) {
			spc = ProgressComponent()
		} else {
			val strpcnt = skillMatcher.group("progress")
			val skill = skillMatcher.group("skill")

			if (strpcnt == "MAX") {
				spc = ProgressComponent(
					Ico.LANTERN, Text.of(skill), Text.of("MAX"), 100f,
					Formatting.RED.colorValue!!
				)
			} else {
				val pcnt = strpcnt.toFloat()
				spc = ProgressComponent(
					Ico.LANTERN, Text.of(skill), pcnt,
					Formatting.GOLD.colorValue!!
				)
			}
		}

		this.addComponent(spc)

		val glpc: ProgressComponent
		val glMatcher = PlayerListMgr.regexAt(45, GARDEN_LEVEL_PATTERN)

		if (glMatcher == null) {
			glpc = ProgressComponent()
		} else {
			val level = glMatcher.group("level")

			if (level == "15" || level == "XV") {
				glpc = ProgressComponent(Ico.SEEDS, Text.literal("Garden Level $level"), 100f, Formatting.RED.colorValue!!)
			} else {
				val strpcnt = glMatcher.group("progress")
				val pcnt = strpcnt.toFloat()

				glpc = ProgressComponent(Ico.SEEDS, Text.literal("Garden Level $level"), pcnt, Formatting.DARK_GREEN.colorValue!!)
			}
		}

		this.addComponent(glpc)

		val speed: Text = Widget.Companion.simpleEntryText(67, "SPD", Formatting.WHITE)
		val spd = IcoTextComponent(Ico.SUGAR, speed)
		val farmfort: Text = Widget.Companion.simpleEntryText(68, "FFO", Formatting.GOLD)
		val ffo = IcoTextComponent(Ico.HOE, farmfort)

		val tc = TableComponent(2, 1, Formatting.YELLOW.colorValue!!)
		tc.addToCell(0, 0, spd)
		tc.addToCell(1, 0, ffo)
		this.addComponent(tc)

		this.addComponent(IcoTextComponent(Ico.HOE, PlayerListMgr.textAt(70)))

		val pc2: ProgressComponent
		val milestoneMatcher = PlayerListMgr.regexAt(69, MS_PATTERN)
		if (milestoneMatcher == null) {
			pc2 = ProgressComponent()
		} else {
			val strpcnt = milestoneMatcher.group("progress")
			val milestone = milestoneMatcher.group("milestone")

			val pcnt = strpcnt.toFloat()
			pc2 = ProgressComponent(
				Ico.MILESTONE, Text.of(milestone), pcnt,
				Formatting.GREEN.colorValue!!
			)
		}
		this.addComponent(pc2)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Skill Info").formatted(
			Formatting.YELLOW,
			Formatting.BOLD
		)

		// match the skill entry
		// group 1: skill name and level
		// group 2: progress to next level (without "%")
		private val SKILL_PATTERN: Pattern = Pattern
			.compile("Skills: (?<skill>[A-Za-z]* [0-9]*): (?<progress>[0-9.MAX]*)%?")

		private val GARDEN_LEVEL_PATTERN: Pattern = Pattern.compile("Garden Level: (?<level>[IVX0-9]+)(?: \\((?<progress>[0-9.]+)% to [IVX0-9]+\\))?")

		// same, more or less
		private val MS_PATTERN: Pattern = Pattern
			.compile("Milestone: (?<milestone>[A-Za-z ]* [0-9]*): (?<progress>[0-9.]*)%")
	}
}
