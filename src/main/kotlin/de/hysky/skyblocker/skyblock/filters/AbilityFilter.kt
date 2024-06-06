package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

object AbilityFilter : SimpleChatFilter("^(?:This ability is on cooldown for " + NUMBER + "s\\.|No more charges, next one in " + NUMBER + "s!)$") {
	override fun state(): ChatFilterResult = SkyblockerConfigManager.config.chat.hideAbility
}
