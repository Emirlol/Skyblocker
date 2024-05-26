package de.hysky.skyblocker.skyblock.chat

import org.junit.jupiter.api.Assertions

internal class ChatRuleTest {
	@get:Test
	val isMatch: Unit
		get() {
			var testRule = ChatRule()
			//test enabled check
			testRule.filter = "test"
			testRule.enabled = false
			Assertions.assertEquals(testRule.isMatch("test"), false)
			//test simple filter works
			testRule.enabled = true
			Assertions.assertEquals(testRule.isMatch("test"), true)
			//test partial match works
			Assertions.assertEquals(testRule.isMatch("test extra"), false)
			testRule.partialMatch = true
			Assertions.assertEquals(testRule.isMatch("test extra"), true)
			//test ignore case works
			Assertions.assertEquals(testRule.isMatch("TEST"), true)
			testRule.ignoreCase = false
			Assertions.assertEquals(testRule.isMatch("TEST"), false)

			//test regex
			testRule = ChatRule()
			testRule.regex = true
			testRule.filter = "[0-9]+"
			Assertions.assertEquals(testRule.isMatch("1234567"), true)
			Assertions.assertEquals(testRule.isMatch("1234567 test"), false)
		}
}