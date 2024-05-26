package de.hysky.skyblocker

import de.hysky.skyblocker.utils.Calculator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CalculatorTest {
	@Test
	fun testShorthands() {
		Assertions.assertEquals(Calculator.calculate("1k"), 1000)
		Assertions.assertEquals(Calculator.calculate("0.12k"), 120)
		Assertions.assertEquals(Calculator.calculate("1k + 0.12k"), 1120)
		Assertions.assertEquals(Calculator.calculate("1 + 1s + 1k + 1m + 1b"), 1001001065)
	}

	@Test
	fun testPrecedence() {
		Assertions.assertEquals(Calculator.calculate("5 + 2 * 2"), 9)
		Assertions.assertEquals(Calculator.calculate("5 - 2 / 2"), 4)
		Assertions.assertEquals(Calculator.calculate("5 * (1 + 2)"), 15)
	}

	@Test
	fun testImplicitMultiplication() {
		Assertions.assertEquals(Calculator.calculate("5(2 + 2)"), 20)
	}

	@Test
	fun testImplicitClosingParenthesis() {
		Assertions.assertEquals(Calculator.calculate("5(2 + 2"), 20)
	}
}