package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.client.resource.language.I18n

class MiscConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var richPresence: RichPresence = RichPresence()

	class RichPresence {
		@kotlin.jvm.JvmField
		@SerialEntry
		var enableRichPresence: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var info: Info = Info.LOCATION

		@kotlin.jvm.JvmField
		@SerialEntry
		var cycleMode: Boolean = false

		@kotlin.jvm.JvmField
		@SerialEntry
		var customMessage: String = "Playing Skyblock"
	}

	enum class Info {
		PURSE, BITS, LOCATION;

		override fun toString(): String {
			return I18n.translate("skyblocker.config.misc.richPresence.info.$name")
		}
	}
}
