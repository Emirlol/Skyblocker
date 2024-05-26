package de.hysky.skyblocker.skyblock.dungeon.puzzle

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.debug.Debug
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.mob.SilverfishEntity
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.joml.Vector2i
import org.joml.Vector2ic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class Silverfish private constructor() : DungeonPuzzle("silverfish", "ice-silverfish-room") {
	val silverfishBoard: Array<BooleanArray> = Array(17) { BooleanArray(17) }
	var silverfishPos: Vector2ic? = null
	val silverfishPath: MutableList<Vector2ic> = ArrayList()

	override fun tick(client: MinecraftClient?) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveSilverfish || client!!.world == null || !DungeonManager.isCurrentRoomMatched()) {
			return
		}
		val room = DungeonManager.getCurrentRoom()

		var boardChanged = false
		val pos = BlockPos.Mutable(23, 67, 24)
		var row = 0
		while (row < silverfishBoard.size) {
			var col = 0
			while (col < silverfishBoard[row].size) {
				val isBlock = !client!!.world!!.getBlockState(room.relativeToActual(pos)).isAir
				if (silverfishBoard[row][col] != isBlock) {
					silverfishBoard[row][col] = isBlock
					boardChanged = true
				}
				pos.move(Direction.WEST)
				col++
			}
			pos.move(silverfishBoard[row].size, 0, -1)
			row++
		}

		val entities = client!!.world!!.getEntitiesByClass(SilverfishEntity::class.java, Box.of(Vec3d.ofCenter(room.relativeToActual(BlockPos(15, 66, 16))), 16.0, 16.0, 16.0)) { silverfishEntity: SilverfishEntity? -> true }
		if (entities.isEmpty()) {
			return
		}
		val newSilverfishBlockPos = room.actualToRelative(entities.first.blockPos)
		val newSilverfishPos: Vector2ic = Vector2i(24 - newSilverfishBlockPos.z, 23 - newSilverfishBlockPos.x)
		if (newSilverfishPos.x() < 0 || newSilverfishPos.x() >= 17 || newSilverfishPos.y() < 0 || newSilverfishPos.y() >= 17) {
			return
		}
		val silverfishChanged = newSilverfishPos != silverfishPos
		if (silverfishChanged) {
			silverfishPos = newSilverfishPos
		}
		if (silverfishChanged || boardChanged) {
			solve()
		}
	}

	fun solve() {
		if (silverfishPos == null) {
			return
		}
		val visited: MutableSet<Vector2ic> = HashSet()
		val queue: Queue<List<Vector2ic>> = ArrayDeque()
		queue.add(java.util.List.of(silverfishPos))
		visited.add(silverfishPos!!)
		while (!queue.isEmpty()) {
			val path = queue.poll()
			val pos = path[path.size - 1]
			if (pos.x() == 0 && pos.y() >= 7 && pos.y() <= 9) {
				silverfishPath.clear()
				silverfishPath.addAll(path)
				return
			}

			var posMutable = Vector2i(pos)
			while (posMutable.x() < 17 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(1, 0)
			}
			posMutable.add(-1, 0)
			addQueue(visited, queue, path, posMutable)

			posMutable = Vector2i(pos)
			while (posMutable.x() >= 0 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(-1, 0)
			}
			posMutable.add(1, 0)
			addQueue(visited, queue, path, posMutable)

			posMutable = Vector2i(pos)
			while (posMutable.y() < 17 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(0, 1)
			}
			posMutable.add(0, -1)
			addQueue(visited, queue, path, posMutable)

			posMutable = Vector2i(pos)
			while (posMutable.y() >= 0 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(0, -1)
			}
			posMutable.add(0, 1)
			addQueue(visited, queue, path, posMutable)
		}
	}

	private fun addQueue(visited: MutableSet<Vector2ic>, queue: Queue<List<Vector2ic>>, path: List<Vector2ic>, newPos: Vector2ic) {
		if (!visited.contains(newPos)) {
			val newPath: MutableList<Vector2ic> = ArrayList(path)
			newPath.add(newPos)
			queue.add(newPath)
			visited.add(newPos)
		}
	}

	override fun render(context: WorldRenderContext?) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveSilverfish || !DungeonManager.isCurrentRoomMatched() || silverfishPath.isEmpty()) {
			return
		}
		val room = DungeonManager.getCurrentRoom()
		val pos = BlockPos.Mutable()
		for (i in 0 until silverfishPath.size - 1) {
			val start = Vec3d.ofCenter(room.relativeToActual(pos.set(23 - silverfishPath[i].y(), 67, 24 - silverfishPath[i].x())))
			val end = Vec3d.ofCenter(room.relativeToActual(pos.set(23 - silverfishPath[i + 1].y(), 67, 24 - silverfishPath[i + 1].x())))
			renderLinesFromPoints(context!!, arrayOf(start, end), RED_COLOR_COMPONENTS, 1f, 5f, true)
		}
	}

	override fun reset() {
		super.reset()
		for (silverfishBoardRow in silverfishBoard) {
			Arrays.fill(silverfishBoardRow, false)
		}
		silverfishPos = null
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Silverfish::class.java)
		val INSTANCE: Silverfish = Silverfish()
		private val RED_COLOR_COMPONENTS: FloatArray = DyeColor.RED.colorComponents
		fun init() {
			if (Debug.debugEnabled()) {
				ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
					dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("dungeons").then(ClientCommandManager.literal("puzzle").then(ClientCommandManager.literal(INSTANCE.puzzleName)
						.then(ClientCommandManager.literal("printBoard").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.silverfishBoard)))
							Command.SINGLE_SUCCESS
						}).then(ClientCommandManager.literal("printPath").executes { context: CommandContext<FabricClientCommandSource> ->
							context.source.sendFeedback(Constants.PREFIX.get().append(INSTANCE.silverfishPath.toString()))
							Command.SINGLE_SUCCESS
						})
					)
					)
					)
					)
				})
			}
		}

		private fun boardToString(silverfishBoard: Array<BooleanArray>): String {
			val sb = StringBuilder()
			for (row in silverfishBoard) {
				sb.append("\n")
				for (cell in row) {
					sb.append(if (cell) '#' else '.')
				}
			}
			return sb.toString()
		}
	}
}
