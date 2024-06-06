package de.hysky.skyblocker.skyblock.end

import de.hysky.skyblocker.config.HudConfigScreen
import de.hysky.skyblocker.config.SkyblockerConfig
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class EndHudConfigScreen(parent: Screen?) : HudConfigScreen(Text.literal("End HUD Config"), parent, EndHudWidget) {
	override fun getConfigPos(config: SkyblockerConfig)= listOf(IntIntMutablePair.of(config.otherLocations.end.x, config.otherLocations.end.y))

	override fun savePos(configManager: SkyblockerConfig, widgets: List<Widget>) {
		configManager.otherLocations.end.x = widgets.first.x
		configManager.otherLocations.end.y = widgets.first.y
	}
}
