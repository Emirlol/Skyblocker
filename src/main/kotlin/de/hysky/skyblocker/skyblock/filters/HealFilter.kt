package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object HealFilter : SimpleChatFilter("^(?:You healed yourself for $NUMBER health!|[a-zA-Z0-9_]{2,16} healed you for $NUMBER health!)$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideHeal
}