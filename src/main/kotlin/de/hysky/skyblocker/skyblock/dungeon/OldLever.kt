package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import net.minecraft.block.Block
import net.minecraft.block.enums.BlockFace
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

object OldLever {
	internal val FLOOR_SHAPE: VoxelShape = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 10.0, 12.0)
	internal val NORTH_SHAPE: VoxelShape = Block.createCuboidShape(5.0, 3.0, 10.0, 11.0, 13.0, 16.0)
	internal val SOUTH_SHAPE: VoxelShape = Block.createCuboidShape(5.0, 3.0, 0.0, 11.0, 13.0, 6.0)
	internal val EAST_SHAPE: VoxelShape = Block.createCuboidShape(0.0, 3.0, 5.0, 6.0, 13.0, 11.0)
	internal val WEST_SHAPE: VoxelShape = Block.createCuboidShape(10.0, 3.0, 5.0, 16.0, 13.0, 11.0)

	@JvmStatic
    fun getShape(face: BlockFace, direction: Direction?): VoxelShape? {
		if (!SkyblockerConfigManager.get().general.hitbox.oldLeverHitbox) return null

		if (face == BlockFace.FLOOR) {
			return FLOOR_SHAPE
		} else if (face == BlockFace.WALL) {
			when (direction) {
				Direction.EAST -> {
					return EAST_SHAPE
				}

				Direction.WEST -> {
					return WEST_SHAPE
				}

				Direction.SOUTH -> {
					return SOUTH_SHAPE
				}

				Direction.NORTH -> {
					return NORTH_SHAPE
				}
			}
		}
		return null
	}
}
