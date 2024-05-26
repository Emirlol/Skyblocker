package de.hysky.skyblocker.skyblock.rift

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.SlayerUtils.isInSlayer
import de.hysky.skyblocker.utils.SlayerUtils.slayerEntity
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.title.Title
import de.hysky.skyblocker.utils.render.title.TitleContainer.removeTitle
import net.minecraft.util.Formatting

object StakeIndicator {
	private val title = Title("skyblocker.rift.stakeNow", Formatting.RED)

	fun updateStake() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableSteakStakeIndicator || !isOnSkyblock || !isInTheRift || !islandArea.contains("Stillgore Château") || !isInSlayer) {
			removeTitle(title)
			return
		}
		val slayerEntity = slayerEntity
		if (slayerEntity != null && slayerEntity.displayName.toString().contains("҉")) {
			displayInTitleContainerAndPlaySound(title)
		} else {
			removeTitle(title)
		}
	}
}