package de.hysky.skyblocker.skyblock.dungeon.secrets

import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.IntSortedSet
import it.unimi.dsi.fastutil.objects.ObjectIntPair
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.item.map.MapDecorationTypes
import net.minecraft.item.map.MapState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import org.joml.RoundingMode
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.*

object DungeonMapUtils {
	val BLACK_COLOR: Byte = MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOWEST)
	val WHITE_COLOR: Byte = MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH)

	fun getColor(map: MapState, pos: Vector2ic?): Byte {
		return if (pos == null) -1 else getColor(map, pos.x(), pos.y())
	}

	fun getColor(map: MapState, x: Int, z: Int): Byte {
		if (x < 0 || z < 0 || x >= 128 || z >= 128) {
			return -1
		}
		return map.colors[x + (z shl 7)]
	}

	fun isEntranceColor(map: MapState, x: Int, z: Int): Boolean {
		return getColor(map, x, z) == Room.Type.ENTRANCE.color
	}

	fun isEntranceColor(map: MapState, pos: Vector2ic?): Boolean {
		return getColor(map, pos) == Room.Type.ENTRANCE.color
	}

	private fun getMapPlayerPos(map: MapState): Vector2i? {
		for (decoration in map.decorations) {
			if (decoration.type().value() == MapDecorationTypes.FRAME.value()) {
				return Vector2i((decoration.x().toInt() shr 1) + 64, (decoration.z().toInt() shr 1) + 64)
			}
		}
		return null
	}

	fun getMapEntrancePosAndRoomSize(map: MapState): ObjectIntPair<Vector2ic>? {
		var mapPos: Vector2ic? = getMapPlayerPos(map) ?: return null
		val posToCheck: Queue<Vector2ic> = ArrayDeque()
		val checked: MutableSet<Vector2ic> = HashSet()
		posToCheck.add(mapPos)
		checked.add(mapPos)
		while ((posToCheck.poll().also { mapPos = it }) != null) {
			if (isEntranceColor(map, mapPos)) {
				val mapEntranceAndRoomSizePos = getMapEntrancePosAndRoomSizeAt(map, mapPos)
				if (mapEntranceAndRoomSizePos.rightInt() > 0) {
					return mapEntranceAndRoomSizePos
				}
			}
			var pos: Vector2ic = Vector2i(mapPos).sub(10, 0)
			if (checked.add(pos)) {
				posToCheck.add(pos)
			}
			pos = Vector2i(mapPos).sub(0, 10)
			if (checked.add(pos)) {
				posToCheck.add(pos)
			}
			pos = Vector2i(mapPos).add(10, 0)
			if (checked.add(pos)) {
				posToCheck.add(pos)
			}
			pos = Vector2i(mapPos).add(0, 10)
			if (checked.add(pos)) {
				posToCheck.add(pos)
			}
		}
		return null
	}

	private fun getMapEntrancePosAndRoomSizeAt(map: MapState, mapPosImmutable: Vector2ic?): ObjectIntPair<Vector2ic> {
		val mapPos = Vector2i(mapPosImmutable)
		// noinspection StatementWithEmptyBody
		while (isEntranceColor(map, mapPos.sub(1, 0))) {
		}
		mapPos.add(1, 0)
		while (isEntranceColor(map, mapPos.sub(0, 1))) {
		}
		return ObjectIntPair.of(mapPos.add(0, 1), getMapRoomSize(map, mapPos))
	}

	fun getMapRoomSize(map: MapState, mapEntrancePos: Vector2ic): Int {
		var i = -1
		while (isEntranceColor(map, mapEntrancePos.x() + ++i, mapEntrancePos.y())) {
		}
		return if (i > 5) i else 0
	}

	/**
	 * Gets the map position of the top left corner of the room the player is in.
	 *
	 * @param map            the map
	 * @param mapEntrancePos the map position of the top left corner of the entrance
	 * @param mapRoomSize    the size of a room on the map
	 * @return the map position of the top left corner of the room the player is in
	 * @implNote `mapPos` is shifted by 2 so room borders are evenly split.
	 * `mapPos` is then shifted by `offset` to align the top left most room at (0, 0)
	 * so subtracting the modulo will give the top left corner of the room shifted by `offset`.
	 * Finally, `mapPos` is shifted back by `offset` to its intended position.
	 */
	fun getMapRoomPos(map: MapState, mapEntrancePos: Vector2ic?, mapRoomSize: Int): Vector2ic? {
		val mapRoomSizeWithGap = mapRoomSize + 4
		val mapPos = getMapPlayerPos(map) ?: return null
		val offset: Vector2ic = Vector2i(mapEntrancePos!!.x() % mapRoomSizeWithGap, mapEntrancePos.y() % mapRoomSizeWithGap)
		return mapPos.add(2, 2).sub(offset).sub(Math.floorMod(mapPos.x(), mapRoomSizeWithGap), Math.floorMod(mapPos.y(), mapRoomSizeWithGap)).add(offset)
	}

	/**
	 * Gets the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room.
	 *
	 * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
	 * @param mapEntrancePos      the map position of the top left corner of the entrance room
	 * @param mapRoomSize         the size of a room on the map
	 * @param physicalPos         the physical position of the northwest corner of the room
	 * @return the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room
	 */
	fun getMapPosFromPhysical(physicalEntrancePos: Vector2ic?, mapEntrancePos: Vector2ic?, mapRoomSize: Int, physicalPos: Vector2ic?): Vector2ic {
		return Vector2i(physicalPos).sub(physicalEntrancePos).div(32).mul(mapRoomSize + 4).add(mapEntrancePos)
	}

	/**
	 * @see .getPhysicalRoomPos
	 */
	fun getPhysicalRoomPos(pos: Vec3d): Vector2ic {
		return getPhysicalRoomPos(pos.getX(), pos.getZ())
	}

	/**
	 * @see .getPhysicalRoomPos
	 */
	fun getPhysicalRoomPos(pos: Vec3i): Vector2ic {
		return getPhysicalRoomPos(pos.x.toDouble(), pos.z.toDouble())
	}

	/**
	 * Gets the physical position of the northwest corner of the room the given coordinate is in. Hypixel Skyblock Dungeons are aligned to a 32 by 32 blocks grid, allowing corners to be calculated through math.
	 *
	 * @param x the x position of the coordinate to calculate
	 * @param z the z position of the coordinate to calculate
	 * @return the physical position of the northwest corner of the room the player is in
	 * @implNote `physicalPos` is shifted by 0.5 so room borders are evenly split.
	 * `physicalPos` is further shifted by 8 because Hypixel offset dungeons by 8 blocks in Skyblock 0.12.3.
	 * Subtracting the modulo gives the northwest corner of the room shifted by 8. Finally, `physicalPos` is shifted back by 8 to its intended position.
	 */
	fun getPhysicalRoomPos(x: Double, z: Double): Vector2ic {
		val physicalPos = Vector2i(x + 8.5, z + 8.5, RoundingMode.TRUNCATE)
		return physicalPos.sub(Math.floorMod(physicalPos.x(), 32), Math.floorMod(physicalPos.y(), 32)).sub(8, 8)
	}

	fun getPhysicalPosFromMap(mapEntrancePos: Vector2ic?, mapRoomSize: Int, physicalEntrancePos: Vector2ic?, vararg mapPositions: Vector2ic?): Array<Vector2ic?> {
		for (i in mapPositions.indices) {
			mapPositions[i] = getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, mapPositions[i])
		}
		return mapPositions
	}

	/**
	 * Gets the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room.
	 *
	 * @param mapEntrancePos      the map position of the top left corner of the entrance room
	 * @param mapRoomSize         the size of a room on the map
	 * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
	 * @param mapPos              the map position of the top left corner of the room
	 * @return the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room
	 */
	fun getPhysicalPosFromMap(mapEntrancePos: Vector2ic?, mapRoomSize: Int, physicalEntrancePos: Vector2ic?, mapPos: Vector2ic?): Vector2ic {
		return Vector2i(mapPos).sub(mapEntrancePos).div(mapRoomSize + 4).mul(32).add(physicalEntrancePos)
	}

	fun getPhysicalCornerPos(direction: Room.Direction?, segmentsX: IntSortedSet, segmentsY: IntSortedSet): Vector2ic {
		return when (direction) {
			Room.Direction.NW -> Vector2i(segmentsX.firstInt(), segmentsY.firstInt())
			Room.Direction.NE -> Vector2i(segmentsX.lastInt() + 30, segmentsY.firstInt())
			Room.Direction.SW -> Vector2i(segmentsX.firstInt(), segmentsY.lastInt() + 30)
			Room.Direction.SE -> Vector2i(segmentsX.lastInt() + 30, segmentsY.lastInt() + 30)
		}
	}

	fun actualToRelative(direction: Room.Direction?, physicalCornerPos: Vector2ic?, pos: BlockPos): BlockPos {
		return when (direction) {
			Room.Direction.NW -> BlockPos(pos.x - physicalCornerPos!!.x(), pos.y, pos.z - physicalCornerPos.y())
			Room.Direction.NE -> BlockPos(pos.z - physicalCornerPos!!.y(), pos.y, -pos.x + physicalCornerPos.x())
			Room.Direction.SW -> BlockPos(-pos.z + physicalCornerPos!!.y(), pos.y, pos.x - physicalCornerPos.x())
			Room.Direction.SE -> BlockPos(-pos.x + physicalCornerPos!!.x(), pos.y, -pos.z + physicalCornerPos.y())
		}
	}

	fun relativeToActual(direction: Room.Direction?, physicalCornerPos: Vector2ic?, posJson: JsonObject): BlockPos {
		return relativeToActual(direction, physicalCornerPos, BlockPos(posJson["x"].asInt, posJson["y"].asInt, posJson["z"].asInt))
	}

	fun relativeToActual(direction: Room.Direction?, physicalCornerPos: Vector2ic?, pos: BlockPos?): BlockPos {
		return when (direction) {
			Room.Direction.NW -> BlockPos(pos!!.x + physicalCornerPos!!.x(), pos.y, pos.z + physicalCornerPos.y())
			Room.Direction.NE -> BlockPos(-pos!!.z + physicalCornerPos!!.x(), pos.y, pos.x + physicalCornerPos.y())
			Room.Direction.SW -> BlockPos(pos!!.z + physicalCornerPos!!.x(), pos.y, -pos.x + physicalCornerPos.y())
			Room.Direction.SE -> BlockPos(-pos!!.x + physicalCornerPos!!.x(), pos.y, -pos.z + physicalCornerPos.y())
		}
	}

	fun getRoomType(map: MapState, mapPos: Vector2ic?): Room.Type {
		return when (getColor(map, mapPos)) {
			30 -> Room.Type.ENTRANCE
			63 -> Room.Type.ROOM
			66 -> Room.Type.PUZZLE
			62 -> Room.Type.TRAP
			74 -> Room.Type.MINIBOSS
			82 -> Room.Type.FAIRY
			18 -> Room.Type.BLOOD
			85 -> Room.Type.UNKNOWN
			else -> null
		}
	}

	fun getRoomSegments(map: MapState, mapPos: Vector2ic?, mapRoomSize: Int, color: Byte): Array<Vector2ic> {
		val segments: MutableSet<Vector2ic?> = HashSet()
		val queue: Queue<Vector2ic?> = ArrayDeque()
		segments.add(mapPos)
		queue.add(mapPos)
		while (!queue.isEmpty()) {
			val curMapPos = queue.poll()
			var newMapPos = Vector2i()
			if (getColor(map, newMapPos.set(curMapPos).sub(1, 0)) == color && !segments.contains(newMapPos.sub(mapRoomSize + 3, 0))) {
				segments.add(newMapPos)
				queue.add(newMapPos)
				newMapPos = Vector2i()
			}
			if (getColor(map, newMapPos.set(curMapPos).sub(0, 1)) == color && !segments.contains(newMapPos.sub(0, mapRoomSize + 3))) {
				segments.add(newMapPos)
				queue.add(newMapPos)
				newMapPos = Vector2i()
			}
			if (getColor(map, newMapPos.set(curMapPos).add(mapRoomSize, 0)) == color && !segments.contains(newMapPos.add(4, 0))) {
				segments.add(newMapPos)
				queue.add(newMapPos)
				newMapPos = Vector2i()
			}
			if (getColor(map, newMapPos.set(curMapPos).add(0, mapRoomSize)) == color && !segments.contains(newMapPos.add(0, 4))) {
				segments.add(newMapPos)
				queue.add(newMapPos)
			}
		}
		DungeonManager.LOGGER.debug("[Skyblocker] Found dungeon room segments: {}", segments.toTypedArray().contentToString())
		return segments.toArray<Vector2ic> { _Dummy_.__Array__() }
	}

	fun getWitherBloodDoorPos(world: World?, physicalPositions: Collection<Vector2ic>): BlockPos? {
		val doorPos = BlockPos.Mutable()
		for (pos in physicalPositions) {
			if (hasWitherOrBloodDoor(world, pos, doorPos)) {
				return doorPos
			}
		}
		return null
	}

	private fun hasWitherOrBloodDoor(world: World?, pos: Vector2ic, doorPos: BlockPos.Mutable): Boolean {
		return isWitherOrBloodDoor(world, doorPos.set(pos.x() + 1, 72, pos.y() + 17)) ||
				isWitherOrBloodDoor(world, doorPos.set(pos.x() + 17, 72, pos.y() + 1)) ||
				isWitherOrBloodDoor(world, doorPos.set(pos.x() + 17, 72, pos.y() + 33)) ||
				isWitherOrBloodDoor(world, doorPos.set(pos.x() + 33, 72, pos.y() + 17))
	}

	private fun isWitherOrBloodDoor(world: World?, pos: BlockPos.Mutable): Boolean {
		return world!!.getStatesInBox(Box.enclosing(pos, pos.move(-3, -3, -3))).allMatch { state: BlockState -> state.isOf(Blocks.COAL_BLOCK) || state.isOf(Blocks.RED_TERRACOTTA) }
	}
}
