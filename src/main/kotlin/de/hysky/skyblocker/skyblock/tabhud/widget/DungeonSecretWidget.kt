package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about the secrets of the dungeon
class DungeonSecretWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		if (PlayerListMgr.regexAt(31, DISCOVERIES) != null) {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 32)
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 33)
		} else {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 31)
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 32)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Discoveries").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
		private val DISCOVERIES: Pattern = Pattern.compile("Discoveries: (\\d+)")
	}
}
