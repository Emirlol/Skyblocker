package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object FireFreezeStaffTimer {
	private var fireFreezeTimer: Long = 0

	init {
		HudRenderEvents.BEFORE_CHAT.register { context, _ -> onDraw(context) }
		ClientReceiveMessageEvents.GAME.register(::onChatMessage)
		ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
	}

	private fun onDraw(context: DrawContext) {
		val client = MinecraftClient.getInstance()

		if (client.currentScreen != null) return

		if (SkyblockerConfigManager.config.dungeons.theProfessor.fireFreezeStaffTimer && fireFreezeTimer != 0L) {
			val now = System.currentTimeMillis()

			if (now >= fireFreezeTimer + 5000) {
				reset()
				return
			}

			val message =
				if (fireFreezeTimer > now
				) String.format("%.2f", (fireFreezeTimer - now).toFloat() / 1000) + "s"
				else "NOW"

			val renderer = client.textRenderer
			val width = client.window.scaledWidth / 2
			val height = client.window.scaledHeight / 2

			context.drawCenteredTextWithShadow(
				renderer, "Fire freeze in: $message", width, height, 0xffffff
			)
		}
	}

	private fun reset() {
		fireFreezeTimer = 0
	}

	private fun onChatMessage(text: Text, overlay: Boolean) {
		if (!overlay
			&& SkyblockerConfigManager.config.dungeons.theProfessor.fireFreezeStaffTimer
			&& (Formatting.strip(text.string) == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
			fireFreezeTimer = System.currentTimeMillis() + 5000L
		}
	}
}
