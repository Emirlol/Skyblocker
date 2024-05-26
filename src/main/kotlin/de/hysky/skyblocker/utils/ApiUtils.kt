package de.hysky.skyblocker.utils

import com.google.gson.JsonParser
import com.mojang.util.UndashedUuid
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
* Contains only basic helpers for using Http APIs
*/
object ApiUtils {
	private val LOGGER: Logger = LoggerFactory.getLogger(ApiUtils::class.java)

	/**
	 * Do not iterate over this map, it will be accessed and modified by multiple threads.
	 */
	private val NAME_2_UUID_CACHE = Object2ObjectOpenHashMap<String, String>()

	fun init() {
		//Clear cache every 20 minutes
		Scheduler.INSTANCE.scheduleCyclic({ NAME_2_UUID_CACHE.clear() }, 24000, true)
	}

	/**
	 * Multithreading is to be handled by the method caller
	 */
	@JvmStatic
	fun name2Uuid(name: String): String? {
		return name2Uuid(name, 0)
	}

	private fun name2Uuid(name: String, retries: Int): String? {
		var retries = retries
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
					Thread.sleep(800)

					return name2Uuid(name, ++retries)
				}
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker] Name to uuid lookup failed! Name: {}", name, e)
		}

		return ""
	}
}
