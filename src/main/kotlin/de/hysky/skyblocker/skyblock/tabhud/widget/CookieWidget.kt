package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about active super cookies
// or not, if you're unwilling to buy one
class CookieWidget : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val footertext = PlayerListMgr.getFooter()
		if (footertext == null || !footertext.contains("Cookie Buff")) {
			this.addComponent(IcoTextComponent())
			return
		}

		val m = COOKIE_PATTERN.matcher(footertext)
		if (!m.find() || m.group("buff") == null) {
			this.addComponent(IcoTextComponent())
			return
		}

		val buff = m.group("buff")
		if (buff.startsWith("Not")) {
			this.addComponent(IcoTextComponent(Ico.COOKIE, Text.of("Not active")))
		} else {
			val cookie: Text = Text.literal("Time Left: ").append(buff)
			this.addComponent(IcoTextComponent(Ico.COOKIE, cookie))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Cookie Info").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		private val COOKIE_PATTERN: Pattern = Pattern.compile(".*\\nCookie Buff\\n(?<buff>.*)\\n")
	}
}
