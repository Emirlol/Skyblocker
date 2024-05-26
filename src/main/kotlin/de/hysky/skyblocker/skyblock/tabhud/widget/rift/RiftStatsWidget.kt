package de.hysky.skyblocker.skyblock.tabhud.widget.rift

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class RiftStatsWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		val riftDamage: Text = Widget.Companion.simpleEntryText(64, "RDG", Formatting.DARK_PURPLE)
		val rdg = IcoTextComponent(Ico.DIASWORD, riftDamage)

		val speed: Text = Widget.Companion.simpleEntryText(65, "SPD", Formatting.WHITE)
		val spd = IcoTextComponent(Ico.SUGAR, speed)

		val intelligence: Text = Widget.Companion.simpleEntryText(66, "INT", Formatting.AQUA)
		val intel = IcoTextComponent(Ico.ENCHANTED_BOOK, intelligence)

		val manaRegen: Text = Widget.Companion.simpleEntryText(67, "MRG", Formatting.AQUA)
		val mrg = IcoTextComponent(Ico.DIAMOND, manaRegen)

		val tc = TableComponent(2, 2, Formatting.AQUA.colorValue!!)
		tc.addToCell(0, 0, rdg)
		tc.addToCell(0, 1, spd)
		tc.addToCell(1, 0, intel)
		tc.addToCell(1, 1, mrg)

		this.addComponent(tc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Stats").formatted(Formatting.DARK_AQUA, Formatting.BOLD)
	}
}