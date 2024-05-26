package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows info about... something?
// related to downed people in dungeons, not sure what this is supposed to show
class DungeonDownedWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val down = PlayerListMgr.strAt(21)
		if (down == null) {
			this.addComponent(IcoTextComponent())
		} else {
			var format = Formatting.RED
			if (down.endsWith("NONE")) {
				format = Formatting.GRAY
			}
			val idx = down.indexOf(": ")
			val downed: Text? = if ((idx == -1)) null
			else Widget.Companion.simpleEntryText(down.substring(idx + 2), "Downed: ", format)
			val d = IcoTextComponent(Ico.SKULL, downed)
			this.addComponent(d)
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GRAY, 22)
		this.addSimpleIcoText(Ico.POTION, "Revive:", Formatting.GRAY, 23)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Downed").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)
	}
}
