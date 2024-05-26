package de.hysky.skyblocker.skyblock.garden

import com.mojang.brigadier.CommandDispatcher
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import de.hysky.skyblocker.utils.ItemUtils.getLoreLineIfMatch
import de.hysky.skyblocker.utils.Location
import de.hysky.skyblocker.utils.Utils.location
import de.hysky.skyblocker.utils.scheduler.Scheduler.Companion.queueOpenScreenCommand
import it.unimi.dsi.fastutil.floats.FloatLongPair
import it.unimi.dsi.fastutil.ints.IntLongPair
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
import it.unimi.dsi.fastutil.longs.LongPriorityQueue
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern

object FarmingHud {
	private val LOGGER: Logger = LoggerFactory.getLogger(FarmingHud::class.java)
	val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)
	private val COUNTER: Pattern = Pattern.compile("Counter: (?<count>[\\d,]+) .+")
	private val FARMING_XP: Pattern = Pattern.compile("§3\\+(?<xp>\\d+.?\\d*) Farming \\((?<percent>[\\d,]+.?\\d*)%\\)")
	private val counter: Deque<IntLongPair> = ArrayDeque()
	private val blockBreaks: LongPriorityQueue = LongArrayFIFOQueue()
	private val farmingXp: Queue<FloatLongPair> = ArrayDeque()
	private var farmingXpPercentProgress = 0f

	fun init() {
		HudRenderEvents.AFTER_MAIN_HUD.register(HudRenderStage { context: DrawContext?, tickDelta: Float ->
			if (shouldRender()) {
				if (!counter.isEmpty() && counter.peek().rightLong() + 10000 < System.currentTimeMillis()) {
					counter.poll()
				}
				if (!blockBreaks.isEmpty && blockBreaks.firstLong() + 1000 < System.currentTimeMillis()) {
					blockBreaks.dequeueLong()
				}
				if (!farmingXp.isEmpty() && farmingXp.peek().rightLong() + 1000 < System.currentTimeMillis()) {
					farmingXp.poll()
				}

				val stack = MinecraftClient.getInstance().player!!.mainHandStack
				val matcher = getLoreLineIfMatch(stack, COUNTER)
				if (matcher != null) {
					try {
						val count = NUMBER_FORMAT.parse(matcher.group("count")).toInt()
						if (counter.isEmpty() || counter.peekLast().leftInt() != count) {
							counter.offer(IntLongPair.of(count, System.currentTimeMillis()))
						}
					} catch (e: ParseException) {
						LOGGER.error("[Skyblocker Farming HUD] Failed to parse counter", e)
					}
				}

				FarmingHudWidget.Companion.INSTANCE.update()
				FarmingHudWidget.Companion.INSTANCE.render(context!!, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground)
			}
		})
		ClientPlayerBlockBreakEvents.AFTER.register(ClientPlayerBlockBreakEvents.After { world: ClientWorld?, player: ClientPlayerEntity?, pos: BlockPos?, state: BlockState? ->
			if (shouldRender()) {
				blockBreaks.enqueue(System.currentTimeMillis())
			}
		})
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message: Text, overlay: Boolean ->
			if (shouldRender() && overlay) {
				val matcher = FARMING_XP.matcher(message.string)
				if (matcher.matches()) {
					try {
						farmingXp.offer(FloatLongPair.of(NUMBER_FORMAT.parse(matcher.group("xp")).toFloat(), System.currentTimeMillis()))
						farmingXpPercentProgress = NUMBER_FORMAT.parse(matcher.group("percent")).toFloat()
					} catch (e: ParseException) {
						LOGGER.error("[Skyblocker Farming HUD] Failed to parse farming xp", e)
					}
				}
			}
		})
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(
					ClientCommandManager.literal("hud").then(ClientCommandManager.literal("farming")
						.executes(queueOpenScreenCommand { FarmingHudConfigScreen(null) })
					)
				)
			)
		})
	}

	private fun shouldRender(): Boolean {
		return SkyblockerConfigManager.get().farming.garden.farmingHud.enableHud && location == Location.GARDEN
	}

	fun counter(): Int {
		return if (counter.isEmpty()) 0 else counter.peekLast().leftInt()
	}

	fun cropsPerMinute(): Float {
		if (counter.isEmpty()) {
			return 0
		}
		val first = counter.peek()
		val last = counter.peekLast()
		return (last.leftInt() - first.leftInt()).toFloat() / (last.rightLong() - first.rightLong()) * 60000f
	}

	fun blockBreaks(): Int {
		return blockBreaks.size()
	}

	fun farmingXpPercentProgress(): Float {
		return farmingXpPercentProgress
	}

	fun farmingXpPerHour(): Double {
		return farmingXp.stream().mapToDouble { obj: FloatLongPair -> obj.leftFloat().toDouble() }.sum() * blockBreaks() * 1800 // Hypixel only sends xp updates around every half a second
	}
}
