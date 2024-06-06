package de.hysky.skyblocker.skyblock.waypoint

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import de.hysky.skyblocker.utils.Utils.locationRaw
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import de.hysky.skyblocker.utils.waypoint.Waypoint
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.util.TriState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.commons.math3.stat.regression.SimpleRegression
import java.util.regex.Pattern

object MythologicalRitual {
	private val GRIFFIN_BURROW_DUG: Pattern = Pattern.compile("(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d)/4\\)")
	private val ORANGE_COLOR_COMPONENTS: FloatArray = DyeColor.ORANGE.colorComponents
	private var lastEchoTime: Long = 0
	private val griffinBurrows: MutableMap<BlockPos?, GriffinBurrow?> = HashMap()
	private var lastDugBurrowPos: BlockPos? = null
	private var previousBurrow: GriffinBurrow? = GriffinBurrow(BlockPos.ORIGIN)

	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
		AttackBlockCallback.EVENT.register(AttackBlockCallback { obj: PlayerEntity?, player: World?, world: Hand?, hand: BlockPos?, pos: Direction? -> onAttackBlock(player, world, hand, pos) })
		UseBlockCallback.EVENT.register(UseBlockCallback { obj: PlayerEntity?, player: World?, world: Hand?, hand: BlockHitResult? -> onUseBlock(player, world, hand) })
		UseItemCallback.EVENT.register(UseItemCallback { obj: PlayerEntity?, player: World?, world: Hand? -> onUseItem(player, world) })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, message: Boolean -> onChatMessage(message) })
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler: ClientPlayNetworkHandler?, sender: PacketSender?, client: MinecraftClient? -> reset() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(
					ClientCommandManager.literal("diana")
						.then(ClientCommandManager.literal("clearGriffinBurrows").executes { context: CommandContext<FabricClientCommandSource?>? ->
							reset()
							Command.SINGLE_SUCCESS
						})
						.then(
							ClientCommandManager.literal("clearGriffinBurrow")
								.then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes { context: CommandContext<FabricClientCommandSource?> ->
									griffinBurrows.remove(context.getArgument("pos", BlockPos::class.java))
									Command.SINGLE_SUCCESS
								})
						)
				)
			)
		})

		// Put a root burrow so echo detection works without a previous burrow
		previousBurrow!!.confirmed = TriState.DEFAULT
		griffinBurrows[BlockPos.ORIGIN] = previousBurrow
	}

	@JvmStatic
    fun onParticle(packet: ParticleS2CPacket) {
		if (isActive) {
			if (ParticleTypes.CRIT == packet.parameters.type || ParticleTypes.ENCHANT == packet.parameters.type) {
				val pos = BlockPos.ofFloored(packet.x, packet.y, packet.z).down()
				if (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world!!.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
					return
				}
				val burrow = griffinBurrows.computeIfAbsent(pos) { pos: BlockPos? -> GriffinBurrow(pos) }
				if (ParticleTypes.CRIT == packet.parameters.type) burrow!!.critParticle++
				if (ParticleTypes.ENCHANT == packet.parameters.type) burrow!!.enchantParticle++
				if (burrow!!.critParticle >= 5 && burrow!!.enchantParticle >= 5 && burrow!!.confirmed == TriState.FALSE) {
					griffinBurrows[pos]!!.init()
				}
			} else if (ParticleTypes.DUST == packet.parameters.type) {
				val pos = BlockPos.ofFloored(packet.x, packet.y, packet.z)
				val burrow = griffinBurrows[pos.down(2)] ?: return
				burrow.regression.addData(packet.x, packet.z)
				val slope = burrow.regression.slope
				if (java.lang.Double.isNaN(slope)) {
					return
				}
				val nextBurrowDirection = Vec3d(100.0, 0.0, slope * 100).normalize()
				if (burrow.nextBurrowLine == null) {
					burrow.nextBurrowLine = arrayOfNulls(1001)
				}
				fillLine(burrow.nextBurrowLine, Vec3d.ofCenter(pos.up()), nextBurrowDirection)
			} else if (ParticleTypes.DRIPPING_LAVA == packet.parameters.type && packet.count == 2) {
				if (System.currentTimeMillis() > lastEchoTime + 10000) {
					return
				}
				if (previousBurrow!!.echoBurrowDirection == null) {
					previousBurrow!!.echoBurrowDirection = arrayOfNulls(2)
				}
				previousBurrow!!.echoBurrowDirection!![0] = previousBurrow!!.echoBurrowDirection!![1]
				previousBurrow!!.echoBurrowDirection!![1] = Vec3d(packet.x, packet.y, packet.z)
				if (previousBurrow!!.echoBurrowDirection!![0] == null || previousBurrow!!.echoBurrowDirection!![1] == null) {
					return
				}
				val echoBurrowDirection = previousBurrow!!.echoBurrowDirection!![1]!!.subtract(previousBurrow!!.echoBurrowDirection!![0]).normalize()
				if (previousBurrow!!.echoBurrowLine == null) {
					previousBurrow!!.echoBurrowLine = arrayOfNulls(1001)
				}
				fillLine(previousBurrow!!.echoBurrowLine, previousBurrow!!.echoBurrowDirection!![0], echoBurrowDirection)
			}
		}
	}

	fun fillLine(line: Array<Vec3d?>?, start: Vec3d?, direction: Vec3d?) {
		assert(line!!.size % 2 == 1)
		val middle = line.size / 2
		line[middle] = start
		for (i in 0 until middle) {
			line[middle + 1 + i] = line[middle + i]!!.add(direction)
			line[middle - 1 - i] = line[middle - i].subtract(direction)
		}
	}

	fun render(context: WorldRenderContext?) {
		if (isActive) {
			for (burrow in griffinBurrows.values) {
				if (burrow!!.shouldRender()) {
					burrow.render(context!!)
				}
				if (burrow.confirmed != TriState.FALSE) {
					if (burrow.nextBurrowLine != null) {
						renderLinesFromPoints(context!!, burrow.nextBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5f, 5f, false)
					}
					if (burrow.echoBurrowLine != null) {
						renderLinesFromPoints(context!!, burrow.echoBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5f, 5f, false)
					}
				}
			}
		}
	}

	fun onAttackBlock(player: PlayerEntity?, world: World?, hand: Hand?, pos: BlockPos, direction: Direction?): ActionResult {
		return onInteractBlock(pos)
	}

	fun onUseBlock(player: PlayerEntity?, world: World?, hand: Hand?, hitResult: BlockHitResult): ActionResult {
		return onInteractBlock(hitResult.blockPos)
	}

	private fun onInteractBlock(pos: BlockPos): ActionResult {
		if (isActive && griffinBurrows.containsKey(pos)) {
			lastDugBurrowPos = pos
		}
		return ActionResult.PASS
	}

	fun onUseItem(player: PlayerEntity, world: World?, hand: Hand?): TypedActionResult<ItemStack?> {
		val stack = player.getStackInHand(hand)
		if (isActive && getItemId(stack) == "ANCESTRAL_SPADE") {
			lastEchoTime = System.currentTimeMillis()
		}
		return TypedActionResult.pass(stack)
	}

	fun onChatMessage(message: Text, overlay: Boolean) {
		if (isActive && GRIFFIN_BURROW_DUG.matcher(message.string).matches()) {
			previousBurrow!!.confirmed = TriState.FALSE
			previousBurrow = griffinBurrows[lastDugBurrowPos]
			previousBurrow!!.confirmed = TriState.DEFAULT
		}
	}

	private val isActive: Boolean
		get() = SkyblockerConfigManager.config.helpers.mythologicalRitual.enableMythologicalRitualHelper && locationRaw == "hub"

	private fun reset() {
		griffinBurrows.clear()
		lastDugBurrowPos = null
		previousBurrow = GriffinBurrow(BlockPos.ORIGIN)

		// Put a root burrow so echo detection works without a previous burrow
		previousBurrow!!.confirmed = TriState.DEFAULT
		griffinBurrows[BlockPos.ORIGIN] = previousBurrow
	}

	private class GriffinBurrow(pos: BlockPos?) : Waypoint(pos, Type.WAYPOINT, ORANGE_COLOR_COMPONENTS, 0.25f) {
		var critParticle: Int = 0
		var enchantParticle: Int = 0
		var confirmed: TriState = TriState.FALSE
		val regression: SimpleRegression = SimpleRegression()
		var nextBurrowLine: Array<Vec3d?>?
		var echoBurrowDirection: Array<Vec3d?>?
		var echoBurrowLine: Array<Vec3d?>?

		fun init() {
			confirmed = TriState.TRUE
			regression.clear()
		}

		override fun shouldRender(): Boolean {
			return super.shouldRender() && confirmed == TriState.TRUE
		}
	}
}
