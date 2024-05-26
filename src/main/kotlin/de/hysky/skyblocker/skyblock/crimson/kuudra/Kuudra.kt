package de.hysky.skyblocker.skyblock.crimson.kuudra

import de.hysky.skyblocker.utils.Utils.isInKuudra
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object Kuudra {
	var phase: KuudraPhase = KuudraPhase.OTHER

	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> KuudraWaypoints.render() })
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { obj: MinecraftClient? -> KuudraWaypoints.load() })
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _handler: ClientPlayNetworkHandler?, _sender: PacketSender?, _client: MinecraftClient? -> reset() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onMessage(text) })
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: KuudraWaypoints? -> KuudraWaypoints.tick() }, 20)
	}

	private fun onMessage(text: Text, overlay: Boolean) {
		if (isInKuudra && !overlay) {
			val message = Formatting.strip(text.string)

			if (message == "[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!") {
				phase = KuudraPhase.RETRIEVE_SUPPLIES
			}

			if (message == "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!") {
				phase = KuudraPhase.DPS
			}

			if (message == "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!") {
				phase = KuudraPhase.OTHER
			}
		}
	}

	private fun reset() {
		phase = KuudraPhase.OTHER
	}

	enum class KuudraPhase {
		OTHER,
		RETRIEVE_SUPPLIES,
		DPS
	}
}
