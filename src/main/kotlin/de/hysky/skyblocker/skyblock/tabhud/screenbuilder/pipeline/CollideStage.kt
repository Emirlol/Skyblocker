package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst
import de.hysky.skyblocker.skyblock.tabhud.widget.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class CollideStage(builder: ScreenBuilder, descr: JsonObject) : PipelineStage() {
	private enum class CollideDirection(private val str: String) {
		LEFT("left"),
		RIGHT("right"),
		TOP("top"),
		BOT("bot");

		companion object {
			@Throws(NoSuchElementException::class)
			fun parse(s: String): CollideDirection {
				for (d in entries) {
					if (d.str == s) {
						return d
					}
				}
				throw NoSuchElementException("\"$s\" is not a valid direction for a collide op!")
			}
		}
	}

	private val direction = CollideDirection.parse(descr["direction"].asString)

	init {
		this.primary = ArrayList(descr.getAsJsonArray("widgets")
			.asList()
			.stream()
			.map { x: JsonElement -> builder.getInstance(x.asString) }
			.toList())
		this.secondary = ArrayList(descr.getAsJsonArray("colliders")
			.asList()
			.stream()
			.map { x: JsonElement -> builder.getInstance(x.asString) }
			.toList())
	}

	override fun run(screenW: Int, screenH: Int) {
		when (this.direction) {
			CollideDirection.LEFT -> primary!!.forEach(Consumer { w: Widget -> collideAgainstL(screenW, w) })
			CollideDirection.RIGHT -> primary!!.forEach(Consumer { w: Widget -> collideAgainstR(screenW, w) })
			CollideDirection.TOP -> primary!!.forEach(Consumer { w: Widget -> collideAgainstT(screenH, w) })
			CollideDirection.BOT -> primary!!.forEach(Consumer { w: Widget -> collideAgainstB(screenH, w) })
		}
	}

	fun collideAgainstL(screenW: Int, w: Widget) {
		val yMin = w.y
		val yMax = w.y + w.height

		var xCor = screenW

		for (other in secondary!!) {
			if (other.y + other.height + ScreenConst.WIDGET_PAD < yMin) {
				// too high, next one
				continue
			}

			if (other.y - ScreenConst.WIDGET_PAD > yMax) {
				// too low, next
				continue
			}

			val xPos = other.x - ScreenConst.WIDGET_PAD - w.width
			xCor = min(xCor.toDouble(), xPos.toDouble()).toInt()
		}
		w.x = xCor
	}

	fun collideAgainstR(screenW: Int, w: Widget) {
		val yMin = w.y
		val yMax = w.y + w.height

		var xCor = 0

		for (other in secondary!!) {
			if (other.y + other.height + ScreenConst.WIDGET_PAD < yMin) {
				// too high, next one
				continue
			}

			if (other.y - ScreenConst.WIDGET_PAD > yMax) {
				// too low, next
				continue
			}

			val xPos = other.x + other.width + ScreenConst.WIDGET_PAD
			xCor = max(xCor.toDouble(), xPos.toDouble()).toInt()
		}
		w.x = xCor
	}

	fun collideAgainstT(screenH: Int, w: Widget) {
		val xMin = w.x
		val xMax = w.x + w.width

		var yCor = screenH

		for (other in secondary!!) {
			if (other.x + other.width + ScreenConst.WIDGET_PAD < xMin) {
				// too far left, next one
				continue
			}

			if (other.x - ScreenConst.WIDGET_PAD > xMax) {
				// too far right, next
				continue
			}

			val yPos = other.y - ScreenConst.WIDGET_PAD - w.height
			yCor = min(yCor.toDouble(), yPos.toDouble()).toInt()
		}
		w.y = yCor
	}

	fun collideAgainstB(screenH: Int, w: Widget) {
		val xMin = w.x
		val xMax = w.x + w.width

		var yCor = 0

		for (other in secondary!!) {
			if (other.x + other.width + ScreenConst.WIDGET_PAD < xMin) {
				// too far left, next one
				continue
			}

			if (other.x - ScreenConst.WIDGET_PAD > xMax) {
				// too far right, next
				continue
			}

			val yPos = other.y + other.height + ScreenConst.WIDGET_PAD
			yCor = max(yCor.toDouble(), yPos.toDouble()).toInt()
		}
		w.y = yCor
	}
}
