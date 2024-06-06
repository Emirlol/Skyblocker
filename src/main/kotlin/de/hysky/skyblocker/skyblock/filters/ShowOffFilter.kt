package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import java.lang.String
import kotlin.arrayOf

class ShowOffFilter : SimpleChatFilter("(?:\\[[0-9]+\\] )?(?:[" + Constants.LEVEL_EMBLEMS + "] )?(?:\\[[A-Z+]+\\] )?([A-Za-z0-9_]+) (?:" + String.join("|", *SHOW_TYPES) + ") \\[(.+)\\]") {
	override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideShowOff
	}

	companion object {
		private val SHOW_TYPES = arrayOf("is holding", "is wearing", "is friends with a", "has")
	}
}
