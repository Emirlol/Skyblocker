package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class AbilityFilter : SimpleChatFilter("^(?:This ability is on cooldown for " + NUMBER + "s\\.|No more charges, next one in " + NUMBER + "s!)$") {
	override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideAbility
	}
}
