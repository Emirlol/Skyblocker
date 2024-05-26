package de.hysky.skyblocker.utils.render.gui

import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text

abstract class AbstractCustomHypixelGUI<T : ScreenHandler?>(handler: T, inventory: PlayerInventory?, title: Text?) : HandledScreen<T>(handler, inventory, title), ScreenHandlerListener {
	@JvmField
    var isWaitingForServer: Boolean = true

	init {
		handler!!.addListener(this)
	}

	protected fun clickSlot(slotID: Int, button: Int = 0) {
		if (isWaitingForServer) return
		if (client == null) return
		checkNotNull(client!!.interactionManager)
		client!!.interactionManager!!.clickSlot(handler!!.syncId, slotID, button, SlotActionType.PICKUP, client!!.player)
		handler!!.cursorStack.count = 0
		isWaitingForServer = true
	}

	fun changeHandler(newHandler: AuctionHouseScreenHandler?) {
		handler!!.removeListener(this)
		(this as HandledScreenAccessor).setHandler(newHandler)
		handler!!.addListener(this)
	}

	override fun removed() {
		super.removed()
		handler!!.removeListener(this)
	}

	override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack) {
		onSlotChange(this.handler, slotId, stack)
		isWaitingForServer = false
	}

	protected abstract fun onSlotChange(handler: T?, slotID: Int, stack: ItemStack?)

	override fun onPropertyUpdate(handler: ScreenHandler, property: Int, value: Int) {}
}
