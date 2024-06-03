package de.hysky.skyblocker.utils

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class BasePlaceholderScreen(title: Text) : Screen(title) {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
	}
}
