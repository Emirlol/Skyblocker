package de.hysky.skyblocker.skyblock.dungeon.puzzle

import com.google.common.primitives.Booleans
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.debug.Debug
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.*
import java.util.concurrent.CompletableFuture

class IceFill private constructor() : DungeonPuzzle("ice-fill", "ice-path") {
	private var solve: CompletableFuture<Void>? = null
	private val iceFillBoards = arrayOf(Array(3) { BooleanArray(3) }, Array(5) { BooleanArray(5) }, Array(7) { BooleanArray(7) })
	private val iceFillPaths: List<MutableList<Vector2ic>> = java.util.List.of<MutableList<Vector2ic>>(ArrayList(), ArrayList(), ArrayList())

	override fun tick(client: MinecraftClient?) {
		if (!SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveIceFill || client!!.world == null || !DungeonManager.isCurrentRoomMatched() || solve != null && !solve!!.isDone) {
			return
		}
		val room = DungeonManager.getCurrentRoom()

		solve = CompletableFuture.runAsync {
			val pos = BlockPos.Mutable()
			for (i in 0..2) {
				if (updateBoard(client!!.world, room, iceFillBoards[i], pos.set(BOARD_ORIGINS[i]))) {
					solve(iceFillBoards[i], iceFillPaths[i])
				}
			}
		}
	}

	private fun updateBoard(world: World?, room: Room, iceFillBoard: Array<BooleanArray>, pos: BlockPos.Mutable): Boolean {
		var boardChanged = false
		var row = 0
		while (row < iceFillBoard.size) {
			var col = 0
			while (col < iceFillBoard[row].size) {
				val actualPos = room.relativeToActual(pos)
				val isBlock = !world!!.getBlockState(actualPos).isAir
				if (iceFillBoard[row][col] != isBlock) {
					iceFillBoard[row][col] = isBlock
					boardChanged = true
				}
				pos.move(Direction.WEST)
				col++
			}
			pos.move(iceFillBoard[row].size, 0, -1)
			row++
		}
		return boardChanged
	}

	fun solve(iceFillBoard: Array<BooleanArray>, iceFillPath: MutableList<Vector2ic>) {
		val start: Vector2ic = Vector2i(iceFillBoard.size - 1, iceFillBoard[0].size / 2)
		val count = iceFillBoard.size * iceFillBoard[0].size - Arrays.stream(iceFillBoard).mapToInt { values: BooleanArray? -> Booleans.countTrue(values) }.sum()

		val newPath = solveDfs(iceFillBoard, count - 1, ArrayList(java.util.List.of(start)), HashSet(java.util.List.of(start)))
		if (newPath != null) {
			iceFillPath.clear()
			iceFillPath.addAll(newPath)
		}
	}

	private fun solveDfs(iceFillBoard: Array<BooleanArray>, count: Int, path: MutableList<Vector2ic>, visited: MutableSet<Vector2ic>): List<Vector2ic>? {
		val pos = path[path.size - 1]
		if (count == 0) {
			return if (pos.x() == 0 && pos.y() == iceFillBoard[0].size / 2) {
				path
			} else {
				null
			}
		}

		val newPosArray = arrayOf<Vector2ic>(pos.add(1, 0, Vector2i()), pos.add(-1, 0, Vector2i()), pos.add(0, 1, Vector2i()), pos.add(0, -1, Vector2i()))
		for (newPos in newPosArray) {
			if (newPos.x() >= 0 && newPos.x() < iceFillBoard.size && newPos.y() >= 0 && newPos.y() < iceFillBoard[0].size && !iceFillBoard[newPos.x()][newPos.y()] && !visited.contains(newPos)) {
				path.add(newPos)
				visited.add(newPos)
				val newPath = solveDfs(iceFillBoard, count - 1, path, visited)
				if (newPath != null) {
					return newPath
				}
				path.removeAt(path.size - 1)
				visited.remove(newPos)
			}
		}

		return null
	}

	override fun render(context: WorldRenderContext?) {
		if (!SkyblockerConfigManager.config.dungeons.puzzleSolvers.solveIceFill || !DungeonManager.isCurrentRoomMatched()) {
			return
		}
		val room = DungeonManager.getCurrentRoom()
		for (i in 0..2) {
			renderPath(context, room, iceFillPaths[i], BOARD_ORIGINS[i])
		}
	}

	private fun renderPath(context: WorldRenderContext?, room: Room, iceFillPath: List<Vector2ic>, originPos: BlockPos) {
		val pos = BlockPos.Mutable()
		for (i in 0 until iceFillPath.size - 1) {
			val start = Vec3d.ofCenter(room.relativeToActual(pos.set(originPos).move(-iceFillPath[i].y(), 0, -iceFillPath[i].x())))
			val end = Vec3d.ofCenter(room.relativeToActual(pos.set(originPos).move(-iceFillPath[i + 1].y(), 0, -iceFillPath[i + 1].x())))
			renderLinesFromPoints(context!!, arrayOf(start, end), RED_COLOR_COMPONENTS, 1f, 5f, true)
		}
	}

	companion object {
		val INSTANCE: IceFill = IceFill()
		private val RED_COLOR_COMPONENTS: FloatArray = DyeColor.RED.colorComponents
		private val BOARD_ORIGINS = arrayOf(
			BlockPos(16, 70, 9),
			BlockPos(17, 71, 16),
			BlockPos(18, 72, 25)
		)

		fun init() {
			if (Debug.debugEnabled()) {
				ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
					dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("dungeons").then(ClientCommandManager.literal("puzzle").then(ClientCommandManager.literal(INSTANCE.puzzleName)
						.then(ClientCommandManager.literal("printBoard1").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(boardToString(INSTANCE.iceFillBoards[0])))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printBoard2").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(boardToString(INSTANCE.iceFillBoards[1])))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printBoard3").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(boardToString(INSTANCE.iceFillBoards[2])))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printPath1").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(INSTANCE.iceFillPaths.first.toString()))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printPath2").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(INSTANCE.iceFillPaths[1].toString()))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printPath3").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.append(INSTANCE.iceFillPaths[2].toString()))
							Command.SINGLE_SUCCESS
						})
					)
					)
					)
					)
				})
			}
		}

		private fun boardToString(iceFillBoard: Array<BooleanArray>): String {
			val sb = StringBuilder()
			for (row in iceFillBoard) {
				sb.append("\n")
				for (cell in row) {
					sb.append(if (cell) '#' else '.')
				}
			}
			return sb.toString()
		}
	}
}
