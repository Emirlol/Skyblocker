package de.hysky.skyblocker.skyblock.tabhud.util

import net.minecraft.util.math.MathHelper

object Colors {
	/**
	 * @param pcnt Percentage between 0% and 100%, NOT 0-1!
	 * @return an int representing a color, where 100% = green and 0% = red
	 */
	@JvmStatic
	fun pcntToCol(pcnt: Float): Int {
		return MathHelper.hsvToRgb(pcnt / 300, 1f, 1f)
	}
}
