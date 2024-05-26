package de.hysky.skyblocker.utils

import net.minecraft.network.ClientConnection
import net.minecraft.text.Text

class ReconfiguringPlaceholderScreen(private val connection: ClientConnection) : BasePlaceholderScreen(Text.translatable("connect.reconfiguring")) {
	override fun tick() {
		if (connection.isOpen) {
			connection.tick()
		} else {
			connection.handleDisconnection()
		}
	}
}
