package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry

class ForagingConfig {
	@SerialEntry
	var hunting: Hunting = Hunting()

	class Hunting
}
