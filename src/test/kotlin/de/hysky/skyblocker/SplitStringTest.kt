package de.hysky.skyblocker

import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SplitStringTest {
	@Test
	fun testSplitString1() {
		val input = "aaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa a"
		val split = SearchOverManager.splitString(input)
		Assertions.assertEquals("aaaaaaaaaaaaaaa", split.left())
		Assertions.assertEquals("aaaaaaaaaaaaaaa", split.right())
	}

	@Test
	fun testSplitString2() {
		val input = "a a a a aaa aa aa aaaa aaa aa aa aa a a aa aaa a aaa aa"
		val split = SearchOverManager.splitString(input)
		Assertions.assertEquals("a a a a aaa aa", split.left())
		Assertions.assertEquals("aa aaaa aaa aa", split.right())
	}

	@Test
	fun testSplitString3() {
		val input = "aaaaa aaaaa aaaaa"
		val split = SearchOverManager.splitString(input)
		Assertions.assertEquals("aaaaa aaaaa", split.left())
		Assertions.assertEquals("aaaaa", split.right())
	}
}
