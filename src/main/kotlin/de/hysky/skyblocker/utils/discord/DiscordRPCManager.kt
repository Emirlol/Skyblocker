package de.hysky.skyblocker.utils.discord

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.MiscConfig
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockJoin
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockLeave
import de.hysky.skyblocker.utils.Utils.bits
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.islandArea
import de.hysky.skyblocker.utils.Utils.purse
import meteordevelopment.discordipc.DiscordIPC
import meteordevelopment.discordipc.RichPresence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.util.concurrent.CompletableFuture


/**
 * Manages the discord rich presence. Automatically connects to discord and displays a customizable activity when playing Skyblock.
 */
object DiscordRPCManager {
	val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("###,###.##")
	@JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("Skyblocker Discord RPC")

	/**
	 * The update task used to avoid multiple update tasks running simultaneously.
	 */
	var updateTask: CompletableFuture<Void>? = null
	var startTimeStamp: Long = 0
	var cycleCount: Int = 0

	fun init() {
		SkyblockEvents.LEAVE.register(SkyblockLeave { obj: DiscordRPCManager? -> initAndUpdatePresence() })
		SkyblockEvents.JOIN.register(SkyblockJoin {
			startTimeStamp = System.currentTimeMillis()
			initAndUpdatePresence(true)
		})
	}

	/**
	 * Checks the [custom message][MiscConfig.RichPresence.customMessage], updates [.cycleCount] if enabled, and updates rich presence.
	 */
	fun updateDataAndPresence() {
		// If the custom message is empty, discord will keep the last message, this is can serve as a default if the user doesn't want a custom message
		if (SkyblockerConfigManager.config.misc.richPresence.customMessage.isEmpty()) {
			SkyblockerConfigManager.config.misc.richPresence.customMessage = "Playing Skyblock"
			SkyblockerConfigManager.save()
		}
		if (SkyblockerConfigManager.config.misc.richPresence.cycleMode) cycleCount = (cycleCount + 1) % 3
		initAndUpdatePresence()
	}

	/**
	 * Updates discord presence asynchronously.
	 *
	 *
	 * When the [previous update][.updateTask] does not exist or [has completed][CompletableFuture.isDone]:
	 *
	 *
	 * Connects to discord if [rich presence is enabled][MiscConfig.RichPresence.enableRichPresence],
	 * the player [is on Skyblock][Utils.isOnSkyblock], and [discord is not already connected][DiscordIPC.isConnected].
	 * Updates the presence if [rich presence is enabled][MiscConfig.RichPresence.enableRichPresence]
	 * and the player [is on Skyblock][Utils.isOnSkyblock].
	 * Stops the connection if [rich presence is disabled][MiscConfig.RichPresence.enableRichPresence]
	 * or the player [is not on Skyblock][Utils.isOnSkyblock] and [discord is connected][DiscordIPC.isConnected].
	 * Saves the update task in [.updateTask]
	 *
	 * @param initialization whether this is the first time the presence is being updates. If `true`, a message will be logged
	 * if [rich presence is disabled][MiscConfig.RichPresence.enableRichPresence].
	 */
	/**
	 * @see .initAndUpdatePresence
	 */
	private fun initAndUpdatePresence(initialization: Boolean = false) {
		if (updateTask == null || updateTask!!.isDone) {
			updateTask = CompletableFuture.runAsync {
				if (SkyblockerConfigManager.config.misc.richPresence.enableRichPresence && isOnSkyblock) {
					if (!DiscordIPC.isConnected()) {
						if (DiscordIPC.start(934607927837356052L, null)) {
							LOGGER.info("[Skyblocker] Discord RPC connected successfully")
						} else {
							if (initialization) {
								LOGGER.error("[Skyblocker] Discord RPC failed to connect")
							}
							return@runAsync
						}
					}
					DiscordIPC.setActivity(buildPresence())
				} else if (DiscordIPC.isConnected()) {
					DiscordIPC.stop()
					LOGGER.info("[Skyblocker] Discord RPC disconnected")
				} else if (initialization) {
					LOGGER.info("[Skyblocker] Discord RPC is currently disabled, will not connect")
				}
			}
		}
	}

	fun buildPresence(): RichPresence {
		val presence = RichPresence()
		presence.setLargeImage("skyblocker-default", null)
		presence.setStart(startTimeStamp)
		presence.setDetails(SkyblockerConfigManager.config.misc.richPresence.customMessage)
		presence.setState(info)
		return presence
	}

	val info: String?
		get() {
			var info: String? = null
			if (!SkyblockerConfigManager.config.misc.richPresence.cycleMode) {
				info = when (SkyblockerConfigManager.config.misc.richPresence.info) {
					MiscConfig.Info.BITS -> "Bits: " + DECIMAL_FORMAT.format(bits.toLong())
					MiscConfig.Info.PURSE -> "Purse: " + DECIMAL_FORMAT.format(purse)
					MiscConfig.Info.LOCATION -> islandArea
				}
			} else if (SkyblockerConfigManager.config.misc.richPresence.cycleMode) {
				when (cycleCount) {
					0 -> info = "Bits: " + DECIMAL_FORMAT.format(bits.toLong())
					1 -> info = "Purse: " + DECIMAL_FORMAT.format(purse)
					2 -> info = islandArea
				}
			}
			return info
		}
}
