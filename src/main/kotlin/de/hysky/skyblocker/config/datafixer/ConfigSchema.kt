package de.hysky.skyblocker.config.datafixer

import com.mojang.datafixers.DSL
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.types.templates.TypeTemplate
import java.util.function.Supplier

class ConfigSchema(versionKey: Int, parent: Schema) : Schema(versionKey, parent) {
	override fun registerTypes(schema: Schema, entityTypes: Map<String, Supplier<TypeTemplate>>, blockEntityTypes: Map<String, Supplier<TypeTemplate>>) {
		schema.registerType(true, ConfigDataFixer.CONFIG_TYPE) { DSL.remainder() }
	}

	override fun registerEntities(schema: Schema): Map<String, Supplier<TypeTemplate>> = mapOf()

	override fun registerBlockEntities(schema: Schema): Map<String, Supplier<TypeTemplate>> = mapOf()
}
