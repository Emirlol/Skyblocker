package de.hysky.skyblocker.events

import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.DrawContext

/**
 * HUD render events that allow for proper layering between different HUD elements.
 * This should always be preferred over Fabric's [net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback].
 *
 * Perhaps in the future this system could be PR'd to Fabric.
 */
object HudRenderEvents {
	/**
	 * Called after the hotbar, status bars, and experience bar have been rendered.
	 */
	val AFTER_MAIN_HUD = createEventForStage()

	/**
	 * Called before the [net.minecraft.client.gui.hud.ChatHud] is rendered.
	 */
	val BEFORE_CHAT = createEventForStage()

	/**
	 * Called after the entire HUD is rendered.
	 */
	val LAST = createEventForStage()

	private fun createEventForStage(): Event<HudRenderStage> = EventFactory.createArrayBacked(HudRenderStage::class.java) { listeners: Array<HudRenderStage> ->
		HudRenderStage { context: DrawContext, tickDelta: Float ->
			for (listener in listeners) {
				listener.onRender(context, tickDelta)
			}
		}
	}

	/**
	 * Implementation Note: Similar to Fabric's [net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback]
	 */
	fun interface HudRenderStage {
		/**
		 * Called sometime during a specific HUD render stage.
		 *
		 * @param drawContext The [DrawContext] instance
		 * @param tickDelta Progress for linearly interpolating between the previous and current game state
		 */
		fun onRender(context: DrawContext, tickDelta: Float)
	}
}
