package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widgte shows, how many active effects you have.
// it also shows one of those in detail.
// the parsing is super suspect and should be replaced by some regexes sometime later
class EffectWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val footertext = PlayerListMgr.getFooter()

		if (footertext == null || !footertext.contains("Active Effects")) {
			this.addComponent(IcoTextComponent())
			return
		}

		val lines = footertext.split("Active Effects".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		if (lines.size < 2) {
			this.addComponent(IcoTextComponent())
			return
		}

		if (lines[1].startsWith("No")) {
			val txt: Text = Text.literal("No effects active").formatted(Formatting.GRAY)
			this.addComponent(IcoTextComponent(Ico.POTION, txt))
		} else if (lines[1].contains("God")) {
			val timeleft = lines[1].split("! ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
			val godpot: Text = Text.literal("God potion!").formatted(Formatting.RED)
			val txttleft: Text = Text.literal(timeleft).formatted(Formatting.LIGHT_PURPLE)
			val iftc = IcoFatTextComponent(Ico.POTION, godpot, txttleft)
			this.addComponent(iftc)
		} else {
			var number = lines[1].substring("You have ".length)
			val idx = number.indexOf(' ')
			if (idx == -1 || lines.size < 4) {
				this.addComponent(IcoFatTextComponent())
				return
			}
			number = number.substring(0, idx)
			val active: Text = Text.literal("Active Effects: ")
				.append(Text.literal(number).formatted(Formatting.YELLOW))

			val iftc = IcoFatTextComponent(
				Ico.POTION, active,
				Text.literal(lines[2]).formatted(Formatting.AQUA)
			)
			this.addComponent(iftc)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Effect Info").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)
	}
}
