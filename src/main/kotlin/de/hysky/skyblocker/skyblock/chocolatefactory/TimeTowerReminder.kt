package de.hysky.skyblocker.skyblock.chocolatefactory

import com.mojang.brigadier.Message
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockJoin
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.util.regex.Pattern

object TimeTowerReminder {
	private const val TIME_TOWER_FILE = "time_tower.txt"
	private val TIME_TOWER_PATTERN: Pattern = Pattern.compile("^TIME TOWER! Your Chocolate Factory production has increased by \\+[\\d.]+x for \\dh!$")
	private val LOGGER: Logger = LoggerFactory.getLogger("Skyblocker Time Tower Reminder")
	private var scheduled = false

	fun init() {
		SkyblockEvents.JOIN.register(SkyblockJoin { obj: TimeTowerReminder? -> checkTempFile() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, message: Boolean -> checkIfTimeTower(message) })
	}

	fun checkIfTimeTower(message: Message, overlay: Boolean) {
		if (!TIME_TOWER_PATTERN.matcher(message.string).matches() || scheduled) return
		Scheduler.INSTANCE.schedule(Runnable { obj: TimeTowerReminder? -> sendMessage() }, 60 * 60 * 20) // 1 hour
		scheduled = true
		val tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile()
		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile()
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker Time Tower Reminder] Failed to create temp file for Time Tower Reminder!", e)
				return
			}
		}

		try {
			FileWriter(tempFile).use { writer ->
				writer.write(System.currentTimeMillis().toString())
			}
		} catch (e: IOException) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to write to temp file for Time Tower Reminder!", e)
		}
	}

	private fun sendMessage() {
		if (MinecraftClient.getInstance().player == null || !isOnSkyblock) return
		MinecraftClient.getInstance().player!!.sendMessage(Constants.PREFIX.append(Text.literal("Your Chocolate Factory's Time Tower has deactivated!").formatted(Formatting.RED)))

		val tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile()
		try {
			scheduled = false
			if (tempFile.exists()) Files.delete(tempFile.toPath())
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to delete temp file for Time Tower Reminder!", e)
		}
	}

	private fun checkTempFile() {
		val tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile()
		if (!tempFile.exists() || scheduled) return

		var time: Long
		try {
			Files.lines(tempFile.toPath()).use { file ->
				time = file.findFirst().orElseThrow().toLong()
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to read temp file for Time Tower Reminder!", e)
			return
		}

		if (System.currentTimeMillis() - time >= 60 * 60 * 1000) sendMessage()
		else Scheduler.INSTANCE.schedule(Runnable { obj: TimeTowerReminder? -> sendMessage() }, 60 * 60 * 20 - ((System.currentTimeMillis() - time) / 50).toInt()) // 50 milliseconds is 1 tick
	}
}
