package de.hysky.skyblocker.skyblock.item

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import dev.isxander.yacl3.config.v2.api.SerialEntry
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import kotlin.math.floor
import kotlin.math.sqrt

object CustomArmorAnimatedDyes {
	private val STATE_TRACKER_MAP = Object2ObjectOpenHashMap<AnimatedDye, AnimatedDyeStateTracker>()
	private val NEW_STATE_TRACKER = Object2ObjectFunction<AnimatedDye, AnimatedDyeStateTracker> { _dye: Any? -> AnimatedDyeStateTracker.create() }
	private const val DEFAULT_TICK_DELAY = 4
	private var ticks = 0

	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { _client: MinecraftClient? -> ++ticks })
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
			.then(ClientCommandManager.literal("custom")
				.then(ClientCommandManager.literal("animatedDye")
					.executes { context: CommandContext<FabricClientCommandSource> -> customizeAnimatedDye(context.source, null, null, 0, false, 0) }
					.then(ClientCommandManager.argument("hex1", StringArgumentType.word())
						.then(ClientCommandManager.argument("hex2", StringArgumentType.word())
							.then(ClientCommandManager.argument("samples", IntegerArgumentType.integer(1))
								.then(ClientCommandManager.argument("cycleBack", BoolArgumentType.bool())
									.executes { context: CommandContext<FabricClientCommandSource> -> customizeAnimatedDye(context.source, StringArgumentType.getString(context, "hex1"), StringArgumentType.getString(context, "hex2"), IntegerArgumentType.getInteger(context, "samples"), BoolArgumentType.getBool(context, "cycleBack"), DEFAULT_TICK_DELAY) }
									.then(ClientCommandManager.argument("tickDelay", IntegerArgumentType.integer(0, 20))
										.executes { context: CommandContext<FabricClientCommandSource> -> customizeAnimatedDye(context.source, StringArgumentType.getString(context, "hex1"), StringArgumentType.getString(context, "hex2"), IntegerArgumentType.getInteger(context, "samples"), BoolArgumentType.getBool(context, "cycleBack"), IntegerArgumentType.getInteger(context, "tickDelay")) })
								)
							)
						)
					)
				)
			)
		)
	}

	private fun customizeAnimatedDye(source: FabricClientCommandSource, hex1: String?, hex2: String?, samples: Int, cycleBack: Boolean, tickDelay: Int): Int {
		if (hex1 != null && hex2 != null && (!CustomArmorDyeColors.isHexadecimalColor(hex1) || !CustomArmorDyeColors.isHexadecimalColor(hex2))) {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.invalidHex")))

			return Command.SINGLE_SUCCESS
		}

		val heldItem = source.player.mainHandStack

		if (isOnSkyblock && heldItem != null && !heldItem.isEmpty) {
			if (heldItem.isIn(ItemTags.DYEABLE)) {
				val itemUuid = getItemUuid(heldItem)

				if (!itemUuid.isEmpty()) {
					val customAnimatedDyes = SkyblockerConfigManager.config.general.customAnimatedDyes

					if (hex1 == null && hex2 == null) {
						if (customAnimatedDyes.containsKey(itemUuid)) {
							customAnimatedDyes.remove(itemUuid)
							SkyblockerConfigManager.save()
							source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.removed")))
						} else {
							source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.neverHad")))
						}
					} else {
						val animatedDye = AnimatedDye(Integer.decode("0x" + hex1!!.replace("#", "")), Integer.decode("0x" + hex2!!.replace("#", "")), samples, cycleBack, tickDelay)

						customAnimatedDyes[itemUuid] = animatedDye
						SkyblockerConfigManager.save()
						source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.added")))
					}
				} else {
					source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.noItemUuid")))
				}
			} else {
				source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.notDyeable")))
			}
		} else {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customAnimatedDyes.unableToSetDye")))
		}

		return Command.SINGLE_SUCCESS
	}

	@JvmStatic
	fun animateColorTransition(animatedDye: AnimatedDye): Int {
		val trackedState = STATE_TRACKER_MAP.computeIfAbsent(animatedDye, NEW_STATE_TRACKER)

		if (trackedState.lastRecordedTick + animatedDye.tickDelay > ticks) {
			return trackedState.lastColor
		}

		trackedState.lastRecordedTick = ticks

		return animatedDye.interpolate(trackedState)
	}

	//Credit to https://codepen.io/OliverBalfour/post/programmatically-making-gradients
	fun interpolate(firstColor: Int, secondColor: Int, percentage: Double): Int {
		val r1 = MathHelper.square((firstColor shr 16) and 0xFF)
		val g1 = MathHelper.square((firstColor shr 8) and 0xFF)
		val b1 = MathHelper.square(firstColor and 0xFF)

		val r2 = MathHelper.square((secondColor shr 16) and 0xFF)
		val g2 = MathHelper.square((secondColor shr 8) and 0xFF)
		val b2 = MathHelper.square(secondColor and 0xFF)

		val inverse = 1.0 - percentage

		val r3 = floor(sqrt(r1 * inverse + r2 * percentage)).toInt()
		val g3 = floor(sqrt(g1 * inverse + g2 * percentage)).toInt()
		val b3 = floor(sqrt(b1 * inverse + b2 * percentage)).toInt()

		return (r3 shl 16) or (g3 shl 8) or b3
	}

	private class AnimatedDyeStateTracker {
		var sampleCounter: Int = 0
		var onBackCycle: Boolean = false
		var lastColor: Int = 0
		var lastRecordedTick: Int = 0

		fun shouldCycleBack(samples: Int, canCycleBack: Boolean): Boolean {
			return canCycleBack && sampleCounter == samples
		}

		val andDecrement: Int
			get() = sampleCounter--

		val andIncrement: Int
			get() = sampleCounter++

		companion object {
			fun create(): AnimatedDyeStateTracker {
				return AnimatedDyeStateTracker()
			}
		}
	}

	@JvmRecord
	data class AnimatedDye(@field:SerialEntry @param:SerialEntry val color1: Int, @field:SerialEntry @param:SerialEntry val color2: Int, @field:SerialEntry @param:SerialEntry val samples: Int, @field:SerialEntry @param:SerialEntry val cycleBack: Boolean, @field:SerialEntry @param:SerialEntry val tickDelay: Int) {
		private fun interpolate(stateTracker: AnimatedDyeStateTracker): Int {
			if (stateTracker.shouldCycleBack(samples, cycleBack)) stateTracker.onBackCycle = true

			if (stateTracker.onBackCycle) {
				val percent: Double = (1.0 / samples.toDouble()) * stateTracker.getAndDecrement()

				//Go back to normal cycle once we've cycled all the way back
				if (stateTracker.sampleCounter == 0) stateTracker.onBackCycle = false

				val interpolatedColor = interpolate(color1, color2, percent)
				stateTracker.lastColor = interpolatedColor

				return interpolatedColor
			}

			//This will only happen if cycleBack is false
			if (stateTracker.sampleCounter == samples) stateTracker.sampleCounter = 0

			val percent: Double = (1.0 / samples.toDouble()) * stateTracker.getAndIncrement()
			val interpolatedColor = interpolate(color1, color2, percent)

			stateTracker.lastColor = interpolatedColor

			return interpolatedColor
		}
	}
}
