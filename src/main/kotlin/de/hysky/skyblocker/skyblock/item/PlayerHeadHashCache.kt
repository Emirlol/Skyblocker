package de.hysky.skyblocker.skyblock.item

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import de.hysky.skyblocker.utils.Http.sendGetRequest
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

object PlayerHeadHashCache {
	private val LOGGER: Logger = LogUtils.getLogger()
	private val CACHE = IntOpenHashSet()

	fun init() {
		CompletableFuture.runAsync(Runnable { obj: PlayerHeadHashCache? -> loadSkins() })
	}

	private fun loadSkins() {
		try {
			val response = sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/items")
			val items = JsonParser.parseString(response).asJsonObject.getAsJsonArray("items")

			items.asList().stream()
				.map { obj: JsonElement -> obj.asJsonObject }
				.filter { item: JsonObject -> item["material"].asString == "SKULL_ITEM" }
				.filter { item: JsonObject -> item.has("skin") }
				.map { item: JsonObject -> Base64.getDecoder().decode(item["skin"].asString) }
				.map { bytes: ByteArray? -> String(bytes!!) }
				.map { profile: String? -> JsonParser.parseString(profile).asJsonObject }
				.map { profile: JsonObject -> profile.getAsJsonObject("textures").getAsJsonObject("SKIN")["url"].asString }
				.map { obj: String? -> getSkinHash() }
				.mapToInt { obj: String -> obj.hashCode() }
				.forEach { k: Int -> CACHE.add(k) }

			LOGGER.info("[Skyblocker Player Head Hash Cache] Successfully cached the hashes of all player head items!")
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Failed to cache skin hashes!", e)
		}
	}

	//From MinecraftProfileTexture#getHash
	@JvmStatic
	fun getSkinHash(url: String?): String {
		try {
			return FilenameUtils.getBaseName(URI(url).path)
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Malformed Skin URL! URL: {}", url, e)
		}

		return ""
	}

	@JvmStatic
	fun contains(hash: Int): Boolean {
		return CACHE.contains(hash)
	}
}
