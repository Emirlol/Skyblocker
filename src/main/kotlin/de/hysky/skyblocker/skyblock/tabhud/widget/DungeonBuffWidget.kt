package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

// this widget shows a list of obtained dungeon buffs
class DungeonBuffWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val footertext = PlayerListMgr.getFooter()

		if (footertext == null || !footertext.contains("Dungeon Buffs")) {
			this.addComponent(PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)))
			return
		}

		val interesting = footertext.split("Dungeon Buffs".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
		var lines = interesting.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

		if (!lines[1].startsWith("Blessing")) {
			this.addComponent(PlainTextComponent(Text.literal("No buffs found!").formatted(Formatting.GRAY)))
			return
		}

		//Filter out text unrelated to blessings
		lines = Arrays.stream<String>(lines).filter { s: String -> s.contains("Blessing") }.toArray<String> { _Dummy_.__Array__() }

		//Alphabetically sort the blessings
		Arrays.sort(lines, Comparator.comparing { obj: String -> obj.lowercase(Locale.getDefault()) })

		for (line in lines) {
			if (line.length < 3) { // empty line is §s
				break
			}
			val color = getBlessingColor(line)
			this.addComponent(PlainTextComponent(Text.literal(line).styled { style: Style -> style.withColor(color) }))
		}
	}

	fun getBlessingColor(blessing: String): Int {
		if (blessing.contains("Life")) return Formatting.LIGHT_PURPLE.colorValue!!
		if (blessing.contains("Power")) return Formatting.RED.colorValue!!
		if (blessing.contains("Stone")) return Formatting.GREEN.colorValue!!
		if (blessing.contains("Time")) return 0xafb8c1
		if (blessing.contains("Wisdom")) return Formatting.AQUA.colorValue!!

		return 0xffffff
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Dungeon Buffs").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)
	}
}