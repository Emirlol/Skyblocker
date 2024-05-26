package de.hysky.skyblocker.utils.render.gui

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

/**
 * A more bare-bones version of Vanilla's Popup Screen. Meant to be extended.
 */
open class AbstractPopupScreen protected constructor(title: Text?, private val backgroundScreen: Screen) : Screen(title) {
	override fun close() {
		checkNotNull(this.client)
		client!!.setScreen(this.backgroundScreen)
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		backgroundScreen.render(context, -1, -1, delta)
		context.draw()
		RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC)
		this.renderInGameBackground(context)
	}

	override fun init() {
		super.init()
		initTabNavigation()
	}

	override fun initTabNavigation() {
		backgroundScreen.resize(this.client, this.width, this.height)
	}

	override fun onDisplayed() {
		super.onDisplayed()
		backgroundScreen.blur()
	}

	class EnterConfirmTextFieldWidget(textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, copyFrom: TextFieldWidget?, text: Text?, private val onEnter: Runnable) : TextFieldWidget(textRenderer, x, y, width, height, copyFrom, text) {
		constructor(textRenderer: TextRenderer?, width: Int, height: Int, text: Text?, onEnter: Runnable) : this(textRenderer, 0, 0, width, height, text, onEnter)

		constructor(textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, text: Text?, onEnter: Runnable) : this(textRenderer, x, y, width, height, null, text, onEnter)


		override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
			if (!super.keyPressed(keyCode, scanCode, modifiers)) {
				if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
					onEnter.run()
					return true
				}
			} else return true
			return false
		}
	}

	companion object {
		private val BACKGROUND_TEXTURE = Identifier("popup/background")

		/**
		 * These are the inner positions and size of the popup, not outer
		 */
        @JvmStatic
        fun drawPopupBackground(context: DrawContext, x: Int, y: Int, width: Int, height: Int) {
			context.drawGuiTexture(BACKGROUND_TEXTURE, x - 18, y - 18, width + 36, height + 36)
		}
	}
}