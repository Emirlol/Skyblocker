package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilled
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

object TeleportOverlay {
	private val COLOR_COMPONENTS = floatArrayOf(118f / 255f, 21f / 255f, 148f / 255f)
	private val client: MinecraftClient = MinecraftClient.getInstance()

	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
	}

	private fun render(wrc: WorldRenderContext) {
		if (isOnSkyblock && SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableTeleportOverlays && client.player != null && client.world != null) {
			val heldItem = client.player!!.mainHandStack
			val itemId = ItemTooltip.getInternalNameFromNBT(heldItem, true)
			val customData = getCustomData(heldItem)

			if (itemId != null) {
				when (itemId) {
					"ASPECT_OF_THE_LEECH_1" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
							render(wrc, 3)
						}
					}

					"ASPECT_OF_THE_LEECH_2" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
							render(wrc, 4)
						}
					}

					"ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableEtherTransmission && client.options.sneakKey.isPressed && customData != null && customData.getInt("ethermerge") == 1) {
							render(wrc, customData, 57)
						} else if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableInstantTransmission) {
							render(wrc, customData, 8)
						}
					}

					"ETHERWARP_CONDUIT" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableEtherTransmission) {
							render(wrc, customData, 57)
						}
					}

					"SINSEEKER_SCYTHE" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission) {
							render(wrc, customData, 4)
						}
					}

					"NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
						if (SkyblockerConfigManager.config.uiAndVisuals.teleportOverlay.enableWitherImpact) {
							render(wrc, 10)
						}
					}
				}
			}
		}
	}

	/**
	 * Renders the teleport overlay with a given base range and the tuned transmission stat.
	 */
	private fun render(wrc: WorldRenderContext, customData: NbtCompound?, baseRange: Int) {
		render(wrc, if (customData != null && customData.contains("tuned_transmission")) baseRange + customData.getInt("tuned_transmission") else baseRange)
	}

	/**
	 * Renders the teleport overlay with a given range. Uses [MinecraftClient.crosshairTarget] if it is a block and within range. Otherwise, raycasts from the player with the given range.
	 *
	 * @implNote [MinecraftClient.player] and [MinecraftClient.world] must not be null when calling this method.
	 */
	private fun render(wrc: WorldRenderContext, range: Int) {
		if (client.crosshairTarget != null && client.crosshairTarget!!.type == HitResult.Type.BLOCK && client.crosshairTarget is BlockHitResult && client.crosshairTarget.squaredDistanceTo(client.player) < range * range) {
			render(wrc, blockHitResult)
		} else if (client.interactionManager != null && range > client.player!!.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE)!!.value) {
			val result = client.player!!.raycast(range.toDouble(), wrc.tickDelta(), false)
			if (result.type == HitResult.Type.BLOCK && result is BlockHitResult) {
				render(wrc, result)
			}
		}
	}

	/**
	 * Renders the teleport overlay at the given [BlockHitResult].
	 *
	 * @implNote [MinecraftClient.world] must not be null when calling this method.
	 */
	private fun render(wrc: WorldRenderContext, blockHitResult: BlockHitResult) {
		val pos = blockHitResult.blockPos
		val state = client.world!!.getBlockState(pos)
		if (!state.isAir && client.world!!.getBlockState(pos.up()).isAir && client.world!!.getBlockState(pos.up(2)).isAir) {
			renderFilled(wrc, pos, COLOR_COMPONENTS, 0.5f, false)
		}
	}
}
