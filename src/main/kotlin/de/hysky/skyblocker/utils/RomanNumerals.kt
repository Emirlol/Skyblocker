package de.hysky.skyblocker.utils

object RomanNumerals {
	private val romanMap = mapOf(
		'I' to 1,
		'V' to 5,
		'X' to 10,
		'L' to 50,
		'C' to 100,
		'D' to 500,
		'M' to 1000
	)

	fun romanToDecimal(romanNumeral: String): Int? {
		if (!isValidRomanNumeral(romanNumeral)) return null
		var decimal = 0
		var lastNumber = 0
		for (i in romanNumeral.length - 1 downTo 0) {
			val value = romanMap[romanNumeral[i]]!!
			decimal = if (value >= lastNumber) decimal + value else decimal - value
			lastNumber = value
		}
		return decimal
	}

	private fun isValidRomanNumeral(romanNumeral: String?): Boolean {
		if (romanNumeral.isNullOrEmpty()) return false
		for (char in romanNumeral) {
			if (!romanMap.containsKey(char)) return false
		}
		return true
	}
}