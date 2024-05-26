package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult

class TeleportPadFilter : SimpleChatFilter(
	"^(Warped from the .* Teleport Pad to the .* Teleport Pad!" +
			"|This Teleport Pad does not have a destination set!)$"
) {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.get().chat.hideTeleportPad
	}
}