package de.hysky.skyblocker.utils

import java.util.*
import java.util.regex.Matcher

object RegexUtils {
	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	@JvmStatic
	fun getLongFromMatcher(matcher: Matcher): OptionalLong {
		return getLongFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)
	}

	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	fun getLongFromMatcher(matcher: Matcher, startingIndex: Int): OptionalLong {
		if (!matcher.find(startingIndex)) return OptionalLong.empty()
		return OptionalLong.of(matcher.group(1).replace(",", "").toLong())
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	@JvmStatic
	fun getIntFromMatcher(matcher: Matcher): OptionalInt {
		return getIntFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)
	}

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	@JvmStatic
	fun getIntFromMatcher(matcher: Matcher, startingIndex: Int): OptionalInt {
		if (!matcher.find(startingIndex)) return OptionalInt.empty()
		return OptionalInt.of(matcher.group(1).replace(",", "").toInt())
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	@JvmStatic
	fun getDoubleFromMatcher(matcher: Matcher): OptionalDouble {
		return getDoubleFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)
	}

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	@JvmStatic
	fun getDoubleFromMatcher(matcher: Matcher, startingIndex: Int): OptionalDouble {
		if (!matcher.find(startingIndex)) return OptionalDouble.empty()
		return OptionalDouble.of(matcher.group(1).replace(",", "").toDouble())
	}
}
