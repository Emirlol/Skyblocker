package de.hysky.skyblocker.skyblock.tabhud

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TabHud {
	var toggleB: KeyBinding? = null
	var toggleA: KeyBinding? = null

	// public static KeyBinding mapTgl;
    @JvmField
    var defaultTgl: KeyBinding? = null

	@JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("Skyblocker Tab HUD")

	fun init() {
		toggleB = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.skyblocker.toggleB",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"key.categories.skyblocker"
			)
		)
		toggleA = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.skyblocker.toggleA",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"key.categories.skyblocker"
			)
		)
		defaultTgl = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.skyblocker.defaultTgl",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"key.categories.skyblocker"
			)
		)
	}
}
