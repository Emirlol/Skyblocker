package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows info about ongoing profile/account upgrades
// or not, if there aren't any
// TODO: not very pretty atm
class UpgradeWidget : Widget(TITLE, Formatting.GOLD.colorValue) {
	override fun updateContent() {
		val footertext = PlayerListMgr.getFooter()

		if (footertext == null) {
			this.addComponent(PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)))
			return
		}

		if (!footertext.contains("Upgrades")) {
			this.addComponent(PlainTextComponent(Text.of("Currently no upgrades...")))
			return
		}

		val interesting = footertext.split("Upgrades".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
		val lines = interesting.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

		for (i in 1 until lines.size) {
			if (lines[i].trim { it <= ' ' }.length < 3) { // empty line is §s
				break
			}
			val itc = IcoTextComponent(Ico.SIGN, Text.of(lines[i]))
			this.addComponent(itc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Upgrade Info").formatted(
			Formatting.GOLD,
			Formatting.BOLD
		)
	}
}
