package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class ComboFilter : SimpleChatFilter("^(\\+\\d+ Kill Combo( \\+\\d+(✯ Magic Find| coins per kill|☯ Combat Wisdom))?|Your Kill Combo has expired! You reached a \\d+ Kill Combo!)$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.get().chat.hideCombo
	}
}
