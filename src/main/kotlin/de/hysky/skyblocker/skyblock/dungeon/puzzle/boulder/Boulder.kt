package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle
import de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder.BoulderSolver.GameState
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import de.hysky.skyblocker.utils.render.title.Title
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class Boulder private constructor() : DungeonPuzzle("boulder", "boxes-room") {
	override fun tick(client: MinecraftClient?) {
		if (!shouldSolve() || !SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveBoulder || client!!.world == null || !DungeonManager.isCurrentRoomMatched()) {
			return
		}

		val room = DungeonManager.getCurrentRoom()

		val chestPos = BlockPos(15, BASE_Y, 29)
		val start = BlockPos(25, BASE_Y, 25)
		val end = BlockPos(5, BASE_Y, 8)
		// Create a target BoulderObject for the puzzle
		val target = BoulderObject(chestPos.x, chestPos.x, chestPos.z, "T")
		// Create a BoulderBoard representing the puzzle's grid
		val board = BoulderBoard(8, 7, target)

		// Populate the BoulderBoard grid with BoulderObjects based on block types in the room
		var column = 1
		for (z in start.z downTo end.z + 1) {
			var row = 0
			for (x in start.x downTo end.x + 1) {
				if (abs((start.x - x).toDouble()) % 3 == 1 && abs((start.z - z).toDouble()) % 3 == 1) {
					val blockType = getBlockType(client!!.world, x, BASE_Y, z)
					board.placeObject(column, row, BoulderObject(x, BASE_Y, z, blockType))
					row++
				}
			}
			if (row == board.width) {
				column++
			}
		}

		// Generate initial game states for the A* solver
		val boardArray = board.boardCharArray
		val initialStates = Arrays.asList(
			GameState(boardArray, board.height - 1, 0),
			GameState(boardArray, board.height - 1, 1),
			GameState(boardArray, board.height - 1, 2),
			GameState(boardArray, board.height - 1, 3),
			GameState(boardArray, board.height - 1, 4),
			GameState(boardArray, board.height - 1, 5),
			GameState(boardArray, board.height - 1, 6)
		)

		// Solve the puzzle using the A* algorithm
		val solution = BoulderSolver.aStarSolve(initialStates)

		if (solution != null) {
			linePoints = arrayOfNulls(solution.size)
			var index = 0
			// Convert solution coordinates to Vec3d points for rendering
			for (coord in solution) {
				val x = coord!![0]
				val y = coord[1]
				// Convert relative coordinates to actual coordinates
				linePoints!![index++] = Vec3d.ofCenter(room.relativeToActual(board.getObject3DPosition(x, y)))
			}

			var button: BlockPos? = null
			if (linePoints != null && linePoints!!.size > 0) {
				// Check for buttons along the path of the solution
				for (i in 0 until linePoints!!.size - 1) {
					val point1 = linePoints!![i]
					val point2 = linePoints!![i + 1]
					button = checkForButtonBlocksOnLine(client!!.world, point1, point2)
					if (button != null) {
						// If a button is found, calculate its bounding box
						boundingBox = getBlockBoundingBox(client.world, button)
						break
					}
				}
				if (button == null) {
					// If no button is found along the path the puzzle is solved; reset the puzzle
					reset()
				}
			}
		} else {
			// If no solution is found, display a title message and reset the puzzle
			val title = Title("skyblocker.dungeons.puzzle.boulder.noSolution", Formatting.GREEN)
			displayInTitleContainerAndPlaySound(title, 15)
			reset()
		}
	}

	override fun render(context: WorldRenderContext?) {
		if (!shouldSolve() || !SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveBoulder || !DungeonManager.isCurrentRoomMatched()) return
		val alpha = 1.0f
		val lineWidth = 5.0f

		if (linePoints != null && linePoints!!.size > 0) {
			for (i in 0 until linePoints!!.size - 1) {
				val startPoint = linePoints!![i]
				val endPoint = linePoints!![i + 1]
				renderLinesFromPoints(context!!, arrayOf<Vec3d?>(startPoint, endPoint), ORANGE_COLOR_COMPONENTS, alpha, lineWidth, true)
			}
			if (boundingBox != null) {
				renderOutline(context!!, boundingBox, RED_COLOR_COMPONENTS, 5f, false)
			}
		}
	}

	override fun reset() {
		super.reset()
		linePoints = null
		boundingBox = null
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Boulder::class.java.name)
		private val INSTANCE = Boulder()
		private val RED_COLOR_COMPONENTS: FloatArray = DyeColor.RED.colorComponents
		private val ORANGE_COLOR_COMPONENTS: FloatArray = DyeColor.ORANGE.colorComponents
		private const val BASE_Y = 65
		var linePoints: Array<Vec3d?>?
		var boundingBox: Box? = null

		fun init() {
		}

		/**
		 * Retrieves the type of block at the specified position in the world.
		 * If the block is Birch or Jungle plank, it will return "B"; otherwise, it will return ".".
		 *
		 * @param world The client world.
		 * @param x     The x-coordinate of the block.
		 * @param y     The y-coordinate of the block.
		 * @param z     The z-coordinate of the block.
		 * @return The type of block at the specified position.
		 */
		fun getBlockType(world: ClientWorld?, x: Int, y: Int, z: Int): String {
			val block = world!!.getBlockState(DungeonManager.getCurrentRoom().relativeToActual(BlockPos(x, y, z))).block
			return if ((block === Blocks.BIRCH_PLANKS || block === Blocks.JUNGLE_PLANKS)) "B" else "."
		}

		/**
		 * Checks for blocks along the line between two points in the world.
		 * Returns the position of a block if it found a button on the line, if any.
		 *
		 * @param world   The client world.
		 * @param point1  The starting point of the line.
		 * @param point2  The ending point of the line.
		 * @return The position of the block found on the line, or null if no block is found.
		 */
		private fun checkForButtonBlocksOnLine(world: ClientWorld?, point1: Vec3d?, point2: Vec3d?): BlockPos? {
			val x1 = point1!!.getX()
			val y1 = point1.getY() + 1
			val z1 = point1.getZ()

			val x2 = point2!!.getX()
			val y2 = point2.getY() + 1
			val z2 = point2.getZ()

			val steps = max(abs(x2 - x1), max(abs(y2 - y1), abs(z2 - z1))).toInt()

			val xStep = (x2 - x1) / steps
			val yStep = (y2 - y1) / steps
			val zStep = (z2 - z1) / steps


			for (step in 0..steps) {
				val currentX = x1 + step * xStep
				val currentY = y1 + step * yStep
				val currentZ = z1 + step * zStep

				val blockPos = BlockPos.ofFloored(currentX, currentY, currentZ)
				val block = world!!.getBlockState(blockPos).block

				if (block === Blocks.STONE_BUTTON) {
					return blockPos
				}
			}
			return null
		}

		/**
		 * Retrieves the bounding box of a block in the world.
		 *
		 * @param world The client world.
		 * @param pos   The position of the block.
		 * @return The bounding box of the block.
		 */
		fun getBlockBoundingBox(world: BlockView?, pos: BlockPos?): Box {
			val blockState = world!!.getBlockState(pos)
			return blockState.getOutlineShape(world, pos).boundingBox.offset(pos)
		}
	}
}
