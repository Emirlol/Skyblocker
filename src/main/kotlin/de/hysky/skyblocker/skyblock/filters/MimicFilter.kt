package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.text.Text
import java.util.regex.Matcher

class MimicFilter : ChatPatternListener(".*?(?:Mimic dead!?|Mimic Killed!|\\\$SKYTILS-DUNGEON-SCORE-MIMIC\\$|\\Q" + SkyblockerConfigManager.config.dungeons.mimicMessage.mimicMessage + "\\E)$") {
	public override fun state(): ChatFilterResult {
		return SkyblockerConfigManager.config.chat.hideMimicKill
	}

	override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		if (!isInDungeons || !DungeonScore.isDungeonStarted() || !DungeonScore.isMimicOnCurrentFloor()) return false
		DungeonScore.onMimicKill() //Only called when the message is cancelled | sent to action bar, complementing DungeonScore#checkMessageForMimic
		return true
	}
}
