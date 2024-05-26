package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst

class AlignStage(builder: ScreenBuilder, descr: JsonObject) : PipelineStage() {
	private enum class AlignReference(private val str: String) {
		HORICENT("horizontalCenter"),
		VERTCENT("verticalCenter"),
		LEFTCENT("leftOfCenter"),
		RIGHTCENT("rightOfCenter"),
		TOPCENT("topOfCenter"),
		BOTCENT("botOfCenter"),
		TOP("top"),
		BOT("bot"),
		LEFT("left"),
		RIGHT("right");

		companion object {
			@Throws(NoSuchElementException::class)
			fun parse(s: String): AlignReference {
				for (d in entries) {
					if (d.str == s) {
						return d
					}
				}
				throw NoSuchElementException("\"$s\" is not a valid reference for an align op!")
			}
		}
	}

	private val reference = AlignReference.parse(descr["reference"].asString)

	init {
		this.primary = ArrayList(descr.getAsJsonArray("apply_to")
			.asList()
			.stream()
			.map { x: JsonElement -> builder.getInstance(x.asString) }
			.toList())
	}

	override fun run(screenW: Int, screenH: Int) {
		var wHalf: Int
		var hHalf: Int
		for (wid in primary!!) {
			when (this.reference) {
				AlignReference.HORICENT -> wid.x = (screenW - wid.width) / 2
				AlignReference.VERTCENT -> wid.y = (screenH - wid.height) / 2
				AlignReference.LEFTCENT -> {
					wHalf = screenW / 2
					wid.x = wHalf - ScreenConst.WIDGET_PAD_HALF - wid.width
				}

				AlignReference.RIGHTCENT -> {
					wHalf = screenW / 2
					wid.x = wHalf + ScreenConst.WIDGET_PAD_HALF
				}

				AlignReference.TOPCENT -> {
					hHalf = screenH / 2
					wid.y = hHalf - ScreenConst.WIDGET_PAD_HALF - wid.height
				}

				AlignReference.BOTCENT -> {
					hHalf = screenH / 2
					wid.y = hHalf + ScreenConst.WIDGET_PAD_HALF
				}

				AlignReference.TOP -> wid.y = ScreenConst.getScreenPad()
				AlignReference.BOT -> wid.y = screenH - wid.height - ScreenConst.getScreenPad()
				AlignReference.LEFT -> wid.x = ScreenConst.getScreenPad()
				AlignReference.RIGHT -> wid.x = screenW - wid.width - ScreenConst.getScreenPad()
			}
		}
	}
}
