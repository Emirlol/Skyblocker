package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class GoodToKnowWidget : Widget(TITLE, Formatting.BLUE.colorValue) {
	override fun updateContent() {
		// After you progress further the tab adds more info so we need to be careful of
		// that
		// In beginning it only shows montezuma, then timecharms and enigma souls are
		// added

		var headerPos = 0
		// this seems suboptimal, but I'm not sure if there's a way to do it better.
		// search for the GTK header and offset the rest accordingly.
		for (i in 45..49) {
			val str = PlayerListMgr.strAt(i)
			if (str != null && str.startsWith("Good to")) {
				headerPos = i
				break
			}
		}

		val posA = PlayerListMgr.textAt(headerPos + 2) // Can be times visited rift
		val posB = PlayerListMgr.textAt(headerPos + 4) // Can be lifetime motes or visited rift
		val posC = PlayerListMgr.textAt(headerPos + 6) // Can be lifetime motes

		var visitedRiftPos = 0
		var lifetimeMotesPos = 0

		// Check each position to see what is or isn't there so we don't try adding
		// invalid components
		if (posA != null && posA.string.contains("times")) visitedRiftPos = headerPos + 2
		if (posB != null && posB.string.contains("Motes")) lifetimeMotesPos = headerPos + 4
		if (posB != null && posB.string.contains("times")) visitedRiftPos = headerPos + 4
		if (posC != null && posC.string.contains("Motes")) lifetimeMotesPos = headerPos + 6

		val timesVisitedRift = if ((visitedRiftPos == headerPos + 4)) posB else if ((visitedRiftPos == headerPos + 2)) posA else Text.literal("No Data").formatted(Formatting.GRAY)
		val lifetimeMotesEarned = if ((lifetimeMotesPos == headerPos + 6)) posC else if ((lifetimeMotesPos == headerPos + 4)) posB else Text.literal("No Data").formatted(Formatting.GRAY)

		if (visitedRiftPos != 0) {
			this.addComponent(
				IcoTextComponent(
					Ico.EXPERIENCE_BOTTLE,
					Text.literal("Visited Rift: ").append(timesVisitedRift)
				)
			)
		}

		if (lifetimeMotesPos != 0) {
			this.addComponent(
				IcoTextComponent(Ico.PINK_DYE, Text.literal("Lifetime Earned: ").append(lifetimeMotesEarned))
			)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Good To Know").formatted(Formatting.BLUE, Formatting.BOLD)
	}
}
