package de.hysky.skyblocker.skyblock.searchoverlay

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType
import de.hysky.skyblocker.utils.NEURepoManager
import de.hysky.skyblocker.utils.NEURepoManager.runAsyncAfterLoad
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import it.unimi.dsi.fastutil.Pair
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.min

object SearchOverManager {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private val LOGGER: Logger = LoggerFactory.getLogger("Skyblocker Search Overlay")

	private val BAZAAR_ENCHANTMENT_PATTERN: Pattern = Pattern.compile("ENCHANTMENT_(\\D*)_(\\d+)")
	private val AUCTION_PET_AND_RUNE_PATTERN: Pattern = Pattern.compile("([A-Z0-9_]+);(\\d+)")

	/**
	 * converts index (in array) +1 to a roman numeral
	 */
	private val ROMAN_NUMERALS = arrayOf(
		"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
		"XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
	)

	private var sign: SignBlockEntity? = null
	private var signFront = true
	private var isAuction = false
	private var isCommand = false

	var search: String = ""

	// Use non-final variables and swap them to prevent concurrent modification
	private var bazaarItems: HashSet<String?>? = null
	private var auctionItems: HashSet<String?>? = null
	private var namesToId: HashMap<String?, String>? = null

	var suggestionsArray: Array<String?> = arrayOf()

	/**
	 * uses the skyblock api and Moulberry auction to load a list of items in bazaar and auction house
	 */
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerSearchCommands(dispatcher) })
		runAsyncAfterLoad(Runnable { obj: SearchOverManager? -> loadItems() })
	}

	private fun registerSearchCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableCommands) {
			dispatcher.register(ClientCommandManager.literal("ahs").executes { context: CommandContext<FabricClientCommandSource?>? -> startCommand(true) })
			dispatcher.register(ClientCommandManager.literal("bzs").executes { context: CommandContext<FabricClientCommandSource?>? -> startCommand(false) })
		}
	}

	private fun startCommand(isAuction: Boolean): Int {
		isCommand = true
		SearchOverManager.isAuction = isAuction
		search = ""
		suggestionsArray = arrayOf()
		CLIENT.send(Runnable { CLIENT.setScreen(OverlayScreen(Text.of(""))) })
		return Command.SINGLE_SUCCESS
	}

	private fun loadItems() {
		val bazaarItems = HashSet<String?>()
		val auctionItems = HashSet<String?>()
		val namesToId = HashMap<String?, String>()

		//get bazaar items
		try {
			if (TooltipInfoType.BAZAAR.data == null) TooltipInfoType.BAZAAR.run()

			val products = TooltipInfoType.BAZAAR.data
			for ((_, value) in products.entrySet()) {
				if (value.isJsonObject) {
					val product = value.asJsonObject
					val id = product["id"].asString
					val sellVolume = product["sellVolume"].asInt
					if (sellVolume == 0) continue  //do not add items that do not sell e.g. they are not actual in the bazaar

					val matcher = BAZAAR_ENCHANTMENT_PATTERN.matcher(id)
					if (matcher.matches()) { //format enchantments
						//remove ultimate if in name
						var name = matcher.group(1)
						if (!name.contains("WISE")) { //only way found to remove ultimate from everything but ultimate wise
							name = name.replace("ULTIMATE_", "")
						}
						name = name.replace("_", " ")
						name = capitalizeFully(name)
						val enchantLevel = matcher.group(2).toInt()
						var level = ""
						if (enchantLevel > 0) {
							level = ROMAN_NUMERALS[enchantLevel - 1]
						}
						bazaarItems.add("$name $level")
						namesToId["$name $level"] = matcher.group(1) + ";" + matcher.group(2)
						continue
					}
					//look up id for name
					val neuItem = NEURepoManager.NEU_REPO.items.getItemBySkyblockId(id)
					if (neuItem != null) {
						val name = Formatting.strip(neuItem.displayName)
						bazaarItems.add(name)
						namesToId[name] = id
						continue
					}
				}
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker] Failed to load bazaar item list! ", e)
		}

		//get auction items
		try {
			if (TooltipInfoType.THREE_DAY_AVERAGE.data == null) {
				TooltipInfoType.THREE_DAY_AVERAGE.run()
			}
			for (entry in TooltipInfoType.THREE_DAY_AVERAGE.data.entrySet()) {
				var id = entry.key

				val matcher = AUCTION_PET_AND_RUNE_PATTERN.matcher(id)
				if (matcher.find()) { //is a pet or rune convert id to name
					var name = matcher.group(1).replace("_", " ")
					name = capitalizeFully(name)
					auctionItems.add(name)
					namesToId[name] = id
					continue
				}
				//something else look up in NEU repo.
				id = id.split("[+-]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
				val neuItem = NEURepoManager.NEU_REPO.items.getItemBySkyblockId(id)
				if (neuItem != null) {
					val name = Formatting.strip(neuItem.displayName)
					auctionItems.add(name)
					namesToId[name] = id
					continue
				}
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker] Failed to load auction house item list! ", e)
		}

		SearchOverManager.bazaarItems = bazaarItems
		SearchOverManager.auctionItems = auctionItems
		SearchOverManager.namesToId = namesToId
	}

	/**
	 * Capitalizes the first letter off every word in a string
	 * @param str string to capitalize
	 */
	private fun capitalizeFully(str: String): String {
		if (str == null || str.isEmpty()) {
			return str
		}

		return Arrays.stream(str.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
			.map { t: String -> t.substring(0, 1).uppercase(Locale.getDefault()) + t.substring(1).lowercase(Locale.getDefault()) }
			.collect(Collectors.joining(" "))
	}

	/**
	 * Receives data when a search is started and resets values
	 * @param sign the sign that is being edited
	 * @param front if it's the front of the sign
	 * @param isAuction if the sign is loaded from the auction house menu or bazaar
	 */
    @JvmStatic
    fun updateSign(sign: SignBlockEntity, front: Boolean, isAuction: Boolean) {
		signFront = front
		SearchOverManager.sign = sign
		isCommand = false
		SearchOverManager.isAuction = isAuction
		if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.keepPreviousSearches) {
			val messages = SearchOverManager.sign!!.getText(signFront).getMessages(CLIENT.shouldFilterText())
			search = messages[0].string
			if (!messages[1].string.isEmpty()) {
				if (!search.endsWith(" ")) {
					search += " "
				}
				search += messages[1].string
			}
		} else {
			search = ""
		}
		suggestionsArray = arrayOf()
	}

	/**
	 * Updates the search value and the suggestions based on that value.
	 * @param newValue new search value
	 */
	fun updateSearch(newValue: String) {
		search = newValue
		//update the suggestion values
		val totalSuggestions = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions
		if (newValue.isBlank() || totalSuggestions == 0) return  //do not search for empty value

		suggestionsArray = (if (isAuction) auctionItems else bazaarItems)!!.stream().filter { item: String? -> item!!.lowercase(Locale.getDefault()).contains(search.lowercase(Locale.getDefault())) }.limit(totalSuggestions.toLong()).toArray<String?> { _Dummy_.__Array__() }
	}

	/**
	 * Gets the suggestion in the suggestion array at the index
	 * @param index index of suggestion
	 */
	fun getSuggestion(index: Int): String? {
		return if (suggestionsArray.size > index && suggestionsArray[index] != null) {
			suggestionsArray[index]
		} else { //there are no suggestions yet
			""
		}
	}

	fun getSuggestionId(index: Int): String? {
		return namesToId!![getSuggestion(index)]
	}

	/**
	 * Gets the item name in the history array at the index
	 * @param index index of suggestion
	 */
	fun getHistory(index: Int): String? {
		if (isAuction) {
			if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.auctionHistory.size > index) {
				return SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.auctionHistory[index]
			}
		} else {
			if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.bazaarHistory.size > index) {
				return SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.bazaarHistory[index]
			}
		}
		return null
	}

	fun getHistoryId(index: Int): String? {
		return namesToId!![getHistory(index)]
	}

	/**
	 * Add the current search value to the start of the history list and truncate to the max history value and save this to the config
	 */
	private fun saveHistory() {
		//save to history
		val config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay
		if (isAuction) {
			if (config.auctionHistory.isEmpty() || config.auctionHistory.first != search) {
				config.auctionHistory.addFirst(search)
				if (config.auctionHistory.size > config.historyLength) {
					config.auctionHistory = config.auctionHistory.subList(0, config.historyLength)
				}
			}
		} else {
			if (config.bazaarHistory.isEmpty() || config.bazaarHistory.first != search) {
				config.bazaarHistory.addFirst(search)
				if (config.bazaarHistory.size > config.historyLength) {
					config.bazaarHistory = config.bazaarHistory.subList(0, config.historyLength)
				}
			}
		}
		SkyblockerConfigManager.save()
	}

	/**
	 * Saves the current value of ([SearchOverManager.search]) then pushes it to a command or sign depending on how the gui was opened
	 */
	fun pushSearch() {
		//save to history
		if (!search.isEmpty()) {
			saveHistory()
		}
		if (isCommand) {
			pushCommand()
		} else {
			pushSign()
		}
	}

	/**
	 * runs the command to search for the value in ([SearchOverManager.search])
	 */
	private fun pushCommand() {
		if (search.isEmpty()) return
		val command = if (isAuction) {
			"/ahSearch " + search
		} else {
			"/bz " + search
		}
		MessageScheduler.INSTANCE.sendMessageAfterCooldown(command)
	}

	/**
	 * pushes the ([SearchOverManager.search]) to the sign. It needs to split it over two lines without splitting a word
	 */
	private fun pushSign() {
		//splits text into 2 lines max = 30 chars
		val split = splitString(search)

		// send packet to update sign
		if (CLIENT.player != null && sign != null) {
			val messages = sign!!.getText(signFront).getMessages(CLIENT.shouldFilterText())
			CLIENT.player!!.networkHandler.sendPacket(
				UpdateSignC2SPacket(
					sign!!.pos, signFront,
					split.left(),
					split.right(),
					messages[2].string,
					messages[3].string
				)
			)
		}
	}

	fun splitString(s: String): Pair<String, String> {
		if (s.length <= 15) {
			return Pair.of(s, "")
		}
		val index = s.lastIndexOf(' ', 15)
		if (index == -1) {
			return Pair.of(s.substring(0, 15), "")
		}
		return Pair.of(s.substring(0, index), s.substring(index + 1, min((index + 16).toDouble(), s.length.toDouble()).toInt()).trim { it <= ' ' })
	}
}
