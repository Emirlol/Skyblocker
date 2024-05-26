package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget

abstract class PipelineStage {
	protected var primary: ArrayList<Widget>? = null
	protected var secondary: ArrayList<Widget>? = null

	abstract fun run(screenW: Int, screenH: Int)
}
