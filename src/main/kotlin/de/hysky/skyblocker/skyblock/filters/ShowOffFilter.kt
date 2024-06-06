package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants

object ShowOffFilter : SimpleChatFilter("(?:\\[[0-9]+] )?(?:[" + Constants.LEVEL_EMBLEMS + "] )?(?:\\[[A-Z+]+] )?([A-Za-z0-9_]+) (?:has|is friends with a|is wearing|is holding) \\[(.+)]") {
	override fun state() = SkyblockerConfigManager.config.chat.hideShowOff
}
