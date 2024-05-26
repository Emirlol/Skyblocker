package de.hysky.skyblocker.utils.render.culling

import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos

class ReducedWorldProvider : WorldProvider() {
	override fun isOpaqueFullCube(x: Int, y: Int, z: Int): Boolean {
		val pos = BlockPos(x, y, z)
		val state = world!!.getBlockState(pos)

		//Fixes edge cases where stairs etc aren't treated as being full blocks for the use case
		val isException = state.isIn(BlockTags.STAIRS) || state.isIn(BlockTags.WALLS) || state.isIn(BlockTags.FENCES)

		return isException || world!!.getBlockState(pos).isOpaqueFullCube(this.world, pos)
	}
}
