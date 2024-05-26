package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper

class CameraPositionWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val yaw = CLIENT.getCameraEntity()!!.yaw.toDouble()
		val pitch = CLIENT.getCameraEntity()!!.pitch.toDouble()

		addComponent(PlainTextComponent(Text.literal("Yaw: " + String.format("%.3f", MathHelper.wrapDegrees(yaw)))))
		addComponent(PlainTextComponent(Text.literal("Pitch: " + String.format("%.3f", MathHelper.wrapDegrees(pitch)))))
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Camera Pos").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
		private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	}
}
