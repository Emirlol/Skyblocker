package de.hysky.skyblocker.skyblock.auction

import de.hysky.skyblocker.mixins.accessors.SlotAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot

class AuctionHouseScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, playerInventory: PlayerInventory?, inventory: Inventory?, rows: Int, isView: Boolean) : GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rows) {
	init {
		val yOffset = (rows - 4) * 18
		// Shift player inventory by 2 pixels and also remove the yOffset
		for (i in rows * 9 until slots.size) {
			val slot = slots[i]
			val slotAccessor = slot as SlotAccessor
			slotAccessor.setY(slot.y + 2 - yOffset)
		}
		// disable ALL THE OTHER SLOTS MWAHAHAHA and also move the good ones around and stuff
		for (i in 0 until rows * 9) {
			val lineI = i % 9
			val slot = slots[i]
			if (!isView && i > 9 && i < (rows - 1) * 9 && lineI > 1 && lineI < 8) {
				val miniInventorySlot = lineI - 2 + (i / 9 - 1) * 6
				val slotAccessor = slot as SlotAccessor
				slotAccessor.setX(8 + miniInventorySlot % 8 * 18)
				slotAccessor.setY(18 + miniInventorySlot / 8 * 18)
			} else {
				slots[i] = object : Slot(slot.inventory, slot.index, slot.x, slot.y) {
					override fun isEnabled(): Boolean {
						return false
					}
				}
			}
		}
	}

	override fun setStackInSlot(slot: Int, revision: Int, stack: ItemStack) {
		super.setStackInSlot(slot, revision, stack)
		onContentChanged(slots[slot].inventory)
	}

	companion object {
		@JvmStatic
		fun of(original: GenericContainerScreenHandler, isView: Boolean): AuctionHouseScreenHandler {
			checkNotNull(MinecraftClient.getInstance().player)
			return AuctionHouseScreenHandler(
				original.type,
				original.syncId,
				MinecraftClient.getInstance().player!!.inventory,
				original.inventory,
				original.rows,
				isView
			)
		}
	}
}
