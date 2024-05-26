package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.HudConfigScreen
import de.hysky.skyblocker.config.SkyblockerConfig
import de.hysky.skyblocker.skyblock.tabhud.widget.EmptyWidget
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class CrystalsHudConfigScreen @JvmOverloads constructor(parent: Screen? = null) : HudConfigScreen(Text.of("Crystals HUD Config"), parent, WIDGET) {
	init {
		WIDGET.setDimensions(CrystalsHud.getDimensionsForConfig())
	}

	override fun getConfigPos(config: SkyblockerConfig): List<IntIntMutablePair> {
		return java.util.List.of(IntIntMutablePair.of(config.mining.crystalsHud.x, config.mining.crystalsHud.y))
	}

	override fun renderWidget(context: DrawContext, widgets: List<Widget>) {
		val size = CrystalsHud.getDimensionsForConfig()
		WIDGET.setDimensions(size)
		context.drawTexture(CrystalsHud.MAP_TEXTURE, WIDGET.x, WIDGET.y, 0f, 0f, size, size, size, size)
	}

	override fun savePos(configManager: SkyblockerConfig, widgets: List<Widget>) {
		configManager.mining.crystalsHud.x = widgets.first.x
		configManager.mining.crystalsHud.y = widgets.first.y
	}

	companion object {
		private val WIDGET = EmptyWidget()
	}
}
