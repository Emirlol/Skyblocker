package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object MoltenWaveFilter : SimpleChatFilter("^Your Molten Wave hit $NUMBER enem(?:y|ies) for $NUMBER damage\\.$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideMoltenWave
}
