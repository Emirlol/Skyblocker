package de.hysky.skyblocker.utils.render.title

import com.mojang.brigadier.CommandDispatcher
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import de.hysky.skyblocker.utils.scheduler.Scheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler.Companion.queueOpenScreenCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.math.MathHelper

object TitleContainer {
	/**
	 * The set of titles which will be rendered.
	 *
	 * @see .containsTitle
	 * @see .addTitle
	 * @see .addTitle
	 * @see .removeTitle
	 */
	private val titles: MutableSet<Title> = LinkedHashSet()

	fun init() {
		HudRenderEvents.BEFORE_CHAT.register(HudRenderStage { obj: DrawContext?, context: Float -> render(context) })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(
				ClientCommandManager.literal("skyblocker")
					.then(
						ClientCommandManager.literal("hud")
							.then(ClientCommandManager.literal("titleContainer")
								.executes(queueOpenScreenCommand { TitleContainerConfigScreen() })
							)
					)
			)
		})
	}

	/**
	 * Returns `true` if the title is currently shown.
	 *
	 * @param title the title to check
	 * @return whether the title in currently shown
	 */
	@JvmStatic
	fun containsTitle(title: Title): Boolean {
		return titles.contains(title)
	}

	/**
	 * Adds a title to be shown
	 *
	 * @param title the title to be shown
	 * @return whether the title is already currently being shown
	 */
	fun addTitle(title: Title): Boolean {
		if (titles.add(title)) {
			title.resetPos()
			return true
		}
		return false
	}

	/**
	 * Adds a title to be shown for a set number of ticks
	 *
	 * @param title the title to be shown
	 * @param ticks the number of ticks to show the title
	 * @return whether the title is already currently being shown
	 */
	fun addTitle(title: Title, ticks: Int): Boolean {
		if (addTitle(title)) {
			Scheduler.INSTANCE.schedule({ removeTitle(title) }, ticks)
			return true
		}
		return false
	}

	/**
	 * Stops showing a title
	 *
	 * @param title the title to stop showing
	 */
	@JvmStatic
	fun removeTitle(title: Title) {
		titles.remove(title)
	}

	private fun render(context: DrawContext, tickDelta: Float) {
		render(context, titles, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y, tickDelta)
	}

	fun render(context: DrawContext, titles: Set<Title>, xPos: Int, yPos: Int, tickDelta: Float) {
		val client = MinecraftClient.getInstance()
		val textRenderer = client.textRenderer

		// Calculate Scale to use
		val scale = 3f * (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale / 100f)

		// Grab direction and alignment values
		val direction = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction
		val alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment
		// x/y refer to the starting position for the text
		// y always starts at yPos
		var x = 0f
		var y = yPos.toFloat()

		//Calculate the width of combined text
		var width = 0f
		for (title in titles) {
			width += textRenderer.getWidth(title.text) * scale + 10
		}

		if (alignment == UIAndVisualsConfig.Alignment.MIDDLE) {
			x = if (direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
				//If middle aligned horizontally, start the xPosition at half of the width to the left.
				xPos - (width / 2)
			} else {
				//If middle aligned vertically, start at xPos, we will shift each text to the left later
				xPos.toFloat()
			}
		}
		if (alignment == UIAndVisualsConfig.Alignment.LEFT || alignment == UIAndVisualsConfig.Alignment.RIGHT) {
			//If left or right aligned, start at xPos, we will shift each text later
			x = xPos.toFloat()
		}

		for (title in titles) {
			//Calculate which x the text should use
			var xToUse = if (direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
				if (alignment == UIAndVisualsConfig.Alignment.RIGHT) x - (textRenderer.getWidth(title.text) * scale) else  //if right aligned we need the text position to be aligned on the right side.
					x
			} else {
				if (alignment == UIAndVisualsConfig.Alignment.MIDDLE) x - (textRenderer.getWidth(title.text) * scale) / 2 else  //if middle aligned we need the text position to be aligned in the middle.
					if (alignment == UIAndVisualsConfig.Alignment.RIGHT) x - (textRenderer.getWidth(title.text) * scale) else  //if right aligned we need the text position to be aligned on the right side.
						x
			}

			//Start displaying the title at the correct position, not at the default position
			if (title.isDefaultPos) {
				title.x = xToUse
				title.y = y
			}

			//Lerp the texts x and y variables
			title.x = MathHelper.lerp(tickDelta * 0.5f, title.x, xToUse)
			title.y = MathHelper.lerp(tickDelta * 0.5f, title.y, y)

			//Translate the matrix to the texts position and scale
			context.matrices.push()
			context.matrices.translate(title.x, title.y, 0f)
			context.matrices.scale(scale, scale, scale)

			//Draw text
			context.drawTextWithShadow(textRenderer, title.text, 0, 0, 0xFFFFFF)
			context.matrices.pop()

			//Calculate the x and y positions for the next title
			if (direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
				if (alignment == UIAndVisualsConfig.Alignment.MIDDLE || alignment == UIAndVisualsConfig.Alignment.LEFT) {
					//Move to the right if middle or left aligned
					x += textRenderer.getWidth(title.text) * scale + 10
				}

				if (alignment == UIAndVisualsConfig.Alignment.RIGHT) {
					//Move to the left if right aligned
					x -= textRenderer.getWidth(title.text) * scale + 10
				}
			} else {
				//Y always moves by the same amount if vertical
				y += textRenderer.fontHeight * scale + 10
			}
		}
	}
}