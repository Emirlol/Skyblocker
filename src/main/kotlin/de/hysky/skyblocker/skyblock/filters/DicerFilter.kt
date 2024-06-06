package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object DicerFilter : SimpleChatFilter("[A-Z]+ DROP! .*Dicer dropped [0-9]+x.+!$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideDicer
}
