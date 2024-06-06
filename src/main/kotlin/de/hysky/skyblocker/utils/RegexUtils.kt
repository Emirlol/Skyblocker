package de.hysky.skyblocker.utils

import java.util.regex.Matcher

object RegexUtils {
	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	fun getLongFromMatcher(matcher: Matcher) = getLongFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)

	/**
	 * @return An OptionalLong of the first group in the matcher, or an empty OptionalLong if the matcher doesn't find anything.
	 */
	fun getLongFromMatcher(matcher: Matcher, startingIndex: Int) = if (!matcher.find(startingIndex)) null
		else matcher.group(1).replace(",", "").toLong()

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	fun getIntFromMatcher(matcher: Matcher) = getIntFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)

	/**
	 * @return An OptionalInt of the first group in the matcher, or an empty OptionalInt if the matcher doesn't find anything.
	 */
	fun getIntFromMatcher(matcher: Matcher, startingIndex: Int) = if (!matcher.find(startingIndex)) null
		else matcher.group(1).replace(",", "").toInt()

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	fun getDoubleFromMatcher(matcher: Matcher) = getDoubleFromMatcher(matcher, if (matcher.hasMatch()) matcher.end() else 0)

	/**
	 * @return An OptionalDouble of the first group in the matcher, or an empty OptionalDouble if the matcher doesn't find anything.
	 * @implNote Assumes the decimal separator is `.`
	 */
	fun getDoubleFromMatcher(matcher: Matcher, startingIndex: Int) = if (!matcher.find(startingIndex)) null
		else matcher.group(1).replace(",", "").toDouble()
}
