package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class MoltenWaveFilter : SimpleChatFilter("^Your Molten Wave hit " + NUMBER + " enem(?:y|ies) for " + NUMBER + " damage\\.$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideMoltenWave
	}
}
