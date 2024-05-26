package de.hysky.skyblocker.utils

object ColorUtils {
	/**
	 * Takes an RGB color as an integer and returns an array of the color's components as floats, in RGB format.
	 * @param color The color to get the components of.
	 * @return An array of the color's components as floats.
	 */
	@JvmStatic
	fun getFloatComponents(color: Int): FloatArray {
		return floatArrayOf(
			((color shr 16) and 0xFF) / 255f,
			((color shr 8) and 0xFF) / 255f,
			(color and 0xFF) / 255f
		)
	}
}
