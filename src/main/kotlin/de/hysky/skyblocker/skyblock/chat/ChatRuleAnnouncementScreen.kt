package de.hysky.skyblocker.skyblock.chat

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object ChatRuleAnnouncementScreen {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private var timer = 0f
	private var text: Text? = null

	fun init() {
		HudRenderEvents.BEFORE_CHAT.register(HudRenderStage { context: DrawContext?, tickDelta: Float ->
			if (timer <= 0 || text == null) {
				return@register
			}
			render(context, tickDelta)
		})
	}

	/**
	 * renders [ChatRuleAnnouncementScreen.text] to the middle of the top of the screen.
	 * @param context render context
	 * @param tickDelta difference from last render to remove from timer
	 */
	private fun render(context: DrawContext?, tickDelta: Float) {
		val scale = SkyblockerConfigManager.get().chat.chatRuleConfig.announcementScale
		//decrement timer
		timer -= tickDelta
		//scale text up and center
		val matrices = context!!.matrices
		matrices.push()
		matrices.translate((context.scaledWindowWidth / 2f).toDouble(), context.scaledWindowHeight * 0.3, 0.0)
		matrices.scale(scale.toFloat(), scale.toFloat(), 0f)
		//render text
		context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, 0, 0, -0x1)

		matrices.pop()
	}

	fun setText(newText: Text?) {
		text = newText
		timer = SkyblockerConfigManager.get().chat.chatRuleConfig.announcementLength.toFloat()
	}
}
