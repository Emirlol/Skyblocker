package de.hysky.skyblocker.config

import de.hysky.skyblocker.config.configs.*
import dev.isxander.yacl3.config.v2.api.SerialEntry

class SkyblockerConfig {
	@SerialEntry
	var version: Int = SkyblockerConfigManager.CONFIG_VERSION

	@JvmField
    @SerialEntry
	var general: GeneralConfig = GeneralConfig()

	@JvmField
    @SerialEntry
	var uiAndVisuals: UIAndVisualsConfig = UIAndVisualsConfig()

	@JvmField
    @SerialEntry
	var helpers: HelperConfig = HelperConfig()

	@JvmField
    @SerialEntry
	var dungeons: DungeonsConfig = DungeonsConfig()

	@SerialEntry
	var foraging: ForagingConfig = ForagingConfig()

	@JvmField
    @SerialEntry
	var crimsonIsle: CrimsonIsleConfig = CrimsonIsleConfig()

	@JvmField
    @SerialEntry
	var mining: MiningConfig = MiningConfig()

	@JvmField
    @SerialEntry
	var farming: FarmingConfig = FarmingConfig()

	@JvmField
    @SerialEntry
	var otherLocations: OtherLocationsConfig = OtherLocationsConfig()

	@JvmField
    @SerialEntry
	var slayers: SlayersConfig = SlayersConfig()

	@JvmField
    @SerialEntry
	var chat: ChatConfig = ChatConfig()

	@JvmField
    @SerialEntry
	var quickNav: QuickNavigationConfig = QuickNavigationConfig()

	@JvmField
    @SerialEntry
	var misc: MiscConfig = MiscConfig()
}
