package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder

import it.unimi.dsi.fastutil.Pair
import java.util.*
import kotlin.math.abs

/**
 * A utility class that provides methods to solve the Boulder puzzle using the A* search algorithm.
 * The BoulderSolver class is responsible for finding the shortest path from the starting position
 * to the target position by exploring possible moves and evaluating their costs.
 */
object BoulderSolver {
	/**
	 * Finds the shortest path to solve the Boulder puzzle using the A* search algorithm.
	 *
	 * @param initialStates The list of initial game states from which to start the search.
	 * @return A list of coordinates representing the shortest path to solve the puzzle,
	 * or null if no solution is found within the maximum number of iterations.
	 */
	fun aStarSolve(initialStates: List<GameState>): List<IntArray>? {
		val visited: MutableSet<GameState> = HashSet()
		val queue: PriorityQueue<Pair<GameState, MutableList<IntArray>>> = PriorityQueue<Pair<GameState, List<IntArray>>>(AStarComparator())

		for (initialState in initialStates) {
			queue.add(Pair.of(initialState, ArrayList()))
		}

		val maxIterations = 10000
		var iterations = 0

		while (!queue.isEmpty() && iterations < maxIterations) {
			val pair = queue.poll()
			val state = pair.left()
			val path = pair.right()

			if (state.isSolved) {
				return path
			}

			if (visited.contains(state)) {
				continue
			}
			visited.add(state)

			val currentCoord = intArrayOf(state.playerX, state.playerY)
			path.add(currentCoord)

			for (direction in arrayOf<IntArray>(intArrayOf(-1, 0), intArrayOf(0, -1), intArrayOf(0, 1), intArrayOf(1, 0))) {
				val newState = GameState(state.grid, state.playerX, state.playerY)
				if (newState.movePlayer(direction[0], direction[1])) {
					queue.add(Pair.of(newState, ArrayList(path)))
				}
			}
			iterations++
		}

		return null
	}

	/**
	 * A comparator used to compare game states based on their A* search cost.
	 * States with lower costs are prioritized for exploration.
	 */
	private class AStarComparator : Comparator<Pair<GameState, List<IntArray?>>> {
		/**
		 * Compares two pairs of game states and their associated paths based on their costs.
		 *
		 * @param a The first pair to compare.
		 * @param b The second pair to compare.
		 * @return A negative integer if a has a lower cost than b,
		 * a positive integer if a has a higher cost than b,
		 * or zero if both have the same cost.
		 */
		override fun compare(a: Pair<GameState, List<IntArray?>>, b: Pair<GameState, List<IntArray?>>): Int {
			val costA = a.right().size + a.left().heuristic()
			val costB = b.right().size + b.left().heuristic()
			return Integer.compare(costA, costB)
		}
	}

	/**
	 * Represents the game state for the Boulder puzzle, including the current grid configuration
	 * and the position of the theoretical player.
	 */
	class GameState(grid: Array<CharArray?>?, playerX: Int, playerY: Int) {
		val grid: Array<CharArray?>
		var playerX: Int
		var playerY: Int

		/**
		 * Constructs a new game state with the specified grid and theoretical player position.
		 *
		 * @param grid     The grid representing the Boulder puzzle configuration.
		 * @param playerX  The x-coordinate of the player's position.
		 * @param playerY  The y-coordinate of the player's position.
		 */
		init {
			this.grid = copyGrid(grid)
			this.playerX = playerX
			this.playerY = playerY
		}

		override fun equals(obj: Any?): Boolean {
			if (this === obj) return true
			if (obj == null || javaClass != obj.javaClass) return false
			val gameState = obj as GameState
			return grid.contentDeepEquals(gameState.grid) && playerX == gameState.playerX && playerY == gameState.playerY
		}

		override fun hashCode(): Int {
			var result = grid.contentDeepHashCode()
			result = 31 * result + playerX
			result = 31 * result + playerY
			return result
		}

		/**
		 * Moves the theoretical player in the specified direction and updates the game state accordingly.
		 *
		 * @param dx The change in x-coordinate (horizontal movement).
		 * @param dy The change in y-coordinate (vertical movement).
		 * @return true if the move is valid and the player is moved, false otherwise.
		 */
		fun movePlayer(dx: Int, dy: Int): Boolean {
			val newX = playerX + dx
			val newY = playerY + dy

			if (isValidPosition(newX, newY)) {
				if (grid[newX]!![newY] == 'B') {
					val nextToBoxX = newX + dx
					val nextToBoxY = newY + dy
					if (isValidPosition(nextToBoxX, nextToBoxY) && grid[nextToBoxX]!![nextToBoxY] == '.') {
						grid[newX]!![newY] = '.'
						grid[nextToBoxX]!![nextToBoxY] = 'B'
						playerX = newX
						playerY = newY
						return true
					}
				} else {
					playerX = newX
					playerY = newY
					return true
				}
			}
			return false
		}

		private fun isValidPosition(x: Int, y: Int): Boolean {
			return x >= 0 && y >= 0 && x < grid.size && y < grid[0]!!.size
		}

		val isSolved: Boolean
			/**
			 * Checks if the puzzle is solved, i.e., if the player is positioned on the target BoulderObject.
			 *
			 * @return true if the theoretical puzzle is solved, false otherwise.
			 */
			get() = grid[playerX]!![playerY] == 'T'

		/**
		 * Calculates the heuristic value for the current game state, representing the estimated
		 * distance from the player's position to the target BoulderObject.
		 *
		 * @return The heuristic value for the current game state.
		 */
		fun heuristic(): Int {
			// should be improved maybe prioritize empty path first
			for (i in grid.indices) {
				for (j in grid[0]!!.indices) {
					if (grid[i]!![j] == 'T') {
						return (abs((playerX - i).toDouble()) + abs((playerY - j).toDouble())).toInt()
					}
				}
			}
			return Int.MAX_VALUE
		}

		/**
		 * Creates a deep copy of the grid array to avoid modifying the original grid.
		 *
		 * @param original The original grid array to copy.
		 * @return A deep copy of the original grid array.
		 */
		private fun copyGrid(original: Array<CharArray?>?): Array<CharArray?> {
			val copy = arrayOfNulls<CharArray>(original!!.size)
			for (i in original.indices) {
				copy[i] = original[i]!!.clone()
			}
			return copy
		}
	}
}
