package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

class AdFilter : ChatPatternListener("(?:\\[[0-9]+\\] )?(?:[" + Constants.LEVEL_EMBLEMS + "] )?(?:\\[[A-Z+]+\\] )?([A-Za-z0-9_]+): (.+)") {
	public override fun onMatch(_message: Text?, matcher: Matcher?): Boolean {
		val message = matcher!!.group(2)
		for (adFilter in AD_FILTERS) if (adFilter.matcher(message).find()) return true
		return false
	}

	override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideAds
	}

	companion object {
		private val AD_FILTERS = arrayOf(
			Pattern.compile("^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|$)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(.)\\1{7,}"),
			Pattern.compile("\\W(?:on|in|check|at) my (?:ah|bin)(?:\\W|$)", Pattern.CASE_INSENSITIVE),
		)
	}
}