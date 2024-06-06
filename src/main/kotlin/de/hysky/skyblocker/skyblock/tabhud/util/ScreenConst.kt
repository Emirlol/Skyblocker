package de.hysky.skyblocker.skyblock.tabhud.util

import de.hysky.skyblocker.config.SkyblockerConfigManager

object ScreenConst {
	const val WIDGET_PAD: Int = 5
	const val WIDGET_PAD_HALF: Int = 3
	private const val SCREEN_PAD_BASE = 20

	val screenPad: Int
		get() = (1f / (SkyblockerConfigManager.config.uiAndVisuals.tabHud.tabHudScale.toFloat() / 100f) * SCREEN_PAD_BASE).toInt()
}
