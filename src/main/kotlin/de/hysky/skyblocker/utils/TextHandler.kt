package de.hysky.skyblocker.utils

import com.mojang.logging.LogUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker

object TextHandler {
	private val logger = LoggerFactory.getLogger("Skyblocker")
	private val player get() = MinecraftClient.getInstance().player
	private const val DEBUG = true

	fun info(message: String, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.GRAY)
			is Log -> log(message, Logger::info)
		}
	}

	fun warn(message: String, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.GOLD)
			is Log -> log(message, Logger::warn)
		}
	}

	fun error(message: String, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.RED)
			is Log -> log(message, Logger::error)
		}
	}

	fun warn(message: String, throwable: Throwable, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.GOLD)
			is Log -> log(message, throwable, Logger::warn)
		}
	}

	fun error(message: String, throwable: Throwable, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.RED)
			is Log -> log(message, throwable, Logger::error)
		}
	}

	fun fatal(message: String) {
		log(message, LogUtils.FATAL_MARKER, Logger::error)
	}

	fun debug(message: String) {
		if (DEBUG) chat(message, Formatting.GREEN)
	}

	//Use the method that takes in text and boolean rather than just text
	//This allows narration to work properly
	fun chat(message: Text) = player?.sendMessage(message, false)

	private fun chat(message: String, formatting: Formatting) = chat(Text.literal(message).formatted(formatting))

	private fun log(message: String, marker: Marker, method: Logger.(Marker, String) -> Unit) {
		logger.method(marker, sanitizeInput(message))
	}

	private fun log(message: String, method: Logger.(String) -> Unit) {
		logger.method(sanitizeInput(message))
	}

	private fun log(message: String, throwable: Throwable, method: Logger.(String, Throwable) -> Unit) {
		logger.method(
			sanitizeInput(message),
			throwable
		)
	}

	private fun sanitizeInput(message: String): String {
		return if (message.startsWith("[Skyblocker]")) message
		else if (message.startsWith('[')) "[Skyblocker ${message.drop(1)}"
		else "[Skyblocker] $message"
	}
}

interface Level
data object Chat : Level
data object Log : Level