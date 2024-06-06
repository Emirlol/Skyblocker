package de.hysky.skyblocker

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims
import net.minecraft.util.Identifier
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ArmorTrimIdSerializationTest {
	private val gson = Gson()

	@Test
	fun serialize() {
		val armorTrimId = CustomArmorTrims.ArmorTrimId(Identifier("material_id"), Identifier("pattern_id"))
		val json = CustomArmorTrims.ArmorTrimId.Companion.CODEC.encodeStart(JsonOps.INSTANCE, armorTrimId).getOrThrow()
		val expectedJson = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}"

		Assertions.assertEquals(expectedJson, json.toString())
	}

	@Test
	fun deserialize() {
		val json = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}"
		val armorTrimId = CustomArmorTrims.ArmorTrimId.Companion.CODEC.parse(JsonOps.INSTANCE, gson.fromJson(json, JsonElement::class.java)).getOrThrow()
		val expectedArmorTrimId = CustomArmorTrims.ArmorTrimId(Identifier("material_id"), Identifier("pattern_id"))

		Assertions.assertEquals(expectedArmorTrimId, armorTrimId)
	}
}
