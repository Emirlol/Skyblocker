package de.hysky.skyblocker.skyblock.tabhud.widget

import net.minecraft.text.Text

class EmptyWidget : Widget(Text.empty(), 0) {
	override fun updateContent() {}
}
