package de.hysky.skyblocker.utils.render.gui

data class ColorHighlight(val slot: Int, val color: Int) {
	companion object {
		private const val RED_HIGHLIGHT = 64 shl 24 or (255 shl 16)
		private const val YELLOW_HIGHLIGHT = 128 shl 24 or (255 shl 16) or (255 shl 8)
		private const val GREEN_HIGHLIGHT = 128 shl 24 or (64 shl 16) or (196 shl 8) or 64
		private const val GRAY_HIGHLIGHT = 128 shl 24 or (64 shl 16) or (64 shl 8) or 64

        fun red(slot: Int) = ColorHighlight(slot, RED_HIGHLIGHT)
        fun yellow(slot: Int) = ColorHighlight(slot, YELLOW_HIGHLIGHT)
        fun green(slot: Int) = ColorHighlight(slot, GREEN_HIGHLIGHT)
        fun gray(slot: Int) = ColorHighlight(slot, GRAY_HIGHLIGHT)
	}
}