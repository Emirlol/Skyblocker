package de.hysky.skyblocker.skyblock.garden

import de.hysky.skyblocker.config.HudConfigScreen
import de.hysky.skyblocker.config.SkyblockerConfig
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class FarmingHudConfigScreen(parent: Screen?) : HudConfigScreen(Text.literal("Farming HUD Config"), parent, FarmingHudWidget.Companion.INSTANCE) {
	override fun getConfigPos(config: SkyblockerConfig): List<IntIntMutablePair> {
		return java.util.List.of(
			IntIntMutablePair.of(config.farming.garden.farmingHud.x, config.farming.garden.farmingHud.y)
		)
	}

	override fun savePos(configManager: SkyblockerConfig, widgets: List<Widget>) {
		configManager.farming.garden.farmingHud.x = widgets.first.x
		configManager.farming.garden.farmingHud.y = widgets.first.y
	}
}
