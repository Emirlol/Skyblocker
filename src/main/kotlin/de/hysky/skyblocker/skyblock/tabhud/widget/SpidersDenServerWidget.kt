package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

/**
 * This widget shows info about the Spider's Den server
 */
class SpidersDenServerWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	/**
	 * Broodmother Mini-Boss tab states
	 */
	private enum class BroodmotherState(private val text: String, private val formatting: Formatting) {
		SOON("Soon", Formatting.GOLD),
		AWAKENING("Awakening", Formatting.GOLD),
		IMMINENT("Imminent", Formatting.DARK_RED),
		ALIVE("Alive!", Formatting.DARK_RED),
		SLAIN("Slain", Formatting.YELLOW),
		DORMANT("Dormant", Formatting.YELLOW),
		UNKNOWN("Unknown", Formatting.GRAY);

		fun text(): String {
			return this.text
		}

		fun formatting(): Formatting {
			return this.formatting
		}

		companion object {
			/**
			 * Returns a state object by text
			 *
			 * @param text text state from tab
			 * @return Broodmother State object
			 */
			fun from(text: String): BroodmotherState {
				return Arrays.stream(entries.toTypedArray())
					.filter { broodmotherState: BroodmotherState -> text == broodmotherState.text() }.findFirst().orElse(UNKNOWN)
			}
		}
	}

	/**
	 * Updates the information in the widget.
	 */
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41)
		this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42)
		this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43)

		val broodmotherState = parseTab()
		this.addSimpleIcoText(Ico.SPIDER_EYE, "Broodmother: ", broodmotherState.formatting(), broodmotherState.text())
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Server Info").formatted(Formatting.DARK_AQUA, Formatting.BOLD)

		/**
		 * Parses the Broodmother string from tab and returns a state object.
		 *
		 * @return Broodmother State object
		 */
		private fun parseTab(): BroodmotherState {
			val state = PlayerListMgr.strAt(45)
			if (state == null || !state.contains(": ")) return BroodmotherState.UNKNOWN

			return BroodmotherState.from(state.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
		}
	}
}
