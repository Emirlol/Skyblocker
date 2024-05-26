package de.hysky.skyblocker.skyblock.fancybars

import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.AnchorHitboxProvider
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.AnchorPositionProvider
import net.minecraft.client.gui.ScreenPos
import net.minecraft.client.gui.ScreenRect
import java.util.*

class BarPositioner {
	private val map: MutableMap<BarAnchor, LinkedList<LinkedList<StatusBar?>>> = HashMap(BarAnchor.entries.size)

	init {
		for (value in BarAnchor.entries) {
			map[value] = LinkedList()
		}
	}


	fun getRowCount(barAnchor: BarAnchor): Int {
		return map[barAnchor]!!.size
	}

	/**
	 * Adds a row to the end of an anchor
	 *
	 * @param barAnchor the anchor
	 */
	fun addRow(barAnchor: BarAnchor) {
		map[barAnchor]!!.add(LinkedList())
	}

	/**
	 * Adds a row at the specified index
	 *
	 * @param barAnchor the anchor
	 * @param row       row index
	 */
	fun addRow(barAnchor: BarAnchor, row: Int) {
		map[barAnchor]!!.add(row, LinkedList())
	}

	/**
	 * adds a bar to the end of a row
	 *
	 * @param barAnchor the anchor
	 * @param row       the row
	 * @param bar       the bar to add
	 */
	fun addBar(barAnchor: BarAnchor, row: Int, bar: StatusBar) {
		val statusBars = map[barAnchor]!![row]
		statusBars.add(bar)
		bar.gridY = row
		bar.gridX = statusBars.lastIndexOf(bar) // optimization baby, start with the end!
		bar.anchor = barAnchor
	}

	/**
	 * adds a bar to the specified x in a row
	 *
	 * @param barAnchor the anchor
	 * @param row       the row
	 * @param x         the index in the row
	 * @param bar       the bar to add
	 */
	fun addBar(barAnchor: BarAnchor, row: Int, x: Int, bar: StatusBar) {
		val statusBars = map[barAnchor]!![row]
		statusBars.add(x, bar)
		bar.gridY = row
		bar.gridX = statusBars.indexOf(bar)
		bar.anchor = barAnchor
	}

	/**
	 * removes the specified bar at x on the row. If it's row is empty after being removed, the row will be auto removed
	 *
	 * @param barAnchor the anchor
	 * @param row       dah row
	 * @param x         dah x
	 */
	fun removeBar(barAnchor: BarAnchor, row: Int, x: Int) {
		val statusBars = map[barAnchor]!![row]
		val remove = statusBars.removeAt(x)
		remove!!.anchor = null
		for (i in x until statusBars.size) {
			statusBars[i]!!.gridX--
		}
		if (statusBars.isEmpty()) removeRow(barAnchor, row)
	}

	/**
	 * removes the specified bar on the row. If it's row is empty after being removed, the row will be auto removed
	 *
	 * @param barAnchor the anchor
	 * @param row       dah row
	 * @param bar       dah bar
	 */
	fun removeBar(barAnchor: BarAnchor, row: Int, bar: StatusBar?) {
		val barRow = map[barAnchor]!![row]
		val x = barRow.indexOf(bar)
		if (x < 0) return  // probably a bad idea


		barRow.remove(bar)
		bar!!.anchor = null
		for (i in x until barRow.size) {
			barRow[i]!!.gridX--
		}
		if (barRow.isEmpty()) removeRow(barAnchor, row)
	}

	/**
	 * row must be empty
	 *
	 * @param barAnchor the anchor
	 * @param row       the row to remove
	 */
	fun removeRow(barAnchor: BarAnchor, row: Int) {
		val barRow = map[barAnchor]!![row]
		check(barRow.isEmpty()) { "Can't remove a non-empty row ($barAnchor,$row)" }
		map[barAnchor]!!.removeAt(row)
		for (i in row until map[barAnchor]!!.size) {
			for (statusBar in map[barAnchor]!![i]) {
				statusBar!!.gridY--
			}
		}
	}


	fun getRow(barAnchor: BarAnchor, row: Int): LinkedList<StatusBar?> {
		return map[barAnchor]!![row]
	}

	fun getBar(barAnchor: BarAnchor, row: Int, x: Int): StatusBar? {
		return map[barAnchor]!![row][x]
	}

	fun hasNeighbor(barAnchor: BarAnchor, row: Int, x: Int, right: Boolean): Boolean {
		val statusBars = map[barAnchor]!![row]
		return if (barAnchor.isRight) {
			right && x < statusBars.size - 1 || (!right && x > 0)
		} else {
			right && x > 0 || (!right && x < statusBars.size - 1)
		}
	}


	enum class BarAnchor
	/**
	 * @param up               whether the rows stack towards the top of the screen from the anchor (false is bottom)
	 * @param right            whether the bars are line up towards the right of the screen from the anchor (false is left)
	 * @param positionProvider provides the position of the anchor for a give screen size
	 * @param sizeRule         the rule the bars should follow. See [SizeRule]
	 * @param hitboxProvider   provides the hitbox for when the anchor has no bars for the config screen
	 */ @JvmOverloads constructor(
		/**
		 * whether the rows stack towards the top of the screen from the anchor (false is bottom)
		 *
		 * @return true if towards the top, false otherwise
		 */
		val isUp: Boolean,
		/**
		 * whether the bars are line up towards the right of the screen from the anchor (false is left)
		 *
		 * @return true if towards the right, false otherwise
		 */
		val isRight: Boolean, private val positionProvider: AnchorPositionProvider, val sizeRule: SizeRule, private val hitboxProvider: AnchorHitboxProvider = AnchorHitboxProvider { anchorPosition: ScreenPos -> ScreenRect(anchorPosition.x() - (if (isRight) 0 else 20), anchorPosition.y() - (if (isUp) 20 else 0), 20, 20) }
	) {
		HOTBAR_LEFT(
			true, false,
			AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(scaledWidth / 2 - 91 - 2, scaledHeight - 5) },
			SizeRule.freeSize(25, 2, 6)
		),

		HOTBAR_RIGHT(
			true, true,
			AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(scaledWidth / 2 + 91 + 2, scaledHeight - 5) },
			SizeRule.freeSize(25, 2, 6)
		),

		HOTBAR_TOP(true, true,
			AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(scaledWidth / 2 - 91, scaledHeight - (if (FancyStatusBars.Companion.isExperienceFancyBarVisible()) 23 else 35)) },
			SizeRule.targetSize(12, 182, 2),
			AnchorHitboxProvider { anchorPosition: ScreenPos -> ScreenRect(anchorPosition.x(), anchorPosition.y() - 20, 182, 20) }),

		SCREEN_TOP_LEFT(
			false, true,
			(AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(5, 5) }),
			SizeRule.freeSize(25, 2, 6)
		),
		SCREEN_TOP_RIGHT(
			false, false,
			(AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(scaledWidth - 5, 5) }),
			SizeRule.freeSize(25, 2, 6)
		),
		SCREEN_BOTTOM_LEFT(
			true, true,
			(AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(5, scaledHeight - 5) }),
			SizeRule.freeSize(25, 2, 6)
		),
		SCREEN_BOTTOM_RIGHT(
			true, false,
			(AnchorPositionProvider { scaledWidth: Int, scaledHeight: Int -> ScreenPos(scaledWidth - 5, scaledHeight - 5) }),
			SizeRule.freeSize(25, 2, 6)
		);

		fun getAnchorPosition(scaledWidth: Int, scaledHeight: Int): ScreenPos {
			return positionProvider.getPosition(scaledWidth, scaledHeight)
		}

		fun getAnchorHitbox(anchorPosition: ScreenPos?): ScreenRect {
			return hitboxProvider.getHitbox(anchorPosition)
		}

		companion object {
			private val cached: List<BarAnchor> = java.util.List.of(*entries.toTypedArray())

			/**
			 * cached version of [BarAnchor.values]
			 *
			 * @return the list of anchors
			 */
			fun allAnchors(): List<BarAnchor> {
				return cached
			}
		}
	}

	/**
	 * The rules the bars on an anchor should follow
	 *
	 * @param isTargetSize whether the bars went to fit to a target width
	 * @param targetSize   the size of all the bars on a row should add up to this (target size)
	 * @param totalWidth   the total width taken by all the bars on the row (target size)
	 * @param widthPerSize the width of each size "unit" (free size)
	 * @param minSize      the minimum (free and target size)
	 * @param maxSize      the maximum (free and target size, THIS SHOULD BE THE SAME AS `targetSize` FOR `isTargetSize = true`)
	 */
	@JvmRecord
	data class SizeRule(val isTargetSize: Boolean, val targetSize: Int, val totalWidth: Int, val widthPerSize: Int, val minSize: Int, val maxSize: Int) {
		companion object {
			fun freeSize(widthPerSize: Int, minSize: Int, maxSize: Int): SizeRule {
				return SizeRule(false, -1, -1, widthPerSize, minSize, maxSize)
			}

			fun targetSize(targetSize: Int, totalWidth: Int, minSize: Int): SizeRule {
				return SizeRule(true, targetSize, totalWidth, -1, minSize, targetSize)
			}
		}
	}

	/**
	 * A record representing a snapshot of a bar's position
	 *
	 * @param barAnchor
	 * @param x
	 * @param y         the row
	 */
	@JvmRecord
	data class BarLocation(val barAnchor: BarAnchor?, val x: Int, val y: Int) {
		fun equals(barAnchor: BarAnchor?, x: Int, y: Int): Boolean {
			return (x == this.x) && y == this.y && barAnchor == this.barAnchor
		}

		companion object {
			val NULL: BarLocation = BarLocation(null, -1, -1)

			fun of(bar: StatusBar): BarLocation {
				return BarLocation(bar.anchor, bar.gridX, bar.gridY)
			}
		}
	}

	/**
	 * provides the position of the anchor for a give screen size
	 */
	internal fun interface AnchorPositionProvider {
		fun getPosition(scaledWidth: Int, scaledHeight: Int): ScreenPos
	}

	internal fun interface AnchorHitboxProvider {
		/**
		 * The hitbox, as in how large the area of "snapping" is if there are no bars on this anchor
		 *
		 * @param anchorPosition the position of the anchor
		 * @return the rectangle that represents the hitbox
		 */
		fun getHitbox(anchorPosition: ScreenPos?): ScreenRect
	}
}
