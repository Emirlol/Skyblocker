package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object ImplosionFilter : SimpleChatFilter("^Your Implosion hit $NUMBER enem(?:y|ies) for $NUMBER damage\\.$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideImplosion
}
