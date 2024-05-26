package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.HudConfigScreen
import de.hysky.skyblocker.config.SkyblockerConfig
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.MiningConfig.DwarvenHudStyle
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class DwarvenHudConfigScreen @JvmOverloads constructor(parent: Screen? = null) : HudConfigScreen(Text.literal("Dwarven HUD Config"), parent, java.util.List.of(HudCommsWidget.INSTANCE_CFG, HudPowderWidget.INSTANCE_CFG)) {
	init {
		if (SkyblockerConfigManager.get().mining.dwarvenHud.style == DwarvenHudStyle.CLASSIC) {
			HudCommsWidget.INSTANCE_CFG.width = 200
			HudCommsWidget.INSTANCE_CFG.height = 20 * CFG_COMMS.size
			HudPowderWidget.INSTANCE_CFG.width = 200
			HudPowderWidget.INSTANCE_CFG.height = 40
		}
	}

	override fun getConfigPos(config: SkyblockerConfig): List<IntIntMutablePair> {
		return java.util.List.of(
			IntIntMutablePair.of(config.mining.dwarvenHud.commissionsX, config.mining.dwarvenHud.commissionsY),
			IntIntMutablePair.of(config.mining.dwarvenHud.powderX, config.mining.dwarvenHud.powderY)
		)
	}

	override fun renderWidget(context: DrawContext, widgets: List<Widget>) {
		DwarvenHud.render(HudCommsWidget.INSTANCE_CFG, HudPowderWidget.INSTANCE_CFG, context, widgets.first.x, widgets.first.y, widgets[1].x, widgets[1].y, CFG_COMMS)
	}

	override fun savePos(configManager: SkyblockerConfig, widgets: List<Widget>) {
		configManager.mining.dwarvenHud.commissionsX = widgets.first.x
		configManager.mining.dwarvenHud.commissionsY = widgets.first.y
		configManager.mining.dwarvenHud.powderX = widgets[1].x
		configManager.mining.dwarvenHud.powderY = widgets[1].y
	}

	companion object {
		private val CFG_COMMS: List<Commission> = java.util.List.of(Commission("Test Commission 1", "1%"), Commission("Test Commission 2", "2%"))
	}
}
