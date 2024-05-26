package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object QuiverWarning {
	private var warning: Type? = null

	fun init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(AllowGame { obj: Text?, text: Boolean -> onChatMessage(text) })
		Scheduler.INSTANCE.scheduleCyclic( { obj: QuiverWarning? -> update() }, 10)
	}

	fun onChatMessage(text: Text, overlay: Boolean): Boolean {
		val message = text.string
		if (SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarning && message.endsWith("left in your Quiver!")) {
			MinecraftClient.getInstance().inGameHud.setDefaultTitleFade()
			if (message.startsWith("You only have 50")) {
				onChatMessage(Type.FIFTY_LEFT)
			} else if (message.startsWith("You only have 10")) {
				onChatMessage(Type.TEN_LEFT)
			} else if (message.startsWith("You don't have any more")) {
				onChatMessage(Type.EMPTY)
			}
		}
		return true
	}

	private fun onChatMessage(warning: Type) {
		if (!isInDungeons) {
			MinecraftClient.getInstance().inGameHud.setTitle(Text.translatable(warning.key).formatted(Formatting.RED))
		} else if (SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarningInDungeons) {
			MinecraftClient.getInstance().inGameHud.setTitle(Text.translatable(warning.key).formatted(Formatting.RED))
			QuiverWarning.warning = warning
		}
	}

	fun update() {
		if (warning != null && SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarning && SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarningAfterDungeon && !isInDungeons) {
			val inGameHud = MinecraftClient.getInstance().inGameHud
			inGameHud.setDefaultTitleFade()
			inGameHud.setTitle(Text.translatable(warning!!.key).formatted(Formatting.RED))
			warning = null
		}
	}

	private enum class Type(key: String) {
		NONE(""),
		FIFTY_LEFT("50Left"),
		TEN_LEFT("10Left"),
		EMPTY("empty");

		val key: String = "skyblocker.quiverWarning.$key"
	}
}
