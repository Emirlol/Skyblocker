package de.hysky.skyblocker.config.datafixer

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFixer
import com.mojang.datafixers.DataFixerBuilder
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.datafixer.JsonHelper.getInt
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.measureTimeMillis

object ConfigDataFixer {
	const val loggerPrefix = "[Config Data Fixer]"
	private val CONFIG_DIR: Path = FabricLoader.getInstance().configDir
	val CONFIG_TYPE: DSL.TypeReference = DSL.TypeReference { "config" }

	fun apply(configDir: Path = CONFIG_DIR.resolve("skyblocker.json"), backupDir: Path = CONFIG_DIR.resolve("skyblocker.json.old")) {
		//User is new - has no config file (or maybe config folder)
		if (!CONFIG_DIR.exists() || !configDir.exists()) return

		//Should never be null if the file exists unless its malformed JSON or something in which case well it gets reset
		val oldConfig = loadConfig(configDir)
		if (oldConfig == null || (getInt(oldConfig, "version") ?: 1) == SkyblockerConfigManager.CONFIG_VERSION) return

		val newConfig = apply(oldConfig)

		//Write the updated file
		if (!writeConfig(configDir, newConfig)) {
			TextHandler.fatal("$loggerPrefix Failed to fix up config file!")
			writeConfig(backupDir, oldConfig)
		}
	}

	fun apply(oldConfig: JsonObject?, newVersion: Int = SkyblockerConfigManager.CONFIG_VERSION): JsonObject {
		var newConfig: JsonObject
		measureTimeMillis {
			newConfig = build().update(CONFIG_TYPE, Dynamic(JsonOps.INSTANCE, oldConfig), getInt(oldConfig, "version") ?: 1, newVersion).value.asJsonObject
		}.let {
			TextHandler.info("$loggerPrefix Applied datafixers in $it ms!")
		}
		return newConfig
	}

	private fun build(): DataFixer {
		val builder = DataFixerBuilder(SkyblockerConfigManager.CONFIG_VERSION)

		builder.addSchema(1) { versionKey: Int, parent: Schema -> ConfigSchema(versionKey, parent) }
		val schema2 = builder.addSchema(2) { versionKey, parent -> Schema(versionKey, parent) }
		builder.addFixer(ConfigFix1(schema2, true))
		val schema3 = builder.addSchema(3) { versionKey, parent -> Schema(versionKey, parent) }
		builder.addFixer(ConfigFix2QuickNav(schema3, true))

		return builder.buildUnoptimized()
	}

	private fun loadConfig(path: Path): JsonObject? {
		try {
			Files.newBufferedReader(path).use { reader ->
				return JsonParser.parseReader(reader).asJsonObject
			}
		} catch (t: Throwable) {
			TextHandler.error("$loggerPrefix Failed to load config file!", t)
		}

		return null
	}

	private fun writeConfig(path: Path, config: JsonObject): Boolean {
		try {
			Files.newBufferedWriter(path).use { writer ->
				SkyblockerMod.GSON.toJson(config, writer)
				return true
			}
		} catch (t: Throwable) {
			TextHandler.error("$loggerPrefix Failed to save config file at ${path}!", t)
		}

		return false
	}
}
