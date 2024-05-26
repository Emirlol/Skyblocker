package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst

class PlaceStage(builder: ScreenBuilder, descr: JsonObject) : PipelineStage() {
	private enum class PlaceLocation(private val str: String) {
		CENTER("center"),
		TOPCENT("centerTop"),
		BOTCENT("centerBot"),
		LEFTCENT("centerLeft"),
		RIGHTCENT("centerRight"),
		TRCORNER("cornerTopRight"),
		TLCORNER("cornerTopLeft"),
		BRCORNER("cornerBotRight"),
		BLCORNER("cornerBotLeft");

		companion object {
			@Throws(NoSuchElementException::class)
			fun parse(s: String): PlaceLocation {
				for (d in entries) {
					if (d.str == s) {
						return d
					}
				}
				throw NoSuchElementException("\"$s\" is not a valid location for a place op!")
			}
		}
	}

	private val where = PlaceLocation.parse(descr["where"].asString)

	init {
		this.primary = ArrayList(descr.getAsJsonArray("apply_to")
			.asList()
			.stream()
			.map { x: JsonElement -> builder.getInstance(x.asString) }
			.limit(1)
			.toList())
	}

	override fun run(screenW: Int, screenH: Int) {
		val wid = primary!!.first
		when (where) {
			PlaceLocation.CENTER -> {
				wid.x = (screenW - wid.width) / 2
				wid.y = (screenH - wid.height) / 2
			}

			PlaceLocation.TOPCENT -> {
				wid.x = (screenW - wid.width) / 2
				wid.y = ScreenConst.getScreenPad()
			}

			PlaceLocation.BOTCENT -> {
				wid.x = (screenW - wid.width) / 2
				wid.y = screenH - wid.height - ScreenConst.getScreenPad()
			}

			PlaceLocation.LEFTCENT -> {
				wid.x = ScreenConst.getScreenPad()
				wid.y = (screenH - wid.height) / 2
			}

			PlaceLocation.RIGHTCENT -> {
				wid.x = screenW - wid.width - ScreenConst.getScreenPad()
				wid.y = (screenH - wid.height) / 2
			}

			PlaceLocation.TLCORNER -> {
				wid.x = ScreenConst.getScreenPad()
				wid.y = ScreenConst.getScreenPad()
			}

			PlaceLocation.TRCORNER -> {
				wid.x = screenW - wid.width - ScreenConst.getScreenPad()
				wid.y = ScreenConst.getScreenPad()
			}

			PlaceLocation.BLCORNER -> {
				wid.x = ScreenConst.getScreenPad()
				wid.y = screenH - wid.height - ScreenConst.getScreenPad()
			}

			PlaceLocation.BRCORNER -> {
				wid.x = screenW - wid.width - ScreenConst.getScreenPad()
				wid.y = screenH - wid.height - ScreenConst.getScreenPad()
			}
		}
	}
}