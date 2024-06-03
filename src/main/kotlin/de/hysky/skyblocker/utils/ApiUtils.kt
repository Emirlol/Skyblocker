package de.hysky.skyblocker.utils

import com.google.gson.JsonParser
import com.mojang.util.UndashedUuid
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient

/*
* Contains only basic helpers for using Http APIs
*/
object ApiUtils {
	/**
	 * Do not iterate over this map, it will be accessed and modified by multiple threads.
	 */
	private val NAME_2_UUID_CACHE = Object2ObjectOpenHashMap<String, String>()

	fun init() {
		//Clear cache every 20 minutes
		Scheduler.scheduleCyclic(24000, true) { NAME_2_UUID_CACHE.clear() }
	}

	/**
	 * Multithreading is to be handled by the method caller
	 */
	context(CoroutineScope)
	suspend fun name2Uuid(name: String): String? = name2Uuid(name, 0)

	context(CoroutineScope)
	private suspend fun name2Uuid(name: String, retries: Int): String? {
		val session = MinecraftClient.getInstance().session

		if (session.username == name) return UndashedUuid.toString(session.uuidOrNull)
		if (NAME_2_UUID_CACHE.containsKey(name)) return NAME_2_UUID_CACHE[name]

		try {
			Http.sendName2UuidRequest(name).use { response ->
				if (response.ok()) {
					val uuid = JsonParser.parseString(response.content).asJsonObject["id"].asString

					NAME_2_UUID_CACHE[name] = uuid

					return uuid
				} else if (response.ratelimited() && retries < 3) {
					delay(800)

					return name2Uuid(name, retries + 1)
				}
			}
		} catch (e: Exception) {
			TextHandler.error("Name to uuid lookup failed! Name: $name", e)
		}

		return null
	}
}
