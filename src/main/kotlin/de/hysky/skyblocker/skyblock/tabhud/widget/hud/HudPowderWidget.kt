package de.hysky.skyblocker.skyblock.tabhud.widget.hud

import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!
class HudPowderWidget  // another repulsive hack to make this widget-like hud element work with the new widget class
// DON'T USE WITH THE WIDGET SYSTEM, ONLY USE FOR DWARVENHUD!
	: Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		updatePowders()
		val mithrilPowderString = formatPowderString(mithrilPowder, mithrilPowderDiff)
		val gemstonePowderString = formatPowderString(gemstonePowder, gemstonePowderDiff)
		val glacitePowderString = formatPowderString(glacitePowder, glacitePowderDiff)

		this.addSimpleIcoText(Ico.MITHRIL, "Mithril: ", Formatting.DARK_GREEN, mithrilPowderString)
		this.addSimpleIcoText(Ico.AMETHYST_SHARD, "Gemstone: ", Formatting.DARK_PURPLE, gemstonePowderString)
		this.addSimpleIcoText(Ico.BLUE_ICE, "Glacite: ", Formatting.AQUA, glacitePowderString)
	}

	companion object {
		/**
		 * American number format instance
		 */
		private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)

		/**
		 * current value of Mithril Powder
		 */
		private var mithrilPowder = 0

		/**
		 * current value of Gemstone Powder
		 */
		private var gemstonePowder = 0

		/**
		 * current value of Glacite Powder
		 */
		private var glacitePowder = 0

		/**
		 * the difference between the previous and current value of Mithril Powder
		 */
		private var mithrilPowderDiff = 0

		/**
		 * the difference between the previous and current value of Gemstone Powder
		 */
		private var gemstonePowderDiff = 0

		/**
		 * the difference between the previous and current value of Glacite Powder
		 */
		private var glacitePowderDiff = 0

		/**
		 * The initial value of the timer for the difference update delay countdown.
		 */
		private var startTime = System.currentTimeMillis()

		private val TITLE: MutableText = Text.literal("Powders").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)


		// disgusting hack to get around text renderer issues.
		// the ctor eventually tries to get the font's height, which doesn't work
		//   when called before the client window is created (roughly).
		// the rebdering god 2 from the fabricord explained that detail, thanks!
		//coppied from the HodCommsWidget to be used in the same place
		@JvmField
		val INSTANCE: HudPowderWidget = HudPowderWidget()
		@JvmField
		val INSTANCE_CFG: HudPowderWidget = HudPowderWidget()

		/**
		 * Converts a string with a number and commas between digits to an integer value.
		 *
		 * @param str a string with a number and commas between digits
		 * @return integer value
		 */
		private fun parsePowder(str: String): Int {
			return try {
				NUMBER_FORMAT.parse(str).toInt()
			} catch (e: ParseException) {
				0
			}
		}

		/**
		 * Converts Powder and difference values to a string and adds commas to the digits of the numbers.
		 *
		 * @param powder the value of Mithril, Gemstone Powder, or Glacite Powder
		 * @param diff   the difference between the previous and current value of Mithril, Gemstone, or Glacite Powder
		 * @return formatted string
		 */
		private fun formatPowderString(powder: Int, diff: Int): String {
			if (diff == 0) return NUMBER_FORMAT.format(powder.toLong())
			return NUMBER_FORMAT.format(powder.toLong()) + (if (diff > 0) " (+" else " (") + NUMBER_FORMAT.format(diff.toLong()) + ")"
		}

		/**
		 * Updates Powders and difference values when Powder values change or every 2 seconds.
		 */
		private fun updatePowders() {
			val elapsedTime = System.currentTimeMillis() - startTime

			val newMithrilPowder = parsePowder(DwarvenHud.mithrilPowder)
			val newGemstonePowder = parsePowder(DwarvenHud.gemStonePowder)
			val newGlacitePowder = parsePowder(DwarvenHud.glacitePowder)

			if (newMithrilPowder != mithrilPowder || newGemstonePowder != gemstonePowder || newGlacitePowder != glacitePowder || elapsedTime > 2000) {
				startTime = System.currentTimeMillis()

				mithrilPowderDiff = newMithrilPowder - mithrilPowder
				gemstonePowderDiff = newGemstonePowder - gemstonePowder
				glacitePowderDiff = newGlacitePowder - glacitePowder

				mithrilPowder = newMithrilPowder
				gemstonePowder = newGemstonePowder
				glacitePowder = newGlacitePowder
			}
		}
	}
}
