package de.hysky.skyblocker.skyblock.dungeon.secrets

import net.minecraft.datafixer.fix.ItemIdFix
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * Utility class to convert the old dungeon rooms data from Dungeon Rooms Mod to a new format.
 * The new format is similar to [DRM's format](https://quantizr.github.io/posts/how-it-works/), but uses ints instead of longs and a custom numeric block id to store the block states.
 * The first byte is the x position, the second byte is the y position, the third byte is the z position, and the fourth byte is the custom numeric block id.
 * Use [DungeonManager.NUMERIC_ID] to get the custom numeric block id of a block.
 * Run this manually when updating dungeon rooms data with DRM's data in `src/test/resources/assets/skyblocker/dungeons/dungeonrooms`.
 */
object DungeonRoomsDFU {
	private val LOGGER: Logger = LoggerFactory.getLogger(DungeonRoomsDFU::class.java)
	private const val DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons"
	private const val DUNGEON_ROOMS_DATA_DIR = DUNGEONS_DATA_DIR + "/dungeonrooms"
	private val OLD_ROOMS = HashMap<String, HashMap<String, HashMap<String, LongArray>>>()
	private val ROOMS = HashMap<String, HashMap<String, HashMap<String, IntArray>>>()

	@JvmStatic
	fun main(args: Array<String>) {
		load().join()
		updateRooms()
		save().join()
	}

	private fun load(): CompletableFuture<Void?> {
		val dungeonFutures: MutableList<CompletableFuture<Void>> = ArrayList()
		val dungeonsURL = DungeonRoomsDFU::class.java.getResource(DUNGEON_ROOMS_DATA_DIR)
		if (dungeonsURL == null) {
			LOGGER.error("Failed to load dungeon secrets, unable to find dungeon rooms data directory")
			return CompletableFuture.completedFuture(null)
		}
		val dungeonsDir = Path.of(dungeonsURL.path)
		val resourcePathIndex = dungeonsDir.toString().indexOf(DUNGEON_ROOMS_DATA_DIR)
		try {
			Files.newDirectoryStream(dungeonsDir) { path: Path? -> Files.isDirectory(path) }.use { dungeons ->
				for (dungeon in dungeons) {
					try {
						Files.newDirectoryStream(dungeon) { path: Path? -> Files.isDirectory(path) }.use { roomShapes ->
							val roomShapeFutures: MutableList<CompletableFuture<Void>> = ArrayList()
							val roomShapesMap = HashMap<String, HashMap<String, LongArray>>()
							for (roomShape in roomShapes) {
								roomShapeFutures.add(CompletableFuture.supplyAsync { readRooms(roomShape, resourcePathIndex) }.thenAccept { rooms: HashMap<String, LongArray> -> roomShapesMap[roomShape.fileName.toString().lowercase(Locale.getDefault())] = rooms })
							}
							OLD_ROOMS[dungeon.fileName.toString().lowercase(Locale.getDefault())] = roomShapesMap
							dungeonFutures.add(CompletableFuture.allOf(*roomShapeFutures.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).thenRun { LOGGER.info("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.fileName, roomShapesMap.size, roomShapesMap.values.stream().mapToInt { obj: HashMap<String, LongArray> -> obj.size }.sum()) })
						}
					} catch (e: IOException) {
						LOGGER.error("Failed to load dungeon secrets for dungeon " + dungeon.fileName, e)
					}
				}
			}
		} catch (e: IOException) {
			LOGGER.error("Failed to load dungeon secrets", e)
		}
		return CompletableFuture.allOf(*dungeonFutures.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).thenRun { LOGGER.info("Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", OLD_ROOMS.size, OLD_ROOMS.values.stream().mapToInt { obj: HashMap<String, HashMap<String, LongArray>> -> obj.size }.sum(), OLD_ROOMS.values.stream().map<Collection<HashMap<String, LongArray>>> { obj: HashMap<String, HashMap<String, LongArray>> -> obj.values }.flatMap { obj: Collection<HashMap<String, LongArray>> -> obj.stream() }.mapToInt { obj: HashMap<String, LongArray> -> obj.size }.sum()) }
	}

	private fun readRooms(roomShape: Path, resourcePathIndex: Int): HashMap<String, LongArray>? {
		try {
			Files.newDirectoryStream(roomShape) { path: Path? -> Files.isRegularFile(path) }.use { rooms ->
				val roomsData = HashMap<String, LongArray>()
				for (room in rooms) {
					val name = room.fileName.toString()
					try {
						ObjectInputStream(InflaterInputStream(DungeonRoomsDFU::class.java.getResourceAsStream(room.toString().substring(resourcePathIndex)))).use { `in` ->
							roomsData[name.substring(0, name.length - 9).lowercase(Locale.getDefault())] = `in`.readObject() as LongArray
							LOGGER.info("Loaded dungeon secrets room {}", name)
						}
					} catch (e: NullPointerException) {
						LOGGER.error("Failed to load dungeon secrets room $name", e)
					} catch (e: IOException) {
						LOGGER.error("Failed to load dungeon secrets room $name", e)
					} catch (e: ClassNotFoundException) {
						LOGGER.error("Failed to load dungeon secrets room $name", e)
					}
				}
				LOGGER.info("Loaded dungeon secrets room shape {} with {} rooms", roomShape.fileName, roomsData.size)
				return roomsData
			}
		} catch (e: IOException) {
			LOGGER.error("Failed to load dungeon secrets room shape " + roomShape.fileName, e)
		}
		return null
	}

	private fun updateRooms() {
		for ((key, value) in OLD_ROOMS) {
			val dungeon = HashMap<String, HashMap<String, IntArray>>()
			for ((key1, value1) in value) {
				val roomShape = HashMap<String, IntArray>()
				for ((key2, value2) in value1) {
					roomShape[key2.replace(" ".toRegex(), "-")] = updateRoom(value2)
				}
				dungeon[key1] = roomShape
			}
			ROOMS[key] = dungeon
		}
	}

	private fun updateRoom(oldRoom: LongArray): IntArray {
		val room = IntArray(oldRoom.size)
		for (i in oldRoom.indices) {
			room[i] = updateBlock(oldRoom[i])
		}
		// Technically not needed, as the long array should be sorted already.
		Arrays.sort(room)
		return room
	}

	/**
	 * Updates the block state from Dungeon Rooms Mod's format to the new format explained in [DungeonRoomsDFU].
	 *
	 * @param oldBlock the old block state in DRM's format
	 * @return the new block state in the new format
	 */
	private fun updateBlock(oldBlock: Long): Int {
		val x = (oldBlock shr 48 and 0xFFFFL).toShort()
		val y = (oldBlock shr 32 and 0xFFFFL).toShort()
		val z = (oldBlock shr 16 and 0xFFFFL).toShort()
		// Blocks should be within the range 0 to 256, since a dungeon room is at most around 128 blocks long and around 150 blocks tall.
		require(!(x < 0 || x > 0xFF || y < 0 || y > 0xFF || z < 0 || z > 0xFF)) { "Invalid block: $oldBlock" }
		val oldId = (oldBlock and 0xFFFFL).toShort()
		// Get the new id for the block.
		var newId = ItemInstanceTheFlatteningFix.getItem(ItemIdFix.fromId(oldId / 100), oldId % 100)
		if (newId == null) {
			newId = ItemIdFix.fromId(oldId / 100)
		}
		return x.toInt() shl 24 or (y.toInt() shl 16) or (z.toInt() shl 8) or DungeonManager.NUMERIC_ID.getByte(newId).toInt()
	}

	private fun save(): CompletableFuture<Void> {
		val dungeonFutures: MutableList<CompletableFuture<Void>> = ArrayList()
		for ((key, value) in ROOMS) {
			val dungeonDir = Path.of("out", "dungeons", key)
			val roomShapeFutures: MutableList<CompletableFuture<Void>> = ArrayList()
			for (roomShape in value.entries) {
				val roomShapeDir = dungeonDir.resolve(roomShape.key)
				roomShapeFutures.add(CompletableFuture.runAsync { saveRooms(roomShapeDir, roomShape) })
			}
			dungeonFutures.add(CompletableFuture.allOf(*roomShapeFutures.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).thenRun { LOGGER.info("Saved dungeon secrets for dungeon {} with {} room shapes and {} rooms total", key, value.size, value.values.stream().mapToInt { obj: HashMap<String, IntArray> -> obj.size }.sum()) })
		}
		return CompletableFuture.allOf(*dungeonFutures.toArray<CompletableFuture<*>> { _Dummy_.__Array__() }).thenRun { LOGGER.info("Saved dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS.size, ROOMS.values.stream().mapToInt { obj: HashMap<String, HashMap<String, IntArray>> -> obj.size }.sum(), ROOMS.values.stream().map<Collection<HashMap<String, IntArray>>> { obj: HashMap<String, HashMap<String, IntArray>> -> obj.values }.flatMap { obj: Collection<HashMap<String, IntArray>> -> obj.stream() }.mapToInt { obj: HashMap<String, IntArray> -> obj.size }.sum()) }
	}

	private fun saveRooms(roomShapeDir: Path, roomShape: Map.Entry<String, HashMap<String, IntArray>>) {
		try {
			Files.createDirectories(roomShapeDir)
		} catch (e: IOException) {
			LOGGER.error("Failed to save dungeon secrets: failed to create dungeon secrets room shape directory $roomShapeDir", e)
		}
		for ((key, value) in roomShape.value) {
			try {
				ObjectOutputStream(DeflaterOutputStream(Files.newOutputStream(roomShapeDir.resolve("$key.skeleton")))).use { out ->
					out.writeObject(value)
					LOGGER.info("Saved dungeon secrets room {}", key)
				}
			} catch (e: IOException) {
				LOGGER.error("Failed to save dungeon secrets room $key", e)
			}
		}
		LOGGER.info("Saved dungeon secrets room shape {} with {} rooms", roomShape.key, roomShape.value.size)
	}
}
