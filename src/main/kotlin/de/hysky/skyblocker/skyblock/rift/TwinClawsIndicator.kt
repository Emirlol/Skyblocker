package de.hysky.skyblocker.skyblock.rift

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.SlayerUtils.getEntityArmorStands
import de.hysky.skyblocker.utils.SlayerUtils.isInSlayer
import de.hysky.skyblocker.utils.SlayerUtils.slayerEntity
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.title.Title
import de.hysky.skyblocker.utils.render.title.TitleContainer.containsTitle
import de.hysky.skyblocker.utils.render.title.TitleContainer.removeTitle
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.minecraft.util.Formatting

object TwinClawsIndicator {
	private val title = Title("skyblocker.rift.iceNow", Formatting.AQUA)
	private var scheduled = false

	fun updateIce() {
		if (!SkyblockerConfigManager.config.slayers.vampireSlayer.enableHolyIceIndicator || !isOnSkyblock || !isInTheRift || !(islandArea.contains("Stillgore Château")) || !isInSlayer) {
			removeTitle(title)
			return
		}

		val slayerEntity = slayerEntity ?: return

		var anyClaws = false
		for (entity in getEntityArmorStands(slayerEntity)) {
			if (entity.displayName.toString().contains("TWINCLAWS")) {
				anyClaws = true
				if (!containsTitle(title) && !scheduled) {
					scheduled = true
					Scheduler.INSTANCE.schedule({
						displayInTitleContainerAndPlaySound(title)
						scheduled = false
					}, SkyblockerConfigManager.config.slayers.vampireSlayer.holyIceIndicatorTickDelay)
				}
			}
		}
		if (!anyClaws) {
			removeTitle(title)
		}
	}
}