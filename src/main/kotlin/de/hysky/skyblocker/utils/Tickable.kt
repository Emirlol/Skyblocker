package de.hysky.skyblocker.utils

import net.minecraft.client.MinecraftClient

fun interface Tickable {
	fun tick(client: MinecraftClient?)
}
