package de.hysky.skyblocker.compatibility.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import de.hysky.skyblocker.config.SkyblockerConfigManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen

@Environment(EnvType.CLIENT)
class ModMenuEntry : ModMenuApi {
	override fun getModConfigScreenFactory() = ConfigScreenFactory { parent: Screen? -> SkyblockerConfigManager.createGUI(parent) }
}