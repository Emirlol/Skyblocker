package de.hysky.skyblocker.skyblock.rift

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.title.Title
import de.hysky.skyblocker.utils.render.title.TitleContainer.removeTitle
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting

object HealingMelonIndicator {
	private val title = Title("skyblocker.rift.healNow", Formatting.DARK_RED)

	@JvmStatic
	fun updateHealth() {
		if (!SkyblockerConfigManager.config.slayers.vampireSlayer.enableHealingMelonIndicator || !isOnSkyblock || !isInTheRift || !islandArea.contains("Stillgore Château")) {
			removeTitle(title)
			return
		}
		val player = MinecraftClient.getInstance().player
		if (player != null && player.health <= SkyblockerConfigManager.config.slayers.vampireSlayer.healingMelonHealthThreshold * 2f) {
			displayInTitleContainerAndPlaySound(title)
		} else {
			removeTitle(title)
		}
	}
}