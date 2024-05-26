package de.hysky.skyblocker.utils.render.gui

@JvmRecord
data class ColorHighlight(@JvmField val slot: Int, @JvmField val color: Int) {
	companion object {
		private const val RED_HIGHLIGHT = 64 shl 24 or (255 shl 16)
		private const val YELLOW_HIGHLIGHT = 128 shl 24 or (255 shl 16) or (255 shl 8)
		private const val GREEN_HIGHLIGHT = 128 shl 24 or (64 shl 16) or (196 shl 8) or 64
		private const val GRAY_HIGHLIGHT = 128 shl 24 or (64 shl 16) or (64 shl 8) or 64

		@JvmStatic
        fun red(slot: Int): ColorHighlight {
			return ColorHighlight(slot, RED_HIGHLIGHT)
		}

		@JvmStatic
        fun yellow(slot: Int): ColorHighlight {
			return ColorHighlight(slot, YELLOW_HIGHLIGHT)
		}

		@JvmStatic
        fun green(slot: Int): ColorHighlight {
			return ColorHighlight(slot, GREEN_HIGHLIGHT)
		}

		@JvmStatic
        fun gray(slot: Int): ColorHighlight {
			return ColorHighlight(slot, GRAY_HIGHLIGHT)
		}
	}
}