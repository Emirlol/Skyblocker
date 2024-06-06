package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class AoteFilter : SimpleChatFilter("^There are blocks in the way!$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideAOTE
	}
}
