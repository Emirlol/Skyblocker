package de.hysky.skyblocker.skyblock.item

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.util.UndashedUuid
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.item.MuseumItemCache.ProfileMuseumData
import de.hysky.skyblocker.utils.Http.sendHypixelRequest
import de.hysky.skyblocker.utils.Utils.profileId
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtSizeTracker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function

object MuseumItemCache {
	private val LOGGER: Logger = LoggerFactory.getLogger(MuseumItemCache::class.java)
	private val CACHE_FILE: Path = SkyblockerMod.CONFIG_DIR.resolve("museum_item_cache.json")
	private val MUSEUM_ITEM_CACHE: MutableMap<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> = Object2ObjectOpenHashMap()
	private const val ERROR_LOG_TEMPLATE = "[Skyblocker] Failed to refresh museum item data for profile {}"

	private var loaded: CompletableFuture<Void>? = null

	fun init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { obj: MinecraftClient? -> load() })
	}

	private fun load(client: MinecraftClient) {
		loaded = CompletableFuture.runAsync {
			try {
				Files.newBufferedReader(CACHE_FILE).use { reader ->
					val cachedData = ProfileMuseumData.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow()
					MUSEUM_ITEM_CACHE.putAll(cachedData)
					LOGGER.info("[Skyblocker] Loaded museum items cache")
				}
			} catch (ignored: NoSuchFileException) {
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to load cached museum items", e)
			}
		}
	}

	private fun save() {
		CompletableFuture.runAsync {
			try {
				Files.newBufferedWriter(CACHE_FILE).use { writer ->
					SkyblockerMod.GSON.toJson(ProfileMuseumData.SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, MUSEUM_ITEM_CACHE).getOrThrow(), writer)
				}
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to save cached museum items!", e)
			}
		}
	}

	private fun updateData4ProfileMember(uuid: String, profileId: String) {
		CompletableFuture.runAsync {
			try {
				sendHypixelRequest("skyblock/museum", "?profile=$profileId").use { response ->
					//The request was successful
					if (response.ok()) {
						val profileData = JsonParser.parseString(response.content).asJsonObject
						val members = profileData.getAsJsonObject("members")

						if (members.has(uuid)) {
							val memberData = members[uuid].asJsonObject

							//We call them sets because it could either be a singular item or an entire armour set
							val donatedSets = memberData["items"].asJsonObject.asMap()

							//Set of all found item ids on profile
							val itemIds = ObjectOpenHashSet<String>()

							for ((_, value) in donatedSets) {
								//Item is plural here because the nbt is a list
								val itemsData = value.asJsonObject["items"].asJsonObject["data"].asString
								val items = NbtIo.readCompressed(ByteArrayInputStream(Base64.getDecoder().decode(itemsData)), NbtSizeTracker.ofUnlimitedBytes()).getList("i", NbtElement.COMPOUND_TYPE.toInt())

								for (i in items.indices) {
									val tag = items.getCompound(i).getCompound("tag")

									if (tag.contains("ExtraAttributes")) {
										val extraAttributes = tag.getCompound("ExtraAttributes")

										if (extraAttributes.contains("id")) itemIds.add(extraAttributes.getString("id"))
									}
								}
							}

							MUSEUM_ITEM_CACHE[uuid]!![profileId] = ProfileMuseumData(System.currentTimeMillis(), itemIds)
							save()

							LOGGER.info("[Skyblocker] Successfully updated museum item cache for profile {}", profileId)
						} else {
							//If the player's Museum API is disabled
							putEmpty(uuid, profileId)

							LOGGER.warn(ERROR_LOG_TEMPLATE + " because the Museum API is disabled!", profileId)
						}
					} else {
						//If the request returns a non 200 status code
						putEmpty(uuid, profileId)

						LOGGER.error(ERROR_LOG_TEMPLATE + " because a non 200 status code was encountered! Status Code: {}", profileId, response.statusCode)
					}
				}
			} catch (e: Exception) {
				//If an exception was somehow thrown
				putEmpty(uuid, profileId)

				LOGGER.error(ERROR_LOG_TEMPLATE, profileId, e)
			}
		}
	}

	private fun putEmpty(uuid: String, profileId: String) {
		MUSEUM_ITEM_CACHE[uuid]!![profileId] = ProfileMuseumData(System.currentTimeMillis(), ObjectOpenHashSet.of())
		save()
	}

	/**
	 * The cache is ticked upon switching skyblock servers
	 */
	fun tick(profileId: String) {
		if (loaded!!.isDone) {
			val uuid = UndashedUuid.toString(MinecraftClient.getInstance().session.uuidOrNull)
			val playerData: MutableMap<String, ProfileMuseumData> = MUSEUM_ITEM_CACHE.computeIfAbsent(uuid) { _uuid: String? -> Object2ObjectOpenHashMap() }
			playerData.putIfAbsent(profileId, ProfileMuseumData.EMPTY)

			if (playerData[profileId]!!.stale()) updateData4ProfileMember(uuid, profileId)
		}
	}

	@JvmStatic
	fun hasItemInMuseum(id: String): Boolean {
		val uuid = UndashedUuid.toString(MinecraftClient.getInstance().session.uuidOrNull)
		val collectedItemIds = if ((!MUSEUM_ITEM_CACHE.containsKey(uuid) || profileId.isBlank() || !MUSEUM_ITEM_CACHE[uuid]!!.containsKey(profileId))) null else MUSEUM_ITEM_CACHE[uuid]!![profileId]!!.collectedItemIds

		return collectedItemIds != null && collectedItemIds.contains(id)
	}

	@JvmRecord
	private data class ProfileMuseumData(val lastUpdated: Long, val collectedItemIds: ObjectOpenHashSet<String>?) {
		fun stale(): Boolean {
			return System.currentTimeMillis() > lastUpdated + MAX_AGE
		}

		companion object {
			val EMPTY: ProfileMuseumData = ProfileMuseumData(0L, null)
			private const val MAX_AGE: Long = 86400000
			private val CODEC: Codec<ProfileMuseumData?> = RecordCodecBuilder.create<ProfileMuseumData?> { instance: RecordCodecBuilder.Instance<ProfileMuseumData?> ->
				instance.group<Long, ObjectOpenHashSet<String>>(
					Codec.LONG.fieldOf("lastUpdated").forGetter<ProfileMuseumData?>(ProfileMuseumData::lastUpdated),
					Codec.STRING.listOf()
						.xmap<ObjectOpenHashSet<String>>(Function<List<String?>, ObjectOpenHashSet<String>> { c: List<String?>? -> ObjectOpenHashSet(c) }, Function<ObjectOpenHashSet<String>, List<String>> { c: ObjectOpenHashSet<String>? -> ObjectArrayList(c) })
						.fieldOf("collectedItemIds")
						.forGetter<ProfileMuseumData> { i: ProfileMuseumData -> ObjectOpenHashSet<String>(i.collectedItemIds) }
				).apply<ProfileMuseumData?>(instance) { lastUpdated: Long, collectedItemIds: ObjectOpenHashSet<String>? -> ProfileMuseumData(lastUpdated, collectedItemIds) }
			}

			//Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
			val SERIALIZATION_CODEC: Codec<Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>> = Codec.unboundedMap<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>(
				Codec.STRING, Codec.unboundedMap<String, ProfileMuseumData?>(Codec.STRING, CODEC)
					.xmap<Object2ObjectOpenHashMap<String, ProfileMuseumData>>(Function<Map<String?, ProfileMuseumData?>, Object2ObjectOpenHashMap<String, ProfileMuseumData>> { m: Map<String?, ProfileMuseumData?>? -> Object2ObjectOpenHashMap(m) }, Function<Object2ObjectOpenHashMap<String?, ProfileMuseumData?>, Map<String, ProfileMuseumData>> { m: Object2ObjectOpenHashMap<String?, ProfileMuseumData?>? -> Object2ObjectOpenHashMap(m) })
			).xmap<Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>>(Function<Map<String?, Object2ObjectOpenHashMap<String?, ProfileMuseumData?>?>, Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>> { m: Map<String?, Object2ObjectOpenHashMap<String?, ProfileMuseumData?>?>? -> Object2ObjectOpenHashMap(m) }, Function<Map<String?, Object2ObjectOpenHashMap<String?, ProfileMuseumData?>?>, Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>> { m: Map<String?, Object2ObjectOpenHashMap<String?, ProfileMuseumData?>?>? -> Object2ObjectOpenHashMap(m) })
		}
	}
}