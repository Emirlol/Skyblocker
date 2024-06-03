package de.hysky.skyblocker.utils

import com.google.gson.JsonObject
import de.hysky.skyblocker.SkyblockerMod
import it.unimi.dsi.fastutil.objects.ObjectLongPair
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.minecraft.client.MinecraftClient

object ProfileUtils {
	private const val HYPIXEL_API_COOLDOWN: Long = 300_000 // 5min = 300_000

	var players: MutableMap<String, ObjectLongPair<JsonObject>> = HashMap()

	fun init() {
		updateProfile()
	}

	fun updateProfile(name: String = MinecraftClient.getInstance().session.username): Deferred<JsonObject?> {
		val playerCache = players[name]
		if (playerCache != null && playerCache.rightLong() + HYPIXEL_API_COOLDOWN > System.currentTimeMillis()) {
			return CompletableDeferred(playerCache.left())
		}

		return SkyblockerMod.globalJob.async {
			val uuid = ApiUtils.name2Uuid(name)
			try {
				Http.sendHypixelRequest("skyblock/profiles", "?uuid=$uuid").use { response ->
					check(response.ok()) { "Failed to get profile uuid for players " + name + "! Response: " + response.content }

					val player = SkyblockerMod.GSON.fromJson(response.content, JsonObject::class.java)
						.getAsJsonArray("profiles")
						.asSequence()
						.map { it.asJsonObject }
						.firstOrNull { profile -> profile.getAsJsonPrimitive("selected").asBoolean }
						?.getAsJsonObject("members")
						?.get(uuid)
						?.asJsonObject ?: throw IllegalStateException("No selected profile found!?")

					players[name] = ObjectLongPair.of(player, System.currentTimeMillis())
					return@async player
				}
			} catch (e: Exception) {
				TextHandler.error("[Profile Utils] Failed to get Player Profile Data for players $name, is the API Down/Limited?", e)
			}
			null
		}
	}
}
