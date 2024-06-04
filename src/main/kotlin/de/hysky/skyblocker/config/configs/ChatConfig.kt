package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.chat.ChatFilterResult
import dev.isxander.yacl3.config.v2.api.SerialEntry

class ChatConfig {
	@SerialEntry
	var hideAbility: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideHeal: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideAOTE: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideImplosion: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideMoltenWave: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideAds: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideTeleportPad: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideCombo: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideAutopet: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideShowOff: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideToggleSkyMall: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideMimicKill: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideDeath: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var hideMana: Boolean = false

	@SerialEntry
	var hideDicer: ChatFilterResult = ChatFilterResult.PASS

	@SerialEntry
	var chatRuleConfig: ChatRuleConfig = ChatRuleConfig()

	class ChatRuleConfig {

		@SerialEntry
		var announcementLength: Int = 60


		@SerialEntry
		var announcementScale: Int = 3
	}
}
