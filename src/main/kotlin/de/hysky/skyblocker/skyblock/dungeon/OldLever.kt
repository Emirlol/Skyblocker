package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import net.minecraft.block.Block
import net.minecraft.block.enums.BlockFace
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

object OldLever {
	private val FLOOR_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 10.0, 12.0)
	private val NORTH_SHAPE = Block.createCuboidShape(5.0, 3.0, 10.0, 11.0, 13.0, 16.0)
	private val SOUTH_SHAPE = Block.createCuboidShape(5.0, 3.0, 0.0, 11.0, 13.0, 6.0)
	private val EAST_SHAPE = Block.createCuboidShape(0.0, 3.0, 5.0, 6.0, 13.0, 11.0)
	private val WEST_SHAPE= Block.createCuboidShape(10.0, 3.0, 5.0, 16.0, 13.0, 11.0)

    fun getShape(face: BlockFace, direction: Direction?): VoxelShape? {
		if (!SkyblockerConfigManager.config.general.hitbox.oldLeverHitbox) return null

	    return when (face) {
		    BlockFace.FLOOR -> {
			    FLOOR_SHAPE
		    }
		    BlockFace.WALL -> {
			    when (direction) {
				    Direction.EAST -> EAST_SHAPE
				    Direction.WEST -> WEST_SHAPE
				    Direction.SOUTH -> SOUTH_SHAPE
				    Direction.NORTH -> NORTH_SHAPE
				    else -> null
			    }
		    }
		    else -> null
	    }
    }
}
