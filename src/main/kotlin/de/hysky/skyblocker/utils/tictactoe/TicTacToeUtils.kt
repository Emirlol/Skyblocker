package de.hysky.skyblocker.utils.tictactoe

import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min

object TicTacToeUtils {
	@JvmStatic
	fun getBestMove(board: Array<CharArray>): BoardIndex {
		val moves = Object2IntOpenHashMap<BoardIndex>()

		for (row in board.indices) {
			for (column in board[row].indices) {
				// Simulate the move as O if the square is empty to determine a solution
				if (board[row][column] != '\u0000') continue
				board[row][column] = 'O'
				val score = alphabeta(board, Int.MIN_VALUE, Int.MAX_VALUE, 0, false)
				board[row][column] = '\u0000'

				moves.put(BoardIndex(row, column), score)
			}
		}

		return Collections.max<Object2IntMap.Entry<BoardIndex>>(moves.object2IntEntrySet(), Comparator.comparingInt<Object2IntMap.Entry<BoardIndex>> { it.unimi.dsi.fastutil.objects.Object2IntMap.Entry.getIntValue() }).key
	}

	private fun hasMovesAvailable(board: Array<CharArray>): Boolean {
		return Arrays.stream(board).flatMap { row: CharArray -> Stream.of(row[0], row[1], row[2]) }.anyMatch { c: Char -> c == '\u0000' }
	}

	private fun getScore(board: Array<CharArray>): Int {
		// Check if X or O has won horizontally
		for (row in 0..2) {
			if (board[row][0] == board[row][1] && board[row][0] == board[row][2]) {
				when (board[row][0]) {
					'X' -> return -10
					'O' -> return 10
				}
			}
		}

		// Check if X or O has won vertically
		for (column in 0..2) {
			if (board[0][column] == board[1][column] && board[0][column] == board[2][column]) {
				when (board[0][column]) {
					'X' -> return -10
					'O' -> return 10
				}
			}
		}

		// Check if X or O has won diagonally
		// Top left to bottom right
		if (board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
			when (board[0][0]) {
				'X' -> return -10
				'O' -> return 10
			}
		}

		// Top right to bottom left
		if (board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
			when (board[0][2]) {
				'X' -> return -10
				'O' -> return 10
			}
		}

		return 0
	}

	private fun alphabeta(board: Array<CharArray>, alpha: Int, beta: Int, depth: Int, maximizePlayer: Boolean): Int {
		var alpha = alpha
		var beta = beta
		val score = getScore(board)

		if (score == 10 || score == -10) return score
		if (!hasMovesAvailable(board)) return 0

		if (maximizePlayer) {
			var bestScore = Int.MIN_VALUE

			for (row in 0..2) {
				for (column in 0..2) {
					if (board[row][column] == '\u0000') {
						board[row][column] = 'O'
						bestScore = max(bestScore.toDouble(), alphabeta(board, alpha, beta, depth + 1, false).toDouble()).toInt()
						board[row][column] = '\u0000'
						alpha = max(alpha.toDouble(), bestScore.toDouble()).toInt()

						//Is this correct? Well the algorithm seems to solve it so I will assume it is
						if (beta <= alpha) break // Pruning
					}
				}
			}

			return bestScore - depth
		} else {
			var bestScore = Int.MAX_VALUE

			for (row in 0..2) {
				for (column in 0..2) {
					if (board[row][column] == '\u0000') {
						board[row][column] = 'X'
						bestScore = min(bestScore.toDouble(), alphabeta(board, alpha, beta, depth + 1, true).toDouble()).toInt()
						board[row][column] = '\u0000'
						beta = min(beta.toDouble(), bestScore.toDouble()).toInt()

						if (beta <= alpha) break // Pruning
					}
				}
			}

			return bestScore + depth
		}
	}
}