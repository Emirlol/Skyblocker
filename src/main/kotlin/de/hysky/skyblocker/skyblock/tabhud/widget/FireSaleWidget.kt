package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Colors
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about fire sales when in the hub.
// or not, if there isn't one going on
class FireSaleWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		val event = PlayerListMgr.textAt(46)

		if (event == null) {
			this.addComponent(PlainTextComponent(Text.literal("No Fire Sales!").formatted(Formatting.GRAY)))
			return
		}

		val text = event.string

		//We're keeping both cases as it might have something to do with having multiple fire sales at once vs having only one
		if (text.contains("starting in") || text.contains("Starts in")) {
			this.addComponent(IcoTextComponent(Ico.CLOCK, event))
			return
		}

		var i = 46
		while (true) {
			val m = PlayerListMgr.regexAt(i, FIRE_PATTERN) ?: break
			val avail = m.group("avail")
			val itemTxt: Text = Text.literal(m.group("item"))
			val total = m.group("total").toFloat() * 1000
			val prgressTxt: Text = Text.literal(String.format("%s/%.0f", avail, total))
			val pcnt = (avail.toFloat() / (total)) * 100f
			val pc = ProgressComponent(Ico.GOLD, itemTxt, prgressTxt, pcnt, Colors.pcntToCol(pcnt))
			this.addComponent(pc)
			i++
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Fire Sales").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)

		// matches a fire sale item
		// group 1: item name
		// group 2: # items available
		// group 3: # items available in total (1 digit + "k")
		private val FIRE_PATTERN: Pattern = Pattern.compile("(?<item>.*): (?<avail>\\d*)/(?<total>[0-9.]*)k")
	}
}
