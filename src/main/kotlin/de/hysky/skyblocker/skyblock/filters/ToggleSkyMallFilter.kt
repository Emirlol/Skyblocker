package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

object ToggleSkyMallFilter : SimpleChatFilter("^You can disable this messaging by toggling Sky Mall in your /hotm!$") {
	override fun state(): ChatFilterResult = SkyblockerConfigManager.config.chat.hideToggleSkyMall
}
