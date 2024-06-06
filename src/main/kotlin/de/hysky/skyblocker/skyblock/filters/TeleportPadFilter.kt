package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager

object TeleportPadFilter : SimpleChatFilter("^(Warped from the .* Teleport Pad to the .* Teleport Pad!|This Teleport Pad does not have a destination set!)$") {
	public override fun state() = SkyblockerConfigManager.config.chat.hideTeleportPad
}