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
import de.hysky.skyblocker.utils.render.title.TitleContainer.removeTitle
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ManiaIndicator {
	private val title = Title("skyblocker.rift.mania", Formatting.RED)

	fun updateMania() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableManiaIndicator || !isOnSkyblock || !isInTheRift || !(islandArea.contains("Stillgore Château")) || !isInSlayer) {
			removeTitle(title)
			return
		}

		val slayerEntity = slayerEntity ?: return

		var anyMania = false
		for (entity in getEntityArmorStands(slayerEntity)) {
			if (entity.displayName.toString().contains("MANIA")) {
				anyMania = true
				val pos = MinecraftClient.getInstance().player!!.blockPos.down()
				val isGreen = MinecraftClient.getInstance().world!!.getBlockState(pos).block === Blocks.GREEN_TERRACOTTA
				title.text = Text.translatable("skyblocker.rift.mania").formatted(if (isGreen) Formatting.GREEN else Formatting.RED)
				displayInTitleContainerAndPlaySound(title)
			}
		}
		if (!anyMania) {
			removeTitle(title)
		}
	}
}