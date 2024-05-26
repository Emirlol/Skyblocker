package de.hysky.skyblocker.skyblock.rift

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilled
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilledWithBeaconBeam
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object EffigyWaypoints {
	private val LOGGER: Logger = LoggerFactory.getLogger(EffigyWaypoints::class.java)
	private val EFFIGIES: List<BlockPos> = java.util.List.of(
		BlockPos(150, 79, 95),  //Effigy 1
		BlockPos(193, 93, 119),  //Effigy 2
		BlockPos(235, 110, 147),  //Effigy 3
		BlockPos(293, 96, 134),  //Effigy 4
		BlockPos(262, 99, 94),  //Effigy 5
		BlockPos(240, 129, 118) //Effigy 6
	)
	private val UNBROKEN_EFFIGIES: MutableList<BlockPos> = ArrayList()

	fun updateEffigies() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableEffigyWaypoints || !isOnSkyblock || !isInTheRift || !islandArea.contains("Stillgore Château")) return

		UNBROKEN_EFFIGIES.clear()

		try {
			for (i in Utils.STRING_SCOREBOARD.indices) {
				val line = Utils.STRING_SCOREBOARD[i]!!

				if (line.contains("Effigies")) {
					val effigiesText: MutableList<Text> = ArrayList()
					val prefixAndSuffix = Utils.TEXT_SCOREBOARD[i]!!.siblings

					//Add contents of prefix and suffix to list
					effigiesText.addAll(prefixAndSuffix.first.siblings)
					effigiesText.addAll(prefixAndSuffix[1].siblings)

					for (i2 in 1 until effigiesText.size) {
						if (effigiesText[i2].style.color == TextColor.fromFormatting(Formatting.GRAY)) UNBROKEN_EFFIGIES.add(EFFIGIES[i2 - 1])
					}
				}
			}
		} catch (e: NullPointerException) {
			LOGGER.error("[Skyblocker] Error while updating effigies.", e)
		}
	}

	fun render(context: WorldRenderContext?) {
		if (SkyblockerConfigManager.get().slayers.vampireSlayer.enableEffigyWaypoints && islandArea.contains("Stillgore Château")) {
			for (effigy in UNBROKEN_EFFIGIES) {
				val colorComponents = DyeColor.RED.colorComponents
				if (SkyblockerConfigManager.get().slayers.vampireSlayer.compactEffigyWaypoints) {
					renderFilledWithBeaconBeam(context!!, effigy.down(6), colorComponents, 0.5f, true)
				} else {
					renderFilledWithBeaconBeam(context!!, effigy, colorComponents, 0.5f, true)
					for (i in 1..5) {
						renderFilled(context, effigy.down(i), colorComponents, 0.5f - (0.075f * i), true)
					}
				}
			}
		}
	}
}