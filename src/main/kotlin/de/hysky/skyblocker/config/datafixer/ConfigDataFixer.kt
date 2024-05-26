package de.hysky.skyblocker.config.datafixer

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFixer
import com.mojang.datafixers.DataFixerBuilder
import com.mojang.datafixers.schemas.Schema
import com.mojang.logging.LogUtils
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.datafixer.JsonHelper.getInt
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.BiFunction

object ConfigDataFixer {
	val LOGGER: Logger = LogUtils.getLogger()
	private val CONFIG_DIR: Path = FabricLoader.getInstance().configDir
	val CONFIG_TYPE: DSL.TypeReference = DSL.TypeReference { "config" }

	@JvmOverloads
	fun apply(configDir: Path = CONFIG_DIR.resolve("skyblocker.json"), backupDir: Path = CONFIG_DIR.resolve("skyblocker.json.old")) {
		//User is new - has no config file (or maybe config folder)
		if (!Files.exists(CONFIG_DIR) || !Files.exists(configDir)) return

		//Should never be null if the file exists unless its malformed JSON or something in which case well it gets reset
		val oldConfig = loadConfig(configDir)
		if (oldConfig == null || getInt(oldConfig, "version").orElse(1) == SkyblockerConfigManager.CONFIG_VERSION) return

		val newConfig = apply(oldConfig)

		//Write the updated file
		if (!writeConfig(configDir, newConfig)) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to fix up config file!")
			writeConfig(backupDir, oldConfig)
		}
	}

	@JvmOverloads
	fun apply(oldConfig: JsonObject?, newVersion: Int = SkyblockerConfigManager.CONFIG_VERSION): JsonObject {
		val start = System.currentTimeMillis()

		val newConfig = build().update(CONFIG_TYPE, Dynamic(JsonOps.INSTANCE, oldConfig), getInt(oldConfig, "version").orElse(1), newVersion).value.asJsonObject

		val end = System.currentTimeMillis()
		LOGGER.info("[Skyblocker Config Data Fixer] Applied datafixers in {} ms!", end - start)
		return newConfig
	}

	private fun build(): DataFixer {
		val builder = DataFixerBuilder(SkyblockerConfigManager.CONFIG_VERSION)

		builder.addSchema(1) { versionKey: Int, parent: Schema? -> ConfigSchema(versionKey, parent) }
		val schema2 = builder.addSchema(2, BiFunction<Int, Schema, Schema> { versionKey: Int?, parent: Schema? -> Schema(versionKey, parent) })
		builder.addFixer(ConfigFix1(schema2, true))
		val schema3 = builder.addSchema(3, BiFunction<Int, Schema, Schema> { versionKey: Int?, parent: Schema? -> Schema(versionKey, parent) })
		builder.addFixer(ConfigFix2QuickNav(schema3, true))

		return builder.buildUnoptimized()
	}

	private fun loadConfig(path: Path): JsonObject? {
		try {
			Files.newBufferedReader(path).use { reader ->
				return JsonParser.parseReader(reader).asJsonObject
			}
		} catch (t: Throwable) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to load config file!", t)
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
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to save config file at {}!", path, t)
		}

		return false
	}
}
