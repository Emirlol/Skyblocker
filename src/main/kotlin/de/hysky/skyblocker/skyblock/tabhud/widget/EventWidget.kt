package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows info about ongoing events (e.g. election)
class EventWidget(private val isInGarden: Boolean) : Widget(TITLE, Formatting.YELLOW.colorValue) {
	override fun updateContent() {
		// hypixel devs carefully inserting the most random edge cases #317:
		// the event info is placed a bit differently when in the garden.
		val offset = if ((isInGarden)) -1 else 0

		this.addSimpleIcoText(Ico.NTAG, "Name:", Formatting.YELLOW, 73 + offset)

		// this could look better
		val time: Text = Widget.Companion.plainEntryText(74 + offset)
		val t = IcoTextComponent(Ico.CLOCK, time)
		this.addComponent(t)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Event Info").formatted(Formatting.YELLOW, Formatting.BOLD)
	}
}
