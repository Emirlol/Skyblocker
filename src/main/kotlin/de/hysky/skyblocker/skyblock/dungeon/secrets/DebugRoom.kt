package de.hysky.skyblocker.skyblock.dungeon.secrets

import de.hysky.skyblocker.utils.waypoint.Waypoint
import it.unimi.dsi.fastutil.ints.IntRBTreeSet
import it.unimi.dsi.fastutil.ints.IntSortedSets
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.world.ClientWorld
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import org.apache.commons.lang3.tuple.MutableTriple
import org.joml.Vector2ic
import java.util.*
import java.util.List
import java.util.Map
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

class DebugRoom(type: Type, vararg physicalPositions: Vector2ic?) : Room(type, *physicalPositions) {
	private val checkedBlocks: MutableList<Waypoint> = Collections.synchronizedList(ArrayList())

	override fun checkBlock(world: ClientWorld?, pos: BlockPos): Boolean {
		val id = DungeonManager.NUMERIC_ID.getByte(Registries.BLOCK.getId(world!!.getBlockState(pos).block).toString())
		if (id.toInt() == 0) {
			return false
		}
		for (directionRooms in possibleRooms!!) {
			val block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getLeft(), directionRooms.getMiddle(), pos), id)
			for (room in directionRooms.getRight()) {
				checkedBlocks.add(Waypoint(pos, SecretWaypoint.Companion.TYPE_SUPPLIER, if (Arrays.binarySearch(roomsData!![room!!], block) >= 0) Room.Companion.GREEN_COLOR_COMPONENTS else Room.Companion.RED_COLOR_COMPONENTS))
			}
		}
		return false
	}

	override fun render(context: WorldRenderContext?) {
		super.render(context)
		synchronized(checkedBlocks) {
			for (checkedBlock in checkedBlocks) {
				checkedBlock.render(context)
			}
		}
	}

	companion object {
		fun ofSinglePossibleRoom(type: Type, physicalPositions: Vector2ic?, roomName: String?, roomData: IntArray?, direction: Direction?): DebugRoom {
			return ofSinglePossibleRoom(type, arrayOf(physicalPositions), roomName, roomData, direction)
		}

		fun ofSinglePossibleRoom(type: Type, physicalPositions: Array<Vector2ic?>?, roomName: String?, roomData: IntArray?, direction: Direction?): DebugRoom {
			val room = DebugRoom(type, *physicalPositions!!)
			val segmentsX = IntSortedSets.unmodifiable(IntRBTreeSet(room.segments.stream().mapToInt { obj: Vector2ic -> obj.x() }.toArray()))
			val segmentsY = IntSortedSets.unmodifiable(IntRBTreeSet(room.segments.stream().mapToInt { obj: Vector2ic -> obj.y() }.toArray()))
			room.roomsData = Map.of(roomName, roomData)
			room.possibleRooms = List.of(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), List.of(roomName)))
			return room
		}
	}
}
