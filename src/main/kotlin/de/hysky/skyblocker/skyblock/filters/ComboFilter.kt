package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object ComboFilter : SimpleChatFilter("^(\\+\\d+ Kill Combo( \\+\\d+(✯ Magic Find| coins per kill|☯ Combat Wisdom))?|Your Kill Combo has expired! You reached a \\d+ Kill Combo!)$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideCombo
}
