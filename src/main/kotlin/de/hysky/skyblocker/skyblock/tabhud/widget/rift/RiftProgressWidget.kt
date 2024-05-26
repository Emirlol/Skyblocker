package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.Colors
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

class RiftProgressWidget : Widget(TITLE, Formatting.BLUE.colorValue) {
	override fun updateContent() {
		// After you progress further, the tab adds more info so we need to be careful
		// of that.
		// In beginning it only shows montezuma, then timecharms and enigma souls are
		// added.

		val pos44 = PlayerListMgr.strAt(44)

		// LHS short-circuits, so the RHS won't be evaluated on pos44 == null
		if (pos44 == null || !pos44.contains("Rift Progress")) {
			this.addComponent(PlainTextComponent(Text.literal("No Progress").formatted(Formatting.GRAY)))
			return
		}

		// let's try to be clever by assuming what progress item may appear where and
		// when to skip testing every slot for every thing.

		// always non-null, as this holds the topmost item.
		// if there is none, there shouldn't be a header.
		val pos45 = PlayerListMgr.strAt(45)

		// Can be Montezuma, Enigma Souls or Timecharms.
		// assume timecharms can only appear here and that they're the last thing to
		// appear, so if this exists, we know the rest.
		if (pos45!!.contains("Timecharms")) {
			addTimecharmsComponent(45)
			addEnigmaSoulsComponent(46)
			addMontezumaComponent(47)
			return
		}

		// timecharms didn't appear at the top, so there's two or one entries.
		// assume that if there's two, souls is always top.
		val pos46 = PlayerListMgr.strAt(46)

		if (pos45.contains("Enigma Souls")) {
			addEnigmaSoulsComponent(45)
			if (pos46 != null) {
				// souls might appear alone.
				// if there's a second entry, it has to be montezuma
				addMontezumaComponent(46)
			}
		} else {
			// first entry isn't souls, so it's just montezuma and nothing else.
			addMontezumaComponent(45)
		}
	}

	private fun addTimecharmsComponent(pos: Int) {
		val m = PlayerListMgr.regexAt(pos, TIMECHARMS_PATTERN)

		val current = m!!.group("current").toInt()
		val total = m.group("total").toInt()
		val pcnt = (current.toFloat() / total.toFloat()) * 100f
		val progressText: Text = Text.literal("$current/$total")

		val pc = ProgressComponent(
			Ico.NETHER_STAR, Text.literal("Timecharms"), progressText,
			pcnt, Colors.pcntToCol(pcnt)
		)

		this.addComponent(pc)
	}

	private fun addEnigmaSoulsComponent(pos: Int) {
		val m = PlayerListMgr.regexAt(pos, ENIGMA_SOULS_PATTERN)

		val current = m!!.group("current").toInt()
		val total = m.group("total").toInt()
		val pcnt = (current.toFloat() / total.toFloat()) * 100f
		val progressText: Text = Text.literal("$current/$total")

		val pc = ProgressComponent(
			Ico.HEART_OF_THE_SEA, Text.literal("Enigma Souls"),
			progressText, pcnt, Colors.pcntToCol(pcnt)
		)

		this.addComponent(pc)
	}

	private fun addMontezumaComponent(pos: Int) {
		val m = PlayerListMgr.regexAt(pos, MONTEZUMA_PATTERN)

		val current = m!!.group("current").toInt()
		val total = m.group("total").toInt()
		val pcnt = (current.toFloat() / total.toFloat()) * 100f
		val progressText: Text = Text.literal("$current/$total")

		val pc = ProgressComponent(
			Ico.BONE, Text.literal("Montezuma"), progressText, pcnt,
			Colors.pcntToCol(pcnt)
		)

		this.addComponent(pc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Rift Progress").formatted(Formatting.BLUE, Formatting.BOLD)

		private val TIMECHARMS_PATTERN: Pattern = Pattern.compile("Timecharms: (?<current>[0-9]+)\\/(?<total>[0-9]+)")
		private val ENIGMA_SOULS_PATTERN: Pattern = Pattern.compile("Enigma Souls: (?<current>[0-9]+)\\/(?<total>[0-9]+)")
		private val MONTEZUMA_PATTERN: Pattern = Pattern.compile("Montezuma: (?<current>[0-9]+)\\/(?<total>[0-9]+)")
	}
}
