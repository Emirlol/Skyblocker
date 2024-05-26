package de.hysky.skyblocker.skyblock.chat

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.Utils.sendMessageToBypassEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame
import net.minecraft.client.MinecraftClient
import net.minecraft.sound.SoundEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object ChatRulesHandler {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private val LOGGER: Logger = LoggerFactory.getLogger(ChatRule::class.java)
	private val CHAT_RULE_FILE: Path = SkyblockerMod.CONFIG_DIR.resolve("chat_rules.json")
	private val MAP_CODEC: Codec<Map<String, List<ChatRule?>>> = Codec.unboundedMap<String, List<ChatRule?>>(Codec.STRING, ChatRule.Companion.LIST_CODEC)

	/**
	 * list of possible locations still formatted for the tool tip
	 */
	val locationsList: List<String?> = listOf(
		"The Farming Islands",
		"Crystal Hollows",
		"Jerry's Workshop",
		"The Park",
		"Dark Auction",
		"Dungeons",
		"The End",
		"Crimson Isle",
		"Hub",
		"Kuudra's Hollow",
		"Private Island",
		"Dwarven Mines",
		"The Garden",
		"Gold Mine",
		"Blazing Fortress",
		"Deep Caverns",
		"Spider's Den",
		"Mineshaft"
	)

	val chatRuleList: MutableList<ChatRule?> = ArrayList()

	fun init() {
		CompletableFuture.runAsync(Runnable { obj: ChatRulesHandler? -> loadChatRules() })
		ClientReceiveMessageEvents.ALLOW_GAME.register(AllowGame { obj: Text?, message: Boolean -> checkMessage(message) })
	}

	private fun loadChatRules() {
		try {
			Files.newBufferedReader(CHAT_RULE_FILE).use { reader ->
				val chatRules = MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow()
				LOGGER.info("[Skyblocker Chat Rules]: {}", chatRules)

				chatRuleList.addAll(chatRules["rules"]!!)
				LOGGER.info("[Skyblocker Chat Rules] Loaded chat rules")
			}
		} catch (e: NoSuchFileException) {
			registerDefaultChatRules()
			LOGGER.warn("[Skyblocker Chat Rules] chat rules file not found, using default rules. This is normal when using for the first time.")
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Chat Rules] Failed to load chat rules file", e)
		}
	}

	private fun registerDefaultChatRules() {
		//clean hub chat
		val cleanHubRule = ChatRule("Clean Hub Chat", false, true, true, true, "(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)", "hub", true, false, false, "", null)
		//mining Ability
		val miningAbilityRule = ChatRule("Mining Ability Alert", false, true, false, true, "is now available!", "Crystal Hollows, Dwarven Mines", false, false, true, "&1Ability", SoundEvents.ENTITY_ARROW_HIT_PLAYER)

		chatRuleList.add(cleanHubRule)
		chatRuleList.add(miningAbilityRule)
	}

	fun saveChatRules() {
		val chatRuleJson = JsonObject()
		chatRuleJson.add("rules", ChatRule.Companion.LIST_CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, chatRuleList).getOrThrow())
		try {
			Files.newBufferedWriter(CHAT_RULE_FILE).use { writer ->
				SkyblockerMod.GSON.toJson(chatRuleJson, writer)
				LOGGER.info("[Skyblocker Chat Rules] Saved chat rules file")
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Chat Rules] Failed to save chat rules file", e)
		}
	}

	/**
	 * Checks each rule in [ChatRulesHandler.chatRuleList] to see if they are a match for the message and if so change outputs based on the options set in the [ChatRule].
	 * @param message the chat message
	 * @param overlay if its overlay
	 */
	private fun checkMessage(message: Text, overlay: Boolean): Boolean {
		if (!isOnSkyblock) return true //do not work not on skyblock

		if (overlay) return true //ignore messages in overlay

		val plain = Formatting.strip(message.string)

		for (rule in chatRuleList) {
			if (rule!!.isMatch(plain)) {
				//get a replacement message
				val newMessage = if (!rule.replaceMessage.isBlank()) {
					formatText(rule.replaceMessage)
				} else {
					message
				}

				if (rule.showAnnouncement) {
					ChatRuleAnnouncementScreen.setText(newMessage)
				}

				//show in action bar
				if (rule.showActionBar && CLIENT.player != null) {
					CLIENT.player!!.sendMessage(newMessage, true)
				}

				//show replacement message in chat
				//bypass MessageHandler#onGameMessage to avoid activating chat rules again
				if (!rule.hideMessage && CLIENT.player != null) {
					sendMessageToBypassEvents(newMessage)
				}

				//play sound
				if (rule.customSound != null && CLIENT.player != null) {
					CLIENT.player!!.playSound(rule.customSound, 100f, 0.1f)
				}

				//do not send original message
				return false
			}
		}
		return true
	}

	/**
	 * Converts a string with color codes into a formatted Text object
	 * @param codedString the string with color codes in
	 * @return formatted text
	 */
	fun formatText(codedString: String?): MutableText {
		if (codedString!!.contains(Formatting.FORMATTING_CODE_PREFIX.toString()) || codedString.contains("&")) {
			val newText = Text.empty()
			val parts = codedString.split(("[" + Formatting.FORMATTING_CODE_PREFIX + "&]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			var style = Style.EMPTY

			for (part in parts) {
				if (part.isEmpty()) continue
				val formatting = Formatting.byCode(part[0])

				if (formatting != null) {
					style = style.withFormatting(formatting)
					Text.literal(part.substring(1)).getWithStyle(style).forEach(Consumer { text: Text? -> newText.append(text) })
				} else {
					newText.append(Text.of(part))
				}
			}
			return newText
		}
		return Text.literal(codedString)
	}
}
