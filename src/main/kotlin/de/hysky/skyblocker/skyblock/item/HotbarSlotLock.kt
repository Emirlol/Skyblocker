package de.hysky.skyblocker.skyblock.item

import de.hysky.skyblocker.config.SkyblockerConfigManager
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object HotbarSlotLock {
	var hotbarSlotLock: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.hotbarSlotLock", GLFW.GLFW_KEY_H, "key.categories.skyblocker"))

	fun isLocked(slot: Int) = SkyblockerConfigManager.config.general.lockedSlots.contains(slot)

	fun handleInputEvents(player: ClientPlayerEntity) {
		while (hotbarSlotLock.wasPressed()) {
			val lockedSlots = SkyblockerConfigManager.config.general.lockedSlots
			val selected = player.inventory.selectedSlot
			if (!isLocked(player.inventory.selectedSlot)) lockedSlots.add(selected)
			else lockedSlots.remove(selected)
			SkyblockerConfigManager.save()
		}
	}
}