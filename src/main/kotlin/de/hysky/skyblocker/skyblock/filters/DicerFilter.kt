package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class DicerFilter : SimpleChatFilter("[A-Z]+ DROP! .*Dicer dropped [0-9]+x.+!$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideDicer
	}
}
