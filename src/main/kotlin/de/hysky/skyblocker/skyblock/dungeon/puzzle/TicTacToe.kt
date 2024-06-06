package de.hysky.skyblocker.skyblock.dungeon.puzzle

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import de.hysky.skyblocker.utils.tictactoe.TicTacToeUtils.getBestMove
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Thanks to Danker for a reference implementation!
 */
class TicTacToe private constructor() : DungeonPuzzle("tic-tac-toe", "tic-tac-toe-1") {
	override fun tick(client: MinecraftClient?) {
		if (!shouldSolve()) {
			return
		}

		nextBestMoveToMake = null

		if (client!!.world == null || client.player == null || !isInDungeons) return

		//Search within 21 blocks for item frames that contain maps
		val searchBox = Box(client.player!!.x - 21, client.player!!.y - 21, client.player!!.z - 21, client.player!!.x + 21, client.player!!.y + 21, client.player!!.z + 21)
		val itemFramesThatHoldMaps = client.world!!.getEntitiesByClass(ItemFrameEntity::class.java, searchBox) { obj: ItemFrameEntity -> obj.containsMap() }

		try {
			//Only attempt to solve if the puzzle wasn't just completed and if its the player's turn
			//The low bit will always be set to 1 on odd numbers
			if (itemFramesThatHoldMaps.size != 9 && (itemFramesThatHoldMaps.size and 1) == 1) {
				val board = Array(3) { CharArray(3) }

				for (itemFrame in itemFramesThatHoldMaps) {
					val mapState = client.world!!.getMapState(itemFrame.mapId) <<?: continue

					//Surely if we pass shouldSolve then the room should be matched right
					val relative = DungeonManager.getCurrentRoom().actualToRelative(itemFrame.blockPos)

					//Determine the row -- 72 = top, 71 = middle, 70 = bottom
					val y = relative.y
					val row = when (y) {
						72 -> 0
						71 -> 1
						70 -> 2
						else -> -1
					}

					//Determine the column - 17 = first, 16 = second, 15 = third
					val z = relative.z
					val column = when (z) {
						17 -> 0
						16 -> 1
						15 -> 2
						else -> -1
					}

					if (row == -1 || column == -1) continue

					//Get the color of the middle pixel of the map which determines whether its X or O
					val middleColor = mapState.colors[8256].toInt() and 0xFF

					if (middleColor == 114) {
						board[row][column] = 'X'
					} else if (middleColor == 33) {
						board[row][column] = 'O'
					}
				}

				val bestMove = getBestMove(board)

				val nextX = 8.0
				val nextY = (72 - bestMove.row).toDouble()
				val nextZ = (17 - bestMove.column).toDouble()

				val nextPos = DungeonManager.getCurrentRoom().relativeToActual(BlockPos.ofFloored(nextX, nextY, nextZ))
				nextBestMoveToMake = Box(nextPos)
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while determining a tic tac toe solution!", e)
		}
	}

	override fun render(context: WorldRenderContext?) {
		try {
			if (SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveTicTacToe && nextBestMoveToMake != null) {
				renderOutline(context!!, nextBestMoveToMake, RED_COLOR_COMPONENTS, 5f, false)
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while rendering the tic tac toe solution!", e)
		}
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(TicTacToe::class.java)
		private val RED_COLOR_COMPONENTS = floatArrayOf(1.0f, 0.0f, 0.0f)

		@Suppress("unused")
		private val INSTANCE = TicTacToe()
		private var nextBestMoveToMake: Box? = null

		fun init() {
		}
	}
}
