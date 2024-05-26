package de.hysky.skyblocker.skyblock.item

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import net.minecraft.util.Identifier
import org.junit.jupiter.api.Assertions

class ArmorTrimIdSerializationTest {
	private val gson = Gson()

	@Test
	fun serialize() {
		val armorTrimId: ArmorTrimId = ArmorTrimId(Identifier("material_id"), Identifier("pattern_id"))
		val json: JsonElement = ArmorTrimId.Companion.CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, armorTrimId).getOrThrow()
		val expectedJson = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}"

		Assertions.assertEquals(expectedJson, json.toString())
	}

	@Test
	fun deserialize() {
		val json = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}"
		val armorTrimId: ArmorTrimId = ArmorTrimId.Companion.CODEC.parse<JsonElement>(JsonOps.INSTANCE, gson.fromJson<JsonElement>(json, JsonElement::class.java)).getOrThrow()
		val expectedArmorTrimId: ArmorTrimId = ArmorTrimId(Identifier("material_id"), Identifier("pattern_id"))

		Assertions.assertEquals(expectedArmorTrimId, armorTrimId)
	}
}
