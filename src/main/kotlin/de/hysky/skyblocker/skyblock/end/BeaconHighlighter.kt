package de.hysky.skyblocker.skyblock.end

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheEnd
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilled
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

object BeaconHighlighter {
	@JvmField
    val beaconPositions: MutableList<BlockPos> = ArrayList()
	private val RED_COLOR_COMPONENTS = floatArrayOf(1.0f, 0.0f, 0.0f)

	/**
	 * Initializes the beacon highlighting system.
	 * [BeaconHighlighter.render] is called after translucent rendering.
	 */
	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _handler: ClientPlayNetworkHandler?, _sender: PacketSender?, _client: MinecraftClient? -> reset() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onMessage(text) })
	}

	private fun reset() {
		beaconPositions.clear()
	}

	private fun onMessage(text: Text, overlay: Boolean) {
		if (isInTheEnd && !overlay) {
			val message = text.string

			if (message.contains("SLAYER QUEST COMPLETE!") || message.contains("NICE! SLAYER BOSS SLAIN!")) reset()
		}
	}

	/**
	 * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
	 * is visible through walls.
	 *
	 * @param context An instance of WorldRenderContext for the RenderHelper to use
	 */
	private fun render(context: WorldRenderContext) {
		if (isInTheEnd && SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons) {
			for (pos in beaconPositions) {
				renderFilled(context, pos, RED_COLOR_COMPONENTS, 0.5f, true)
			}
		}
	}
}
