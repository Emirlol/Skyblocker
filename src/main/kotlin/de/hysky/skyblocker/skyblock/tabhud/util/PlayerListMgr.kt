package de.hysky.skyblocker.skyblock.tabhud.util

import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This class may be used to get data from the player list. It doesn't get its
 * data every frame, instead, a scheduler is used to update the data this class
 * is holding periodically. The list is sorted like in the vanilla game.
 */
object PlayerListMgr {
	val LOGGER: Logger = LoggerFactory.getLogger("Skyblocker Regex")

	private var playerList: List<PlayerListEntry>? = null
	var footer: String? = null
		private set

	fun updateList() {
		if (!isOnSkyblock) {
			return
		}

		val cpnwh = MinecraftClient.getInstance().networkHandler

		// check is needed, else game crashes on server leave
		if (cpnwh != null) {
			playerList = cpnwh.playerList.stream().sorted(PlayerListHudAccessor.getOrdering()).toList()
		}
	}

	@JvmStatic
	fun updateFooter(f: Text?) {
		if (f == null) {
			footer = null
		} else {
			footer = f.string
		}
	}

	/**
	 * Get the display name at some index of the player list and apply a pattern to
	 * it
	 *
	 * @return the matcher if p fully matches, else null
	 */
	@JvmStatic
	fun regexAt(idx: Int, p: Pattern): Matcher? {
		val str = strAt(idx) ?: return null

		val m = p.matcher(str)
		if (!m.matches()) {
			LOGGER.error("no match: \"{}\" against \"{}\"", str, p)
			return null
		} else {
			return m
		}
	}

	/**
	 * Get the display name at some index of the player list as string
	 *
	 * @return the string or null, if the display name is null, empty or whitespace
	 * only
	 */
	fun strAt(idx: Int): String? {
		if (playerList == null) {
			return null
		}

		if (playerList!!.size <= idx) {
			return null
		}

		val txt = playerList!![idx].displayName ?: return null
		val str = txt.string.trim { it <= ' ' }
		if (str.isEmpty()) {
			return null
		}
		return str
	}

	/**
	 * Gets the display name at some index of the player list
	 *
	 * @return the text or null, if the display name is null
	 *
	 * @implNote currently designed specifically for crimson isles faction quests
	 * widget and the rift widgets, might not work correctly without
	 * modification for other stuff. you've been warned!
	 */
	fun textAt(idx: Int): Text? {
		if (playerList == null) {
			return null
		}

		if (playerList!!.size <= idx) {
			return null
		}

		val txt = playerList!![idx].displayName ?: return null

		// Rebuild the text object to remove leading space thats in all faction quest
		// stuff (also removes trailing space just in case)
		val newText = Text.empty()
		val size = txt.siblings.size

		for (i in 0 until size) {
			val current = txt.siblings[i]
			var textToAppend = current.string

			// Trim leading & trailing space - this can only be done at the start and end
			// otherwise it'll produce malformed results
			if (i == 0) textToAppend = textToAppend.trimStart()
			if (i == size - 1) textToAppend = textToAppend.trimEnd()

			newText.append(Text.literal(textToAppend).setStyle(current.style))
		}

		// Avoid returning an empty component - Rift advertisements needed this
		if (newText.string.isEmpty()) {
			return null
		}

		return newText
	}

	/**
	 * Get the display name at some index of the player list as Text as seen in the
	 * game
	 *
	 * @return the PlayerListEntry at that index
	 */
	fun getRaw(idx: Int): PlayerListEntry {
		return playerList!![idx]
	}

	val size: Int
		get() = playerList!!.size
}
