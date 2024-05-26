package de.hysky.skyblocker.skyblock.item

import de.hysky.skyblocker.utils.Utils.islandArea
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import java.util.*

class SkyblockCraftingTableScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, playerInventory: PlayerInventory?, inventory: Inventory?, rows: Int) : GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rows) {
	val mirrorverse: Boolean = islandArea.lowercase(Locale.getDefault()).contains("mirrorverse")

	init {
		val activeSlots = if (mirrorverse) riftNormalSlots else normalSlots

		for (i in 0 until rows * 9) {
			val originalSlot = slots[i]
			if (Arrays.binarySearch(activeSlots, i) >= 0) {
				val coords = getCoords(i)
				val slot = Slot(originalSlot.inventory, originalSlot.index, coords[0], coords[1])
				slot.id = i
				slots[i] = slot
			} else {
				val slot = DisabledSlot(originalSlot.inventory, originalSlot.index, originalSlot.x, originalSlot.y)
				slot.id = i
				slots[i] = slot
			}
		}
		val yOffset = (rows - 4) * 18 + 19
		for (i in rows * 9 until slots.size) {
			val originalSlot = slots[i]
			val slot = Slot(originalSlot.inventory, originalSlot.index, originalSlot.x, originalSlot.y - yOffset)
			slot.id = i
			slots[i] = slot
		}
	}

	constructor(handler: GenericContainerScreenHandler, playerInventory: PlayerInventory?) : this(handler.type, handler.syncId, playerInventory, handler.inventory, handler.rows)

	private fun getCoords(slot: Int): IntArray {
		if (mirrorverse) {
			if (slot == 24) return intArrayOf(124, 35)
			val gridX = slot % 9 - 2
			val gridY = slot / 9 - 1
			return intArrayOf(30 + gridX * 18, 17 + gridY * 18)
		} else {
			if (slot == 23) return intArrayOf(124, 35)
			if (slot == 16 || slot == 25 || slot == 34) {
				val y = (slot / 9 - 1) * 18 + 8
				return intArrayOf(174, y)
			}
			val gridX = slot % 9 - 1
			val gridY = slot / 9 - 1
			return intArrayOf(30 + gridX * 18, 17 + gridY * 18)
		}
	}

	class DisabledSlot(inventory: Inventory?, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
		override fun isEnabled(): Boolean {
			return false
		}
	}

	companion object {
		private val normalSlots = intArrayOf(
			10, 11, 12, 16,
			19, 20, 21, 23, 25,
			28, 29, 30, 34
		)

		private val riftNormalSlots = intArrayOf(
			11, 12, 13,
			20, 21, 22, 24,
			29, 30, 31
		)
	}
}
