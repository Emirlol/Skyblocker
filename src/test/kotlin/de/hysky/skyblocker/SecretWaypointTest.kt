package de.hysky.skyblocker

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SecretWaypointTest {
	private val gson = Gson()

	@Test
	fun testCodecSerialize() {
		val waypoint = SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos(-1, 0, 1))
		val json: JsonElement = SecretWaypoint.CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, waypoint).getOrThrow()
		val expectedJson = "{\"secretIndex\":0,\"category\":\"default\",\"name\":\"name\",\"pos\":[-1,0,1]}"

		Assertions.assertEquals(expectedJson, json.toString())
	}

	@Test
	fun testCodecDeserialize() {
		val json = "{\"secretIndex\":0,\"category\":\"default\",\"name\":\"name\",\"pos\":[-1,0,1]}"
		val waypoint: SecretWaypoint = SecretWaypoint.CODEC.parse<JsonElement>(JsonOps.INSTANCE, gson.fromJson<JsonElement>(json, JsonElement::class.java)).getOrThrow()
		val expectedWaypoint = SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos(-1, 0, 1))

		Assertions.assertEquals(expectedWaypoint, waypoint)
	}

	@Test
	fun testListCodecSerialize() {
		val waypoints = java.util.List.of<SecretWaypoint>(SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos(-1, 0, 1)), SecretWaypoint(1, SecretWaypoint.Category.CHEST, "name", BlockPos(2, 0, -2)))
		val json: JsonElement = SecretWaypoint.LIST_CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, waypoints).getOrThrow()
		val expectedJson = "[{\"secretIndex\":0,\"category\":\"default\",\"name\":\"name\",\"pos\":[-1,0,1]},{\"secretIndex\":1,\"category\":\"chest\",\"name\":\"name\",\"pos\":[2,0,-2]}]"

		Assertions.assertEquals(expectedJson, json.toString())
	}

	@Test
	fun testListCodecDeserialize() {
		val json = "[{\"secretIndex\":0,\"category\":\"default\",\"name\":\"name\",\"pos\":[-1,0,1]},{\"secretIndex\":1,\"category\":\"chest\",\"name\":\"name\",\"pos\":[2,0,-2]}]"
		val waypoints: List<SecretWaypoint> = SecretWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, gson.fromJson<JsonElement>(json, JsonElement::class.java)).getOrThrow()
		val expectedWaypoints = listOf(SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos(-1, 0, 1)), SecretWaypoint(1, SecretWaypoint.Category.CHEST, "name", BlockPos(2, 0, -2)))

		Assertions.assertEquals(expectedWaypoints.size, waypoints.size)
		for (i in expectedWaypoints.indices) {
			val expectedWaypoint = expectedWaypoints[i]
			val waypoint = waypoints[i]
			Assertions.assertEquals(expectedWaypoint, waypoint)
		}
	}

	@Test
	fun testGetCategory() {
		val waypointJson = JsonObject()
		waypointJson.addProperty("category", "chest")
		val category: SecretWaypoint.Category = SecretWaypoint.Category.get(waypointJson)
		Assertions.assertEquals(SecretWaypoint.Category.CHEST, category)
	}

	@Test
	fun testGetCategoryDefault() {
		val waypointJson = JsonObject()
		waypointJson.addProperty("category", "")
		val category: SecretWaypoint.Category = SecretWaypoint.Category.get(waypointJson)
		Assertions.assertEquals(SecretWaypoint.Category.DEFAULT, category)
	}

	companion object {
		@BeforeAll
		fun setup() {
			SharedConstants.createGameVersion()
			Bootstrap.initialize()
		}
	}
}
