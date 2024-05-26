package de.hysky.skyblocker.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TextLogger {
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

	fun error(message: String, throwable: Throwable, level: Level = Log) {
		when (level) {
			is Chat -> chat(message, Formatting.RED)
			is Log -> log(message, throwable, Logger::error)
		}
	}

	fun debug(message: String) {
		if (DEBUG) chat(message, Formatting.GREEN)
	}

	private fun chat(message: String, formatting: Formatting) = player?.sendMessage(Text.literal(message).formatted(formatting), false)

	private fun log(message: String, method: Logger.(String) -> Unit) {
		logger.method(
			if (message.startsWith("[Skyblocker]")) message
			else if (message.startsWith('[')) "[Skyblocker ${message.drop(1)}"
			else "[Skyblocker] $message"
		)
	}

	private fun log(message: String, throwable: Throwable, method: Logger.(String, Throwable) -> Unit) {
		logger.method(
			if (message.startsWith("[Skyblocker]")) message
			else if (message.startsWith('[')) "[Skyblocker ${message.drop(1)}"
			else "[Skyblocker] $message",
			throwable
		)
	}
}

interface Level
data object Chat : Level
data object Log : Level