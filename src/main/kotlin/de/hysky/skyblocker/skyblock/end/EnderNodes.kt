package de.hysky.skyblocker.skyblock.end

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheEnd
import de.hysky.skyblocker.utils.scheduler.Scheduler
import de.hysky.skyblocker.utils.waypoint.Waypoint
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import it.unimi.dsi.fastutil.ints.IntIntPair
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.function.Supplier

object EnderNodes {
	private val client: MinecraftClient = MinecraftClient.getInstance()
	private val enderNodes: MutableMap<BlockPos, EnderNode> = HashMap()

	fun init() {
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: EnderNodes? -> update() }, 20)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		AttackBlockCallback.EVENT.register(AttackBlockCallback { player: PlayerEntity?, world: World?, hand: Hand?, pos: BlockPos, direction: Direction? ->
			enderNodes.remove(pos)
			ActionResult.PASS
		})
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler: ClientPlayNetworkHandler?, sender: PacketSender?, client: MinecraftClient? -> reset() })
	}

	@JvmStatic
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
		if (yFrac == 0.25) {
			pos = BlockPos.ofFloored(x, y - 1, z)
			direction = Direction.UP
		} else if (yFrac == 0.75) {
			pos = BlockPos.ofFloored(x, y + 1, z)
			direction = Direction.DOWN
		} else if (xFrac == 0.25) {
			pos = BlockPos.ofFloored(x - 1, y, z)
			direction = Direction.EAST
		} else if (xFrac == 0.75) {
			pos = BlockPos.ofFloored(x + 1, y, z)
			direction = Direction.WEST
		} else if (zFrac == 0.25) {
			pos = BlockPos.ofFloored(x, y, z - 1)
			direction = Direction.SOUTH
		} else if (zFrac == 0.75) {
			pos = BlockPos.ofFloored(x, y, z + 1)
			direction = Direction.NORTH
		} else {
			return
		}

		val enderNode = enderNodes.computeIfAbsent(pos) { pos: BlockPos -> EnderNode(pos) }
		val particles = enderNode.particles[direction]
		particles!!.left(particles.leftInt() + 1)
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

	private fun shouldProcess(): Boolean {
		return SkyblockerConfigManager.config.otherLocations.end.enableEnderNodeHelper && isInTheEnd
	}

	private fun reset() {
		enderNodes.clear()
	}

	class EnderNode(pos: BlockPos) : Waypoint(pos, Supplier { SkyblockerConfigManager.config.uiAndVisuals.waypoints.waypointType }, DyeColor.CYAN.colorComponents, false) {
		val particles: Map<Direction, IntIntPair> = java.util.Map.of<Direction, IntIntPair>(
			Direction.UP, IntIntMutablePair(0, 0),
			Direction.DOWN, IntIntMutablePair(0, 0),
			Direction.EAST, IntIntMutablePair(0, 0),
			Direction.WEST, IntIntMutablePair(0, 0),
			Direction.SOUTH, IntIntMutablePair(0, 0),
			Direction.NORTH, IntIntMutablePair(0, 0)
		)
		private var lastConfirmed: Long = 0

		fun updateParticles() {
			val currentTimeMillis = System.currentTimeMillis()
			if (lastConfirmed + 2000 > currentTimeMillis || client.world == null || !particles.entries.stream().allMatch { entry: Map.Entry<Direction, IntIntPair> -> entry.value.leftInt() >= 5 && entry.value.rightInt() >= 5 || !client.world!!.getBlockState(pos!!.offset(entry.key)).isAir }) return
			lastConfirmed = currentTimeMillis
			for ((_, value) in particles) {
				value.left(0)
				value.right(0)
			}
		}

		override fun shouldRender(): Boolean {
			return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis()
		}
	}
}
