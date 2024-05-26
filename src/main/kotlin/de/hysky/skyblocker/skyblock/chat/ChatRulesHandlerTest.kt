package de.hysky.skyblocker.skyblock.chat

import net.minecraft.text.MutableText
import org.junit.jupiter.api.Assertions

internal class ChatRulesHandlerTest {
	@Test
	fun formatText() {
		//generate test text
		val testText: MutableText = net.minecraft.text.Text.empty()
		var style = net.minecraft.text.Style.EMPTY.withFormatting(net.minecraft.util.Formatting.DARK_BLUE)
		net.minecraft.text.Text.of("test").getWithStyle(style).forEach(java.util.function.Consumer { text: net.minecraft.text.Text? -> testText.append(text) })
		style = style.withFormatting(net.minecraft.util.Formatting.UNDERLINE)
		net.minecraft.text.Text.of("line").getWithStyle(style).forEach(java.util.function.Consumer { text: net.minecraft.text.Text? -> testText.append(text) })
		style = style.withFormatting(net.minecraft.util.Formatting.DARK_GREEN)
		net.minecraft.text.Text.of("dark green").getWithStyle(style).forEach(java.util.function.Consumer { text: net.minecraft.text.Text? -> testText.append(text) })
		style = style.withFormatting(net.minecraft.util.Formatting.ITALIC)
		net.minecraft.text.Text.of("italic").getWithStyle(style).forEach(java.util.function.Consumer { text: net.minecraft.text.Text? -> testText.append(text) })

		//generated text
		val text: MutableText? = ChatRulesHandler.formatText("&1test&nline&2dark green&oitalic")

		Assertions.assertEquals(text, testText)
	}
}