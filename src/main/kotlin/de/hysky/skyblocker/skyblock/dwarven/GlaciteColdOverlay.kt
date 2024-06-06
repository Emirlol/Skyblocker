package de.hysky.skyblocker.skyblock.dwarven

import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils
import de.hysky.skyblocker.utils.Utils.isInDwarvenMines
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.regex.Pattern

object GlaciteColdOverlay {
	private val POWDER_SNOW_OUTLINE = Identifier("textures/misc/powder_snow_outline.png")
	private val COLD_PATTERN: Pattern = Pattern.compile("Cold: -(\\d+)❄")
	private var cold = 0
	private var resetTime = System.currentTimeMillis()

	fun init() {
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: GlaciteColdOverlay? -> update() }, 20)
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> coldReset(text) })
	}

	private fun coldReset(text: Text, b: Boolean) {
		if (!isInDwarvenMines || b) {
			return
		}
		val message = text.string
		if (message == "The warmth of the campfire reduced your ❄ Cold to 0!") {
			cold = 0
			resetTime = System.currentTimeMillis()
		}
	}

	private fun update() {
		if (!isInDwarvenMines || System.currentTimeMillis() - resetTime < 3000 || !SkyblockerConfigManager.config.mining.glacite.coldOverlay) {
			cold = 0
			return
		}
		for (line in Utils.STRING_SCOREBOARD) {
			val coldMatcher = COLD_PATTERN.matcher(line)
			if (coldMatcher.matches()) {
				val value = coldMatcher.group(1)
				cold = value.toInt()
				return
			}
		}
		cold = 0
	}

	private fun renderOverlay(context: DrawContext, texture: Identifier, opacity: Float) {
		RenderSystem.disableDepthTest()
		RenderSystem.depthMask(false)
		RenderSystem.enableBlend()
		context.setShaderColor(1.0f, 1.0f, 1.0f, opacity)
		context.drawTexture(texture, 0, 0, -90, 0.0f, 0.0f, context.scaledWindowWidth, context.scaledWindowHeight, context.scaledWindowWidth, context.scaledWindowHeight)
		RenderSystem.disableBlend()
		RenderSystem.depthMask(true)
		RenderSystem.enableDepthTest()
		context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
	}

	@JvmStatic
	fun render(context: DrawContext) {
		if (isInDwarvenMines && SkyblockerConfigManager.config.mining.glacite.coldOverlay) {
			renderOverlay(context, POWDER_SNOW_OUTLINE, cold / 100f)
		}
	}
}
