package de.hysky.skyblocker.skyblock.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.utils.Utils.map
import net.minecraft.sound.SoundEvent
import java.util.*
import java.util.regex.Pattern

/**
 * Data class to contain all the settings for a chat rule
 */
class ChatRule {
	var name: String

	//inputs
	var enabled: Boolean
	var partialMatch: Boolean
	var regex: Boolean
	var ignoreCase: Boolean
	var filter: String
	var validLocations: String

	//output
	var hideMessage: Boolean
	var showActionBar: Boolean
	var showAnnouncement: Boolean
	var replaceMessage: String?
	var customSound: SoundEvent?

	/**
	 * Creates a chat rule with default options.
	 */
	constructor() {
		this.name = "New Rule"

		this.enabled = true
		this.partialMatch = false
		this.regex = false
		this.ignoreCase = true
		this.filter = ""
		this.validLocations = ""

		this.hideMessage = true
		this.showActionBar = false
		this.showAnnouncement = false
		this.replaceMessage = null
		this.customSound = null
	}

	constructor(name: String, enabled: Boolean, isPartialMatch: Boolean, isRegex: Boolean, isIgnoreCase: Boolean, filter: String, validLocations: String, hideMessage: Boolean, showActionBar: Boolean, showAnnouncement: Boolean, replaceMessage: String?, customSound: SoundEvent?) {
		this.name = name
		this.enabled = enabled
		this.partialMatch = isPartialMatch
		this.regex = isRegex
		this.ignoreCase = isIgnoreCase
		this.filter = filter
		this.validLocations = validLocations
		this.hideMessage = hideMessage
		this.showActionBar = showActionBar
		this.showAnnouncement = showAnnouncement
		this.replaceMessage = replaceMessage
		this.customSound = customSound
	}

	private constructor(name: String, enabled: Boolean, isPartialMatch: Boolean, isRegex: Boolean, isIgnoreCase: Boolean, filter: String, validLocations: String, hideMessage: Boolean, showActionBar: Boolean, showAnnouncement: Boolean, replaceMessage: Optional<String?>, customSound: Optional<SoundEvent?>) : this(name, enabled, isPartialMatch, isRegex, isIgnoreCase, filter, validLocations, hideMessage, showActionBar, showAnnouncement, replaceMessage.orElse(null), customSound.orElse(null))

	private val replaceMessageOpt: Optional<String>
		get() = if (replaceMessage == null) Optional.empty() else Optional.of(replaceMessage!!)

	private val customSoundOpt: Optional<SoundEvent>
		get() = if (customSound == null) Optional.empty() else Optional.of(customSound!!)

	/**
	 * checks every input option and if the games state and the inputted str matches them returns true.
	 * @param inputString the chat message to check if fits
	 * @return if the inputs are all true and the outputs should be performed
	 */
	fun isMatch(inputString: String?): Boolean {
		//enabled
		if (!enabled) return false

		//ignore case
		val testString: String?
		val testFilter: String

		if (ignoreCase) {
			testString = inputString!!.lowercase(Locale.getDefault())
			testFilter = filter.lowercase(Locale.getDefault())
		} else {
			testString = inputString
			testFilter = filter
		}

		//filter
		if (testFilter.isBlank()) return false
		if (regex) {
			if (partialMatch) {
				if (!Pattern.compile(testFilter).matcher(testString).find()) return false
			} else {
				if (!testString!!.matches(testFilter.toRegex())) return false
			}
		} else {
			if (partialMatch) {
				if (!testString!!.contains(testFilter)) return false
			} else {
				if (testFilter != testString) return false
			}
		}

		//location
		if (validLocations.isBlank()) { //if no locations do not check
			return true
		}

		val cleanedMapLocation = map.lowercase(Locale.getDefault()).replace(" ", "")
		var isLocationValid: Boolean? = null
		for (validLocation in validLocations.replace(" ", "").lowercase(Locale.getDefault()).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) { //the locations are split by "," and start with ! if not locations
			if (validLocation == null) continue
			if (validLocation.startsWith("!")) { //not location
				if (validLocation.substring(1) == cleanedMapLocation) {
					isLocationValid = false
					break
				} else {
					isLocationValid = true
				}
			} else {
				if (validLocation == cleanedMapLocation) { //normal location
					isLocationValid = true
					break
				}
			}
		}

		//if location is not in the list at all and is a not a "!" location or and is a normal location
		if (isLocationValid != null && isLocationValid) {
			return true
		}

		return false
	}

	companion object {
		private val CODEC: Codec<ChatRule?> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ChatRule?> ->
			instance.group(
				Codec.STRING.fieldOf("name").forGetter { obj: ChatRule? -> obj!!.name },
				Codec.BOOL.fieldOf("enabled").forGetter { obj: ChatRule -> obj.enabled },
				Codec.BOOL.fieldOf("isPartialMatch").forGetter { obj: ChatRule -> obj.partialMatch },
				Codec.BOOL.fieldOf("isRegex").forGetter { obj: ChatRule -> obj.regex },
				Codec.BOOL.fieldOf("isIgnoreCase").forGetter { obj: ChatRule -> obj.ignoreCase },
				Codec.STRING.fieldOf("filter").forGetter { obj: ChatRule -> obj.filter },
				Codec.STRING.fieldOf("validLocations").forGetter { obj: ChatRule -> obj.validLocations },
				Codec.BOOL.fieldOf("hideMessage").forGetter { obj: ChatRule -> obj.hideMessage },
				Codec.BOOL.fieldOf("showActionBar").forGetter { obj: ChatRule -> obj.showActionBar },
				Codec.BOOL.fieldOf("showAnnouncement").forGetter { obj: ChatRule -> obj.showAnnouncement },
				Codec.STRING.optionalFieldOf("replaceMessage").forGetter { obj: ChatRule -> obj.replaceMessageOpt },
				SoundEvent.CODEC.optionalFieldOf("customSound").forGetter { obj: ChatRule -> obj.customSoundOpt })
				.apply(instance) { name: String, enabled: Boolean, isPartialMatch: Boolean, isRegex: Boolean, isIgnoreCase: Boolean, filter: String, validLocations: String, hideMessage: Boolean, showActionBar: Boolean, showAnnouncement: Boolean, replaceMessage: Optional<String?>, customSound: Optional<SoundEvent?> -> ChatRule(name, enabled, isPartialMatch, isRegex, isIgnoreCase, filter, validLocations, hideMessage, showActionBar, showAnnouncement, replaceMessage, customSound) }
		}
		val LIST_CODEC: Codec<List<ChatRule?>> = CODEC.listOf()
	}
}



