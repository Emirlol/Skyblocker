package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about the garden server
class GardenServerWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41)
		this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42)
		this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43)

		val copperText: Text = Widget.Companion.simpleEntryText(44, "Copper:", Formatting.WHITE)
		(copperText.siblings.first as MutableText).withColor(COPPER_COLOR)

		this.addComponent(IcoTextComponent(Ico.COPPER, copperText))

		val hasPesthunterBonus = PlayerListMgr.strAt(46) != null

		if (hasPesthunterBonus) {
			this.addComponent(IcoTextComponent(Ico.NETHERITE_UPGRADE_ST, PlayerListMgr.textAt(46)))
		}

		val offset = if (hasPesthunterBonus) 1 else 0

		val m = PlayerListMgr.regexAt(53 + offset, VISITOR_PATTERN)
		if (m == null) {
			this.addComponent(IcoTextComponent())
			return
		}

		val vis = m.group("vis").replace("[()]*".toRegex(), "")
		val col = if (vis == "Not Unlocked!" || vis == "Queue Full!") {
			Formatting.RED
		} else {
			Formatting.GREEN
		}
		val visitor: Text = Widget.Companion.simpleEntryText(vis, "Next Visitor: ", col)
		val v = IcoTextComponent(Ico.PLAYER, visitor)
		this.addComponent(v)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Server Info").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)

		//From the armor trim tooltip
		private const val COPPER_COLOR = 11823181

		// match the next visitor in the garden
		// group 1: visitor name
		private val VISITOR_PATTERN: Pattern = Pattern.compile("Visitors: (?<vis>.*)")
	}
}
