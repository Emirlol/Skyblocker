package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class ImplosionFilter : SimpleChatFilter("^Your Implosion hit " + NUMBER + " enem(?:y|ies) for " + NUMBER + " damage\\.$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.get().chat.hideImplosion
	}
}
