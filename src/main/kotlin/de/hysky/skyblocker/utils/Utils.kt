package de.hysky.skyblocker.utils

import com.google.gson.JsonParser
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockJoin
import de.hysky.skyblocker.mixins.accessors.MessageHandlerAccessor
import de.hysky.skyblocker.skyblock.item.MuseumItemCache
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.Instant
import java.util.*

/**
 * Utility variables and methods for retrieving Skyblock related information.
 */
object Utils {
	private val ALTERNATE_HYPIXEL_ADDRESS = System.getProperty("skyblocker.alternateHypixelAddress", "")

	private const val PROFILE_PREFIX = "Profile: "
	private const val PROFILE_MESSAGE_PREFIX = "§aYou are playing on profile: §e"
	private const val PROFILE_ID_PREFIX = "Profile ID: "
	var isOnHypixel = false
		private set
	var isOnSkyblock = false
		private set

	/**
	 * @return the location parsed from /locraw.
	 */
	var location: Location? = null
		private set


	var profile: String = ""
		private set


	var profileId: String = ""
		private set

	/**
	 * The following fields store data returned from /locraw: [server], [gameType], [locationRaw], and [map].
	 */

	/**
	 * @return the server parsed from /locraw.
	 */
	var server: String = ""
		private set

	/**
	 * @return the game type parsed from /locraw.
	 */
	var gameType: String = ""
		private set

	/**
	 * @return the location raw parsed from /locraw.
	 */
	var locationRaw: String = ""
		private set

	/**
	 * @return the map parsed from /locraw.
	 */
	var map: String = ""
		private set

	private var clientWorldJoinTime = 0L
	private var sentLocRaw = false
	private var canSendLocRaw = false

	//This is required to prevent the location change event from being fired twice.
	private var locationChanged = true

	/**
	 * @return the current mayor as cached on skyblock join.
	 */
	var mayor: String? = null
		private set

	/**
	 * The parent text will always be empty, the actual text content is inside the text's siblings.
	 */
	val TEXT_SCOREBOARD = ObjectArrayList<Text>()
	val STRING_SCOREBOARD = ObjectArrayList<String>()

	val isInDungeons: Boolean
		get() = location == Location.DUNGEON || FabricLoader.getInstance().isDevelopmentEnvironment

	val isInCrystalHollows: Boolean
		get() = location == Location.CRYSTAL_HOLLOWS || FabricLoader.getInstance().isDevelopmentEnvironment

	val isInDwarvenMines: Boolean
		get() = location == Location.DWARVEN_MINES || location == Location.GLACITE_MINESHAFT || FabricLoader.getInstance().isDevelopmentEnvironment

	val isInTheRift: Boolean
		get() = location == Location.THE_RIFT

	val isInTheEnd: Boolean
		get() = location == Location.THE_END

	val isInKuudra: Boolean
		get() = location == Location.KUUDRAS_HOLLOW

	val isInModernForagingIsland: Boolean
		get() = location == Location.MODERN_FORAGING_ISLAND

	fun init() {
		SkyblockEvents.JOIN.register(SkyblockJoin { tickMayorCache(false) })
		ClientPlayConnectionEvents.JOIN.register(::onClientWorldJoin)
		ClientReceiveMessageEvents.ALLOW_GAME.register(::onChatMessage)
		ClientReceiveMessageEvents.GAME_CANCELED.register(::onChatMessage) // Somehow this works even though onChatMessage returns a boolean
		Scheduler.scheduleCyclic(24000, true) { tickMayorCache(true) } // Update every 20 minutes
	}

	/**
	 * Updates all the fields stored in this class from the sidebar, player list, and /locraw.
	 */
	fun update() {
		val client = MinecraftClient.getInstance()
		updateScoreboard(client)
		updatePlayerPresenceFromScoreboard(client)
		updateFromPlayerList(client)
		updateLocRaw()
	}

	/**
	 * Updates [isOnSkyblock], [isInDungeons], and [isInjected] from the scoreboard.
	 */
	private fun updatePlayerPresenceFromScoreboard(client: MinecraftClient) {
		var sidebar: List<String> = STRING_SCOREBOARD
		val fabricLoader = FabricLoader.getInstance()

		if (client.world == null || client.isInSingleplayer || sidebar.isEmpty()) {
			if (fabricLoader.isDevelopmentEnvironment) {
				sidebar = emptyList()
			} else {
				isOnSkyblock = false
				return
			}
		}

		if (sidebar.isEmpty() && !fabricLoader.isDevelopmentEnvironment) return

		if (fabricLoader.isDevelopmentEnvironment || isConnectedToHypixel(client)) {
			if (!isOnHypixel) isOnHypixel = true

			if (fabricLoader.isDevelopmentEnvironment || sidebar.first().contains("SKYBLOCK") || sidebar.first().contains("SKIBLOCK")) {
				if (!isOnSkyblock) {
					isOnSkyblock = true
					SkyblockEvents.JOIN.invoker().onSkyblockJoin()
				}
			} else {
				onLeaveSkyblock()
			}
		} else if (isOnHypixel) {
			isOnHypixel = false
			onLeaveSkyblock()
		}
	}

	private fun isConnectedToHypixel(client: MinecraftClient): Boolean {
		val serverAddress = client.currentServerEntry?.address?.lowercase(Locale.getDefault()) ?: ""
		val serverBrand = client.player?.networkHandler?.brand ?: ""

		return serverAddress.equals(ALTERNATE_HYPIXEL_ADDRESS, true) || serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord")
	}

	private fun onLeaveSkyblock() {
		if (isOnSkyblock) {
			isOnSkyblock = false
			SkyblockEvents.LEAVE.invoker().onSkyblockLeave()
		}
	}

	val islandArea: String
		get() {
			try {
				for (sidebarLine in STRING_SCOREBOARD) {
					if (sidebarLine!!.contains("⏣") || sidebarLine.contains("ф") /* Rift */) {
						return sidebarLine.trim()
					}
				}
			} catch (e: IndexOutOfBoundsException) {
				TextHandler.error("Failed to get location from sidebar", e)
			}
			return "Unknown"
		}

	val purse: Double
		get() {
			var purseString: String? = null
			var purse = 0.0

			try {
				for (sidebarLine in STRING_SCOREBOARD) {
					if (sidebarLine.contains("Piggy:") || sidebarLine.contains("Purse:")) purseString = sidebarLine
				}
				purse = purseString?.replace("[^0-9.]".toRegex(), "")?.trim()?.toDouble() ?: 0.0
			} catch (e: IndexOutOfBoundsException) {
				TextHandler.error("Failed to get purse from sidebar", e)
			}
			return purse
		}

	val bits: Int
		get() {
			var bits = 0
			var bitsString: String? = null
			try {
				for (sidebarLine in STRING_SCOREBOARD) {
					if (sidebarLine!!.contains("Bits")) bitsString = sidebarLine
				}
				if (bitsString != null) {
					bits = bitsString.replace("[^0-9.]".toRegex(), "").trim().toInt()
				}
			} catch (e: IndexOutOfBoundsException) {
				TextHandler.error("Failed to get bits from sidebar", e)
			}
			return bits
		}

	private fun updateScoreboard(client: MinecraftClient) {
		try {
			TEXT_SCOREBOARD.clear()
			STRING_SCOREBOARD.clear()

			val player = client.player ?: return

			val scoreboard = player.scoreboard
			val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1))
			val textLines = ObjectArrayList<Text>()
			val stringLines = ObjectArrayList<String>()

			for (scoreHolder in scoreboard.knownScoreHolders) {
				//Limit to just objectives displayed in the scoreboard (specifically sidebar objective)
				if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
					val team = scoreboard.getScoreHolderTeam(scoreHolder.nameForScoreboard)

					if (team != null) {
						val textLine: Text = Text.empty().append(team.prefix.copy()).append(team.suffix.copy())
						val strLine = team.prefix.string + team.suffix.string

						if (strLine.trim { it <= ' ' }.isNotEmpty()) {
							val formatted = Formatting.strip(strLine)

							textLines.add(textLine)
							stringLines.add(formatted)
						}
					}
				}
			}

			if (objective != null) {
				stringLines.add(objective.displayName.string)
				textLines.add(Text.empty().append(objective.displayName.copy()))

				stringLines.reverse()
				textLines.reverse()
			}

			TEXT_SCOREBOARD.addAll(textLines)
			STRING_SCOREBOARD.addAll(stringLines)
		} catch (e: NullPointerException) {
			//Do nothing
		}
	}

	private fun updateFromPlayerList(client: MinecraftClient) {
		client.networkHandler?.playerList?.forEach { playerListEntry ->
			val name = playerListEntry.displayName?.string ?: return@forEach
			if (name.startsWith(PROFILE_PREFIX)) {
				profile = name.substring(PROFILE_PREFIX.length)
			}
		}
	}

	private fun onClientWorldJoin(handler: ClientPlayNetworkHandler?, sender: PacketSender?, client: MinecraftClient?) {
		clientWorldJoinTime = System.currentTimeMillis()
		resetLocRawInfo()
	}

	/**
	 * Sends /locraw to the server if the player is on skyblock and on a new island.
	 */
	private fun updateLocRaw() {
		if (isOnSkyblock) {
			val currentTime = System.currentTimeMillis()
			if (!sentLocRaw && canSendLocRaw && currentTime > clientWorldJoinTime + 1000) {
				MessageScheduler.sendMessageAfterCooldown("/locraw")
				sentLocRaw = true
				canSendLocRaw = false
				locationChanged = true
			}
		} else {
			resetLocRawInfo()
		}
	}

	/**
	 * Parses /locraw chat message and updates [.server], [.gameType], [.locationRaw], [.map]
	 * and [.location]
	 *
	 * @param message json message from chat
	 */
	private fun parseLocRaw(message: String) {
		val locRaw = JsonParser.parseString(message).asJsonObject

		if (locRaw.has("server")) {
			server = locRaw["server"].asString
		}
		if (locRaw.has("gameType")) {
			gameType = locRaw["gameType"].asString
		}
		if (locRaw.has("mode")) {
			locationRaw = locRaw["mode"].asString
			location = Location.from(locationRaw)
		} else {
			location = null
		}
		if (locRaw.has("map")) {
			map = locRaw["map"].asString
		}

		if (locationChanged && location != null) {
			SkyblockEvents.LOCATION_CHANGE.invoker().onSkyblockLocationChange(location!!)
			locationChanged = false
		}
	}

	/**
	 * Parses the /locraw reply from the server and updates the player's profile id
	 *
	 * @return not display the message in chat if the command is sent by the mod
	 */
	fun onChatMessage(text: Text, overlay: Boolean): Boolean {
		val message = text.string

		if (message.startsWith("{\"server\":") && message.endsWith("}")) {
			parseLocRaw(message)
			val shouldFilter = !sentLocRaw
			sentLocRaw = false

			return shouldFilter
		}

		if (isOnSkyblock) {
			if (message.startsWith(PROFILE_MESSAGE_PREFIX)) {
				profile = message.substring(PROFILE_MESSAGE_PREFIX.length).split("§b".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
			} else if (message.startsWith(PROFILE_ID_PREFIX)) {
				profileId = message.substring(PROFILE_ID_PREFIX.length)

				MuseumItemCache.tick(profileId)
			}
		}

		return true
	}

	private fun resetLocRawInfo() {
		sentLocRaw = false
		canSendLocRaw = true
		server = ""
		gameType = ""
		locationRaw = ""
		map = ""
		location = null
	}

	private fun tickMayorCache(refresh: Boolean) {
		if (mayor?.isNotEmpty() == true && !refresh) return

		SkyblockerMod.globalJob.launch {
			try {
				withTimeout(15_000) {
					val json = JsonParser.parseString(Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/election")).asJsonObject
					if (json["success"].asBoolean) json["mayor"].asJsonObject["name"].asString.let {
						if (it.isNotEmpty()) mayor = it
					}
				}
			} catch (e: Exception) {
				TextHandler.error("Failed to get mayor status!", e)
			}
		}
	}

	/**
	 * Used to avoid triggering things like chat rules or chat listeners infinitely, do not use otherwise.
	 *
	 * Bypasses MessageHandler#onGameMessage
	 */
	fun sendMessageToBypassEvents(message: Text?) {
		val client = MinecraftClient.getInstance()

		client.inGameHud.chatHud.addMessage(message)
		(client.messageHandler as MessageHandlerAccessor).invokeAddToChatLog(message, Instant.now())
		client.narratorManager.narrateSystemMessage(message)
	}
}
