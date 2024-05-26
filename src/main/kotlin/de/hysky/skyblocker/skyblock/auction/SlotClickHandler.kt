package de.hysky.skyblocker.skyblock.auction

fun interface SlotClickHandler {
	@JvmOverloads
	fun click(slot: Int, button: Int = 0)
}
