package de.hysky.skyblocker.config.datafixer

import com.mojang.datafixers.DataFix
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

abstract class ConfigDataFix(outputSchema: Schema, changesType: Boolean) : DataFix(outputSchema, changesType) {
	protected fun <T> fixVersion(dynamic: Dynamic<T>): Dynamic<T> {
		return dynamic.set("version", dynamic.createInt(DataFixUtils.getVersion(versionKey)))
	}
}
