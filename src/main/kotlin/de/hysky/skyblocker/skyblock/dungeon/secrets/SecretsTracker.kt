package de.hysky.skyblocker.skyblock.dungeon.secrets

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr.regexAt
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget
import de.hysky.skyblocker.utils.ApiUtils.name2Uuid
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Http.sendHypixelRequest
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils.isInDungeons
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import kotlin.concurrent.Volatile

/**
 * Tracks the amount of secrets players get every run
 */
object SecretsTracker {
	private val LOGGER: Logger = LoggerFactory.getLogger(SecretsTracker::class.java)
	private val TEAM_SCORE_PATTERN: Pattern = Pattern.compile(" +Team Score: [0-9]+ \\([A-z+]+\\)")

	@Volatile
	private var currentRun: TrackedRun? = null

	@Volatile
	private var lastRun: TrackedRun? = null

	@Volatile
	private var lastRunEnded = 0L

	fun init() {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> onMessage(text) })
	}

	private fun calculate(phase: RunPhase) {
		when (phase) {
			RunPhase.START -> CompletableFuture.runAsync {
				val newlyStartedRun = TrackedRun()
				//Initialize players in new run
				for (i in 0..4) {
					val playerName = getPlayerNameAt(i + 1)

					//The player name will be blank if there isn't a player at that index
					if (!playerName.isEmpty()) {
						//If the player was a part of the last run, had non-empty secret data and that run ended less than 5 mins ago then copy the secret data over

						if (lastRun != null && System.currentTimeMillis() <= lastRunEnded + 300000 && lastRun!!.playersSecretData.getOrDefault(playerName, SecretData.EMPTY) !== SecretData.EMPTY) {
							newlyStartedRun.playersSecretData[playerName] = lastRun!!.playersSecretData[playerName]
						} else {
							newlyStartedRun.playersSecretData[playerName] = getPlayerSecrets(playerName)
						}
					}
				}
				currentRun = newlyStartedRun
			}

			RunPhase.END -> CompletableFuture.runAsync {
				//In case the game crashes from something
				if (currentRun != null) {
					val secretsFound = Object2ObjectOpenHashMap<String, SecretData>()

					//Update secret counts
					for (entry in currentRun!!.playersSecretData.entries) {
						val playerName = entry.key
						val startingSecrets = entry.value
						val secretsNow = getPlayerSecrets(playerName)
						val secretsPlayerFound = secretsNow.secrets - startingSecrets!!.secrets

						//Add an entry to the secretsFound map with the data - if the secret data from now or the start was cached a warning will be shown
						secretsFound[playerName] = secretsNow.updated(secretsPlayerFound, startingSecrets.cached || secretsNow.cached)
						entry.setValue(secretsNow)
					}

					//Print the results all in one go, so its clean and less of a chance of it being broken up
					for ((key, value) in secretsFound) {
						sendResultMessage(key, value, true)
					}

					//Swap the current and last run as well as mark the run end time
					lastRunEnded = System.currentTimeMillis()
					lastRun = currentRun
					currentRun = null
				} else {
					sendResultMessage(null, null, false)
				}
			}
		}
	}

	private fun sendResultMessage(player: String?, secretData: SecretData?, success: Boolean) {
		val playerEntity: PlayerEntity? = MinecraftClient.getInstance().player
		if (playerEntity != null) {
			if (success) {
				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.feedback", Text.literal(player).withColor(0xf57542), "§7" + secretData!!.secrets, getCacheText(secretData.cached, secretData.cacheAge))))
			} else {
				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secretsTracker.failFeedback")))
			}
		}
	}

	private fun getCacheText(cached: Boolean, cacheAge: Int): Text {
		return Text.literal("\u2139").styled { style: Style ->
			style.withColor(if (cached) 0xeac864 else 0x218bff).withHoverEvent(
				HoverEvent(HoverEvent.Action.SHOW_TEXT, if (cached) Text.translatable("skyblocker.api.cache.HIT", cacheAge) else Text.translatable("skyblocker.api.cache.MISS"))
			)
		}
	}

	private fun onMessage(text: Text, overlay: Boolean) {
		if (isInDungeons && SkyblockerConfigManager.get().dungeons.playerSecretsTracker && !overlay) {
			val message = Formatting.strip(text.string)

			try {
				if (message == "[NPC] Mort: Here, I found this map when I first entered the dungeon.") calculate(RunPhase.START)
				if (TEAM_SCORE_PATTERN.matcher(message).matches()) calculate(RunPhase.END)
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker] Encountered an unknown error while trying to track player secrets!", e)
			}
		}
	}

	private fun getPlayerNameAt(index: Int): String {
		val matcher = regexAt(1 + (index - 1) * 4, DungeonPlayerWidget.PLAYER_PATTERN)

		return if (matcher != null) matcher.group("name") else ""
	}

	private fun getPlayerSecrets(name: String): CompletableFuture<SecretData> {
		return SkyblockerMod.globalJob.async {
			name2Uuid(name)
		}.asCompletableFuture().thenApplyAsync { uuid ->
			if (!uuid.isNullOrEmpty()) {
				try {
					sendHypixelRequest("player", "?uuid=$uuid").use { response ->
						SecretData(getSecretCountFromAchievements(JsonParser.parseString(response.content).asJsonObject), response.cached(), response.age)
					}
				} catch (e: Exception) {
					TextHandler.error("Encountered an error while trying to fetch $name's secret count!", e)
					SecretData.EMPTY
				}
			} else {
				SecretData.EMPTY
			}
		}
	}

	/**
	 * Gets a player's secret count from their hypixel achievements
	 */
	private fun getSecretCountFromAchievements(playerJson: JsonObject): Int {
		val player = playerJson.getAsJsonObject("player")
		val achievements = if (player.has("achievements")) player.getAsJsonObject("achievements") else null
		return if ((achievements != null && achievements.has("skyblock_treasure_hunter"))) achievements["skyblock_treasure_hunter"].asInt else 0
	}

	/**
	 * This will either reflect the value at the start or the end depending on when this is called
	 */
	@JvmRecord
	private data class TrackedRun(val playersSecretData: Object2ObjectOpenHashMap<String, SecretData?>) {
		constructor() : this(Object2ObjectOpenHashMap<String, SecretData?>())
	}

	@JvmRecord
	private data class SecretData(val secrets: Int, val cached: Boolean, val cacheAge: Int) {
		//If only we had Derived Record Creation :( - https://bugs.openjdk.org/browse/JDK-8321133
		fun updated(secrets: Int, cached: Boolean): SecretData {
			return SecretData(secrets, cached, this.cacheAge)
		}

		companion object {
			val EMPTY: SecretData = SecretData(0, false, 0)
		}
	}

	private enum class RunPhase {
		START, END
	}
}
