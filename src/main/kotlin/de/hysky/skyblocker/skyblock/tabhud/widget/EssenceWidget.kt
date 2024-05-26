package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows your dungeon essences (dungeon hub only)
class EssenceWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	private var undead: Text? = null
	private var wither: Text? = null
	private var diamond: Text? = null
	private var gold: Text? = null
	private var dragon: Text? = null
	private var spider: Text? = null
	private var ice: Text? = null
	private var crimson: Text? = null

	override fun updateContent() {
		wither = Widget.Companion.simpleEntryText(46, "Wither:", Formatting.DARK_PURPLE)
		spider = Widget.Companion.simpleEntryText(47, "Spider:", Formatting.DARK_PURPLE)
		undead = Widget.Companion.simpleEntryText(48, "Undead:", Formatting.DARK_PURPLE)
		dragon = Widget.Companion.simpleEntryText(49, "Dragon:", Formatting.DARK_PURPLE)
		gold = Widget.Companion.simpleEntryText(50, "Gold:", Formatting.DARK_PURPLE)
		diamond = Widget.Companion.simpleEntryText(51, "Diamond:", Formatting.DARK_PURPLE)
		ice = Widget.Companion.simpleEntryText(52, "Ice:", Formatting.DARK_PURPLE)
		crimson = Widget.Companion.simpleEntryText(53, "Crimson:", Formatting.DARK_PURPLE)

		val tc = TableComponent(2, 4, Formatting.DARK_AQUA.colorValue!!)

		tc.addToCell(0, 0, IcoTextComponent(Ico.WITHER, wither))
		tc.addToCell(0, 1, IcoTextComponent(Ico.STRING, spider))
		tc.addToCell(0, 2, IcoTextComponent(Ico.FLESH, undead))
		tc.addToCell(0, 3, IcoTextComponent(Ico.DRAGON, dragon))
		tc.addToCell(1, 0, IcoTextComponent(Ico.GOLD, gold))
		tc.addToCell(1, 1, IcoTextComponent(Ico.DIAMOND, diamond))
		tc.addToCell(1, 2, IcoTextComponent(Ico.ICE, ice))
		tc.addToCell(1, 3, IcoTextComponent(Ico.REDSTONE, crimson))
		this.addComponent(tc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Essences").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
