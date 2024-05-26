package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.chat.ChatFilterResult
import dev.isxander.yacl3.config.v2.api.SerialEntry

class ChatConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var hideAbility: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideHeal: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideAOTE: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideImplosion: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideMoltenWave: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideAds: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideTeleportPad: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideCombo: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideAutopet: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideShowOff: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideToggleSkyMall: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideMimicKill: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideDeath: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideMana: Boolean = false

	@kotlin.jvm.JvmField
	@SerialEntry
	var hideDicer: ChatFilterResult = ChatFilterResult.PASS

	@kotlin.jvm.JvmField
	@SerialEntry
	var chatRuleConfig: ChatRuleConfig = ChatRuleConfig()

	class ChatRuleConfig {
		@kotlin.jvm.JvmField
		@SerialEntry
		var announcementLength: Int = 60

		@kotlin.jvm.JvmField
		@SerialEntry
		var announcementScale: Int = 3
	}
}
