package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object AoteFilter : SimpleChatFilter("^There are blocks in the way!$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideAOTE
}
