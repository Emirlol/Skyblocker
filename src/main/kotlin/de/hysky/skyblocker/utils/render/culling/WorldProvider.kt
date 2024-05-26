package de.hysky.skyblocker.utils.render.culling

import com.logisticscraft.occlusionculling.DataProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

open class WorldProvider : DataProvider {
	protected var world: ClientWorld? = null

	override fun prepareChunk(chunkX: Int, chunkZ: Int): Boolean {
		this.world = CLIENT.world
		return this.world != null
	}

	override fun isOpaqueFullCube(x: Int, y: Int, z: Int): Boolean {
		val pos = BlockPos(x, y, z)
		return world!!.getBlockState(pos).isOpaqueFullCube(this.world, pos)
	}

	override fun cleanup() {
		this.world = null
	}

	companion object {
		private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	}
}
