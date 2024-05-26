package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows your crimson isle faction quests
class QuestWidget : Widget(TITLE, Formatting.AQUA.colorValue) {
	override fun updateContent() {
		for (i in 51..55) {
			val q = PlayerListMgr.textAt(i)
			val itc = IcoTextComponent(Ico.BOOK, q)
			this.addComponent(itc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Faction Quests").formatted(
			Formatting.AQUA,
			Formatting.BOLD
		)
	}
}
