package de.hysky.skyblocker

import de.hysky.skyblocker.utils.Constants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConstantsTest {
	@Test
	fun testPrefix() {
		val time = java.time.LocalDate.now()
		// Yes, this test won't work on April 1st. I'm sorry.
		if (time.monthValue != 4 || time.dayOfMonth != 1) {
			Assertions.assertEquals("empty[siblings=[literal{[}[style={color=gray}], literal{S}[style={color=#00FF4C}], literal{k}[style={color=#02FA60}], literal{y}[style={color=#04F574}], literal{b}[style={color=#07EF88}], literal{l}[style={color=#09EA9C}], literal{o}[style={color=#0BE5AF}], literal{c}[style={color=#0DE0C3}], literal{k}[style={color=#10DAD7}], literal{e}[style={color=#12D5EB}], literal{r}[style={color=#14D0FF}], literal{] }[style={color=gray}]]]", Constants.PREFIX.get().toString())
		}
	}
}