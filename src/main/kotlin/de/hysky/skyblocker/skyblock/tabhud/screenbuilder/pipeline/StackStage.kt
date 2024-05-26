package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst

class StackStage(builder: ScreenBuilder, descr: JsonObject) : PipelineStage() {
	private enum class StackDirection(private val str: String) {
		HORIZONTAL("horizontal"),
		VERTICAL("vertical");

		companion object {
			@Throws(NoSuchElementException::class)
			fun parse(s: String): StackDirection {
				for (d in entries) {
					if (d.str == s) {
						return d
					}
				}
				throw NoSuchElementException("\"$s\" is not a valid direction for a stack op!")
			}
		}
	}

	private enum class StackAlign(private val str: String) {
		TOP("top"),
		BOT("bot"),
		LEFT("left"),
		RIGHT("right"),
		CENTER("center");

		companion object {
			@Throws(NoSuchElementException::class)
			fun parse(s: String): StackAlign {
				for (d in entries) {
					if (d.str == s) {
						return d
					}
				}
				throw NoSuchElementException("\"$s\" is not a valid alignment for a stack op!")
			}
		}
	}

	private val direction = StackDirection.parse(descr["direction"].asString)
	private val align = StackAlign.parse(descr["align"].asString)

	init {
		this.primary = ArrayList(descr.getAsJsonArray("apply_to")
			.asList()
			.stream()
			.map { x: JsonElement -> builder.getInstance(x.asString) }
			.toList())
	}

	override fun run(screenW: Int, screenH: Int) {
		when (this.direction) {
			StackDirection.HORIZONTAL -> stackWidgetsHoriz(screenW)
			StackDirection.VERTICAL -> stackWidgetsVert(screenH)
		}
	}

	fun stackWidgetsVert(screenH: Int) {
		var compHeight = -ScreenConst.WIDGET_PAD
		for (wid in primary!!) {
			compHeight += wid.height + 5
		}

		var y = when (this.align) {
			StackAlign.TOP -> ScreenConst.getScreenPad()
			StackAlign.BOT -> (screenH - compHeight) - ScreenConst.getScreenPad()
			else -> (screenH - compHeight) / 2
		}

		for (wid in primary!!) {
			wid.y = y
			y += wid.height + ScreenConst.WIDGET_PAD
		}
	}

	fun stackWidgetsHoriz(screenW: Int) {
		var compWidth = -ScreenConst.WIDGET_PAD
		for (wid in primary!!) {
			compWidth += wid.width + ScreenConst.WIDGET_PAD
		}

		var x = when (this.align) {
			StackAlign.LEFT -> ScreenConst.getScreenPad()
			StackAlign.RIGHT -> (screenW - compWidth) - ScreenConst.getScreenPad()
			else -> (screenW - compWidth) / 2
		}

		for (wid in primary!!) {
			wid.x = x
			x += wid.width + ScreenConst.WIDGET_PAD
		}
	}
}