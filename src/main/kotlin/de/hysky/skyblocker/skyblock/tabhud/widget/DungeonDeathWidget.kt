package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows various dungeon info
// deaths, healing, dmg taken, milestones
class DungeonDeathWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val m = PlayerListMgr.regexAt(25, DEATH_PATTERN)
		if (m == null) {
			this.addComponent(IcoTextComponent())
		} else {
			val f = if ((m.group("deathnum") == "0")) Formatting.GREEN else Formatting.RED
			val d: Text = Widget.Companion.simpleEntryText(m.group("deathnum"), "Deaths: ", f)
			val deaths = IcoTextComponent(Ico.SKULL, d)
			this.addComponent(deaths)
		}

		this.addSimpleIcoText(Ico.SWORD, "Damage Dealt:", Formatting.RED, 26)
		this.addSimpleIcoText(Ico.POTION, "Healing Done:", Formatting.RED, 27)
		this.addSimpleIcoText(Ico.NTAG, "Milestone:", Formatting.YELLOW, 28)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Death").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// match the deaths entry
		// group 1: amount of deaths
		private val DEATH_PATTERN: Pattern = Pattern.compile("Team Deaths: (?<deathnum>\\d+).*")
	}
}
