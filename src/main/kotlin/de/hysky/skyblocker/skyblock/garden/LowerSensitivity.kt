package de.hysky.skyblocker.skyblock.garden

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import de.hysky.skyblocker.utils.Location
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.location
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld

object LowerSensitivity {
	var isSensitivityLowered: Boolean = false
		private set

	fun init() {
		ClientTickEvents.END_WORLD_TICK.register(ClientTickEvents.EndWorldTick { world: ClientWorld? ->
			if (!isOnSkyblock || location != Location.GARDEN || MinecraftClient.getInstance().player == null) {
				if (isSensitivityLowered) lowerSensitivity(false)
				return@register
			}
			if (SkyblockerConfigManager.config.farming.garden.lockMouseTool) {
				val mainHandStack = MinecraftClient.getInstance().player!!.mainHandStack
				val itemId = getItemId(mainHandStack)
				val shouldLockMouse = FarmingHudWidget.Companion.FARMING_TOOLS.containsKey(itemId) && (!SkyblockerConfigManager.config.farming.garden.lockMouseGroundOnly || MinecraftClient.getInstance().player!!.isOnGround)
				if (shouldLockMouse && !isSensitivityLowered) lowerSensitivity(true)
				else if (!shouldLockMouse && isSensitivityLowered) lowerSensitivity(false)
			}
		})
	}

	fun lowerSensitivity(lowerSensitivity: Boolean) {
		if (isSensitivityLowered == lowerSensitivity) return
		isSensitivityLowered = lowerSensitivity
	}
}
