package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder

import net.minecraft.util.math.BlockPos

@JvmRecord
data class BoulderObject(val x: Int, val y: Int, val z: Int, val type: String) {
	fun get3DPosition(): BlockPos {
		return BlockPos(x, y, z)
	}
}
