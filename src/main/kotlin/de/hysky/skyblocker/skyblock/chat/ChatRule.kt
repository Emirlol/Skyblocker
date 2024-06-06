package de.hysky.skyblocker.skyblock.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.utils.Utils.map
import net.minecraft.sound.SoundEvent
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Data class to contain all the settings for a chat rule
 */
data class ChatRule(
	var name: String = "New Rule",
	val enabled: Boolean = true,
	var isPartialMatch: Boolean = false,
	var isRegex: Boolean = false,
	var isIgnoreCase: Boolean = true,
	var filter: String = "",
	var validLocations: String = "",
	var hideMessage: Boolean = true,
	var showActionBar: Boolean = false,
	var showAnnouncement: Boolean = false,
	var replaceMessage: String? = null,
	var customSound: SoundEvent? = null
) {
	/**
	 * checks every input option and if the games state and the inputted str matches them returns true.
	 * @param inputString the chat message to check if fits
	 * @return if the inputs are all true and the outputs should be performed
	 */
	fun isMatch(inputString: String): Boolean {
		//enabled
		if (!enabled) return false

		//ignore case
		val testString: String
		val testFilter: String

		if (isIgnoreCase) {
			testString = inputString.lowercase(Locale.getDefault())
			testFilter = filter.lowercase(Locale.getDefault())
		} else {
			testString = inputString
			testFilter = filter
		}

		//filter
		if (testFilter.isBlank()) return false
		if (isRegex) {
			val regex = testFilter.toRegex()
			if (isPartialMatch) {
				regex.find(testString) ?: return false
			} else {
				regex.matchEntire(testString) ?: return false
			}
		} else {
			if (isPartialMatch) {
				if (testString !in testFilter) return false
			} else {
				if (testFilter != testString) return false
			}
		}

		//location
		if (validLocations.isBlank()) return true //if no locations do not check

		val cleanedMapLocation = map.lowercase(Locale.getDefault()).replace(" ", "")
		var isLocationValid: Boolean? = null
		for (validLocation in validLocations.replace(" ", "").lowercase(Locale.getDefault()).split(",").dropLastWhile { it.isEmpty() }) { //the locations are split by "," and start with ! if not locations
			if (validLocation.startsWith("!")) { //not location
				if (validLocation.substring(1) == cleanedMapLocation) {
					isLocationValid = false
					break
				} else {
					isLocationValid = true
				}
			} else if (validLocation == cleanedMapLocation) { //normal location
				isLocationValid = true
				break
			}
		}

		//if location is not in the list at all and is a not a "!" location or and is a normal location
		return isLocationValid != null && isLocationValid
	}

	companion object {
		private val CODEC: Codec<ChatRule> = RecordCodecBuilder.create { instance ->
			instance.group(
				Codec.STRING.fieldOf("name").forGetter { it.name },
				Codec.BOOL.fieldOf("enabled").forGetter { it.enabled },
				Codec.BOOL.fieldOf("isPartialMatch").forGetter { it.isPartialMatch },
				Codec.BOOL.fieldOf("isRegex").forGetter { it.isRegex },
				Codec.BOOL.fieldOf("isIgnoreCase").forGetter { it.isIgnoreCase },
				Codec.STRING.fieldOf("filter").forGetter { it.filter },
				Codec.STRING.fieldOf("validLocations").forGetter { it.validLocations },
				Codec.BOOL.fieldOf("hideMessage").forGetter { it.hideMessage },
				Codec.BOOL.fieldOf("showActionBar").forGetter { it.showActionBar },
				Codec.BOOL.fieldOf("showAnnouncement").forGetter { it.showAnnouncement },
				Codec.STRING.optionalFieldOf("replaceMessage").forGetter { Optional.ofNullable(it.replaceMessage) },
				SoundEvent.CODEC.optionalFieldOf("customSound").forGetter { it: ChatRule -> Optional.ofNullable(it.customSound) }
			).apply(instance) { name, enabled, isPartialMatch, isRegex, isIgnoreCase, filter, validLocations, hideMessage, showActionBar, showAnnouncement, replaceMessage, customSound -> ChatRule(name, enabled, isPartialMatch, isRegex, isIgnoreCase, filter, validLocations, hideMessage, showActionBar, showAnnouncement, replaceMessage.getOrNull(), customSound.getOrNull()) }
		}
		val LIST_CODEC: Codec<List<ChatRule>> = CODEC.listOf()
	}
}



