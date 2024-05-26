package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// empty widget for when nothing can be shown
class ErrorWidget : Widget {
	var error: Text = Text.of("No info available!")

	constructor() : super(TITLE, Formatting.RED.colorValue)

	constructor(error: String?) : super(TITLE, Formatting.RED.colorValue) {
		this.error = Text.of(error)
	}

	override fun updateContent() {
		val inf = PlainTextComponent(this.error)
		this.addComponent(inf)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Error").formatted(
			Formatting.RED,
			Formatting.BOLD
		)
	}
}
