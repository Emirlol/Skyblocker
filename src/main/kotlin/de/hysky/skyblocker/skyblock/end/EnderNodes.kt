package de.hysky.skyblocker.skyblock.end

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheEnd
import de.hysky.skyblocker.utils.scheduler.Scheduler
import de.hysky.skyblocker.utils.waypoint.Waypoint
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper

object EnderNodes {
	private val client: MinecraftClient = MinecraftClient.getInstance()
	private val enderNodes: MutableMap<BlockPos, EnderNode> = HashMap()

	fun init() {
		Scheduler.scheduleCyclic(20, task = ::update)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(::render)
		AttackBlockCallback.EVENT.register { _, _, _, pos: BlockPos, _ ->
			enderNodes.remove(pos)
			ActionResult.PASS
		}
		ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
	}

	fun onParticle(packet: ParticleS2CPacket) {
		if (!shouldProcess()) return
		val particleType = packet.parameters.type
		if (ParticleTypes.PORTAL.type != particleType && ParticleTypes.WITCH.type != particleType) return

		val x = packet.x
		val y = packet.y
		val z = packet.z
		val xFrac = MathHelper.floorMod(x, 1.0)
		val yFrac = MathHelper.floorMod(y, 1.0)
		val zFrac = MathHelper.floorMod(z, 1.0)
		val pos: BlockPos
		val direction: Direction
		when {
			yFrac == 0.25 -> {
				pos = BlockPos.ofFloored(x, y - 1, z)
				direction = Direction.UP
			}
			yFrac == 0.75 -> {
				pos = BlockPos.ofFloored(x, y + 1, z)
				direction = Direction.DOWN
			}
			xFrac == 0.25 -> {
				pos = BlockPos.ofFloored(x - 1, y, z)
				direction = Direction.EAST
			}
			xFrac == 0.75 -> {
				pos = BlockPos.ofFloored(x + 1, y, z)
				direction = Direction.WEST
			}
			zFrac == 0.25 -> {
				pos = BlockPos.ofFloored(x, y, z - 1)
				direction = Direction.SOUTH
			}
			zFrac == 0.75 -> {
				pos = BlockPos.ofFloored(x, y, z + 1)
				direction = Direction.NORTH
			}
			else -> return
		}

		val enderNode = enderNodes.computeIfAbsent(pos) { EnderNode(it) }
		val particles = enderNode.particles[direction]!!
		particles.left(particles.leftInt() + 1)
		particles.right(particles.rightInt() + 1)
	}

	private fun update() {
		if (shouldProcess()) {
			for (enderNode in enderNodes.values) {
				enderNode.updateParticles()
			}
		}
	}

	private fun render(context: WorldRenderContext) {
		if (shouldProcess()) {
			for (enderNode in enderNodes.values) {
				if (enderNode.shouldRender()) {
					enderNode.render(context)
				}
			}
		}
	}

	private fun shouldProcess() = SkyblockerConfigManager.config.otherLocations.end.enableEnderNodeHelper && isInTheEnd

	private fun reset() = enderNodes.clear()

	class EnderNode(pos: BlockPos) : Waypoint(pos, SkyblockerConfigManager.config.uiAndVisuals.waypoints.waypointType, DyeColor.CYAN.colorComponents, throughWalls = false) {
		val particles = mapOf(
			Direction.UP to IntIntMutablePair(0, 0),
			Direction.DOWN to IntIntMutablePair(0, 0),
			Direction.EAST to IntIntMutablePair(0, 0),
			Direction.WEST to IntIntMutablePair(0, 0),
			Direction.SOUTH to  IntIntMutablePair(0, 0),
			Direction.NORTH to IntIntMutablePair(0, 0)
		)
		private var lastConfirmed = 0L

		fun updateParticles() {
			val currentTimeMillis = System.currentTimeMillis()
			if (lastConfirmed + 2000 > currentTimeMillis || client.world == null || !particles.all { entry -> entry.value.leftInt() >= 5 && entry.value.rightInt() >= 5 || !client.world!!.getBlockState(pos.offset(entry.key)).isAir }) return
			lastConfirmed = currentTimeMillis
			for (value in particles.values) {
				value.left(0)
				value.right(0)
			}
		}

		override fun shouldRender() = super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis()
	}
}
