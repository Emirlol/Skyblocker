package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import kotlin.math.min

// this widget shows a list of players with their skins.
// responsible for non-private-island areas
class PlayerListWidget : Widget(TITLE, Formatting.GREEN.colorValue) {
	override fun updateContent() {
		val list = ArrayList<PlayerListEntry?>()

		// hard cap to 4x20 entries.
		// 5x20 is too wide (and not possible in theory. in reality however...)
		val listlen = min(PlayerListMgr.getSize().toDouble(), 160.0).toInt()

		// list isn't fully loaded, so our hack won't work...
		if (listlen < 80) {
			this.addComponent(PlainTextComponent(Text.literal("List loading...").formatted(Formatting.GRAY)))
			return
		}

		// unintuitive int ceil division stolen from
		// https://stackoverflow.com/questions/7139382/java-rounding-up-to-an-int-using-math-ceil#21830188
		val tblW = ((listlen - 80) - 1) / 20 + 1

		val tc = TableComponent(tblW, min((listlen - 80).toDouble(), 20.0).toInt(), Formatting.GREEN.colorValue!!)

		for (i in 80 until listlen) {
			list.add(PlayerListMgr.getRaw(i))
		}

		if (SkyblockerConfigManager.config.uiAndVisuals.tabHud.nameSorting == UIAndVisualsConfig.NameSorting.ALPHABETICAL) {
			list.sort(Comparator.comparing { o: PlayerListEntry -> o.profile.name.lowercase(Locale.getDefault()) })
		}

		var x = 0
		var y = 0

		for (ple in list) {
			tc.addToCell(x, y, PlayerComponent(ple))
			y++
			if (y >= 20) {
				y = 0
				x++
			}
		}

		this.addComponent(tc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Players").formatted(
			Formatting.GREEN,
			Formatting.BOLD
		)
	}
}
