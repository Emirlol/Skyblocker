package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.client.resource.language.I18n

class MiscConfig {
	@SerialEntry
	var richPresence: RichPresence = RichPresence()

	class RichPresence {

		@SerialEntry
		var enableRichPresence: Boolean = false

		@SerialEntry
		var info: Info = Info.LOCATION

		@SerialEntry
		var cycleMode: Boolean = false

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
