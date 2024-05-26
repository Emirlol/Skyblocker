package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

/**
 * Represents the game board for the Boulder puzzle, managing the grid of BoulderObjects.
 * This class handles operations such as placing objects on the board, retrieving objects,
 * and generating a character representation of the game board.
 */
class BoulderBoard(val height: Int, val width: Int, target: BoulderObject) {
	private val grid = Array(height) { arrayOfNulls<BoulderObject>(width) }

	/**
	 * Constructs a BoulderBoard with the specified height, width, and target BoulderObject.
	 *
	 * @param height The height of the board.
	 * @param width  The width of the board.
	 * @param target The target BoulderObject that needs to be reached to solve the puzzle.
	 */
	init {
		val offsetX = target.x - 23
		val y = 65

		for (z in 0 until width) {
			if (z == width / 2) {
				grid[0][z] = target
			} else {
				grid[0][z] = BoulderObject(offsetX, y, z, "B")
			}
			grid[height - 1][z] = BoulderObject(24 - (3 * z), y, 6, "P")
		}
	}

	/**
	 * Retrieves the BoulderObject at the specified position on the board.
	 *
	 * @param x The x-coordinate of the position.
	 * @param y The y-coordinate of the position.
	 * @return The BoulderObject at the specified position, or null if no object is present.
	 */
	fun getObjectAtPosition(x: Int, y: Int): BoulderObject? {
		return if (isValidPosition(x, y)) grid[x][y] else null
	}

	/**
	 * Retrieves the 3D position of the BoulderObject at the specified position on the board.
	 *
	 * @param x The x-coordinate of the position.
	 * @param y The y-coordinate of the position.
	 * @return The BlockPos representing the 3D position of the BoulderObject,
	 * or null if no object is present at the specified position.
	 */
	fun getObject3DPosition(x: Int, y: Int): BlockPos? {
		val `object` = getObjectAtPosition(x, y)
		return if ((`object` != null)) `object`.get3DPosition().offset(Direction.Axis.Y, -1) else null
	}

	/**
	 * Places a BoulderObject at the specified position on the board.
	 *
	 * @param x      The x-coordinate of the position.
	 * @param y      The y-coordinate of the position.
	 * @param object The BoulderObject to place on the board.
	 */
	fun placeObject(x: Int, y: Int, `object`: BoulderObject?) {
		grid[x][y] = `object`
	}

	/**
	 * Checks whether the specified position is valid within the bounds of the game board.
	 *
	 * @param x The x-coordinate of the position to check.
	 * @param y The y-coordinate of the position to check.
	 * @return `true` if the position is valid within the bounds of the board, `false` otherwise.
	 */
	private fun isValidPosition(x: Int, y: Int): Boolean {
		return x >= 0 && y >= 0 && x < height && y < width
	}

	val boardCharArray: Array<CharArray>
		/**
		 * Generates a character array representation of the game board.
		 * Each character represents a type of BoulderObject or an empty space.
		 *
		 * @return A 2D character array representing the game board.
		 */
		get() {
			val boardCharArray = Array(height) { CharArray(width) }
			for (x in 0 until height) {
				for (y in 0 until width) {
					val boulderObject = grid[x][y]
					boardCharArray[x][y] = if ((boulderObject != null)) boulderObject.type[0] else '.'
				}
			}
			return boardCharArray
		}

	/**
	 * Prints the current state of the game board to the console.
	 * Each character represents a type of BoulderObject or an empty space.
	 */
	fun boardToString(): String {
		val sb = StringBuilder()
		for (x in 0 until height) {
			for (y in 0 until width) {
				val boulderObject = grid[x][y]
				val displayChar = if ((boulderObject != null)) boulderObject.type else "."
				sb.append(displayChar)
			}
			sb.append("\n")
		}
		return sb.toString()
	}
}
