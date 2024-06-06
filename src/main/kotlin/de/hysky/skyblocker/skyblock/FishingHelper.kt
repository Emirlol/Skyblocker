package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import de.hysky.skyblocker.utils.render.title.Title
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.abs

object FishingHelper {
	private val title = Title("skyblocker.fishing.reelNow", Formatting.GREEN)
	private var startTime: Long = 0
	private var startTimeFish: Long = 0
	private var normalYawVector: Vec3d? = null

	fun init() {
		UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, world: World?, hand: Hand? ->
			val stack = player.getStackInHand(hand)
			if (!isOnSkyblock) {
				return@register TypedActionResult.pass<ItemStack>(stack)
			}
			if (stack.item is FishingRodItem) {
				if (player.fishHook == null) {
					start(player)
				} else {
					reset()
				}
			}
			TypedActionResult.pass<ItemStack>(stack)
		})
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
	}

	fun start(player: PlayerEntity) {
		startTime = System.currentTimeMillis()
		startTimeFish = System.currentTimeMillis()
		val yawRad = player.yaw * 0.017453292f
		normalYawVector = Vec3d(-MathHelper.sin(yawRad).toDouble(), 0.0, MathHelper.cos(yawRad).toDouble())
	}

	fun reset() {
		startTime = 0
		startTimeFish = 0
	}

	fun resetFish() {
		startTimeFish = 0
	}

	@JvmStatic
    fun onSound(packet: PlaySoundS2CPacket) {
		val path = packet.sound.value().id.path
		if (SkyblockerConfigManager.config.helpers.fishing.enableFishingHelper && startTimeFish != 0L && System.currentTimeMillis() >= startTimeFish + 2000 && ("entity.generic.splash" == path || "entity.player.splash" == path)) {
			val player = MinecraftClient.getInstance().player
			if (player?.fishHook != null) {
				val soundToFishHook = player.fishHook!!.pos.subtract(packet.x, 0.0, packet.z)
				if (abs(normalYawVector!!.x * soundToFishHook.z - normalYawVector!!.z * soundToFishHook.x) < 0.2 && abs(normalYawVector!!.dotProduct(soundToFishHook)) < 4.0 && player.pos.squaredDistanceTo(packet.x, packet.y, packet.z) > 1.0) {
					displayInTitleContainerAndPlaySound(title, 10)
					resetFish()
				}
			} else {
				reset()
			}
		}
	}

	fun render(context: WorldRenderContext?) {
		if (SkyblockerConfigManager.config.helpers.fishing.enableFishingTimer && startTime != 0L) {
			val player = MinecraftClient.getInstance().player
			if (player?.fishHook != null) {
				val time = ((System.currentTimeMillis() - startTime) / 100f).toInt() / 10f //leave 1dp in seconds
				val scale = SkyblockerConfigManager.config.helpers.fishing.fishingTimerScale
				val pos = player.fishHook!!.pos.add(0.0, 0.4 + scale / 10, 0.0)
				val text: Text = if (time >= 20 && SkyblockerConfigManager.config.helpers.fishing.changeTimerColor) {
					Text.literal(time.toString()).formatted(Formatting.GREEN)
				} else {
					Text.literal(time.toString())
				}

				renderText(context!!, text, pos, scale, true)
			}
		}
	}
}
