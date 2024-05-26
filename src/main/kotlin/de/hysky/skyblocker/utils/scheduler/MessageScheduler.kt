package de.hysky.skyblocker.utils.scheduler

import de.hysky.skyblocker.utils.scheduler.Scheduler.ScheduledTask
import de.hysky.skyblocker.utils.scheduler.Scheduler.currentTick
import net.minecraft.client.MinecraftClient
import net.minecraft.util.StringHelper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A scheduler for sending chat messages or commands.
 */
object MessageScheduler {
	/**
	 * The timestamp of the last message send,
	 */
	private var lastMessage: Long = 0
	/**
	 * The minimum delay that the server will accept between chat messages.
	 */
	private const val MIN_DELAY = 200
	private val LOGGER: Logger = LoggerFactory.getLogger(MessageScheduler::class.java)

	/**
	 * Sends a chat message or command after the minimum cooldown. Prefer this method to send messages or commands to the server.
	 *
	 * @param message the message to send
	 */
	fun sendMessageAfterCooldown(message: String) {
		if (lastMessage + MIN_DELAY < System.currentTimeMillis()) {
			sendMessage(message)
			lastMessage = System.currentTimeMillis()
		} else {
			queueMessage(message, 0)
		}
	}

	private fun sendMessage(message: String) {
		val client = MinecraftClient.getInstance()
		if (client.player == null) {
			LOGGER.error("[Skyblocker Message Scheduler] Tried to send a message while player is null: {}", message)
			return
		}
		StringHelper.truncateChat(StringUtils.normalizeSpace(message.trim { it <= ' ' })).let {
			if (it.startsWith("/")) {
				client.player!!.networkHandler.sendCommand(it.substring(1))
			} else {
				client.inGameHud.chatHud.addToMessageHistory(it)
				client.player!!.networkHandler.sendChatMessage(it)
			}
		}
	}

	/**
	 * Queues a chat message or command to send in `delay` ticks. Use this method to send messages or commands a set time in the future. The minimum cooldown is still respected.
	 *
	 * @param message the message to send
	 * @param delay   the delay before sending the message in ticks
	 */
	fun queueMessage(message: String, delay: Int) {
		Scheduler.schedule(currentTick + delay, ScheduledMessage(false) { sendMessage(message) })
	}

	class ScheduledMessage(interval: Int, cyclic: Boolean, multithreaded: Boolean, task: () -> Unit) : ScheduledTask(interval, cyclic, multithreaded, task) {
		constructor(multithreaded: Boolean, task: () -> Unit) : this(-1, false, multithreaded, task)

		override fun runTask(): Boolean {
			if (lastMessage + MIN_DELAY < System.currentTimeMillis()) {
				task.invoke()
				lastMessage = System.currentTimeMillis()
				return true
			}
			return false
		}
	}
}
