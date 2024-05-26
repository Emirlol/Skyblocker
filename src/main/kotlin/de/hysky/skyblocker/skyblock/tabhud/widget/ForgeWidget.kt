package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows what you're forging right now.
// for locked slots, the unlock requirement is shown
class ForgeWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		var forgestart = 54
		// why is it forges and not looms >:(
		val pos = PlayerListMgr.strAt(53)
		if (pos == null) {
			this.addComponent(IcoTextComponent())
			return
		}

		if (!pos.startsWith("Forges")) {
			forgestart += 2
		}

		var i = forgestart
		var slot = 1
		while (i < forgestart + 5 && i < 60) {
			val fstr = PlayerListMgr.strAt(i)
			if (fstr == null || fstr.length < 3) {
				if (i == forgestart) {
					this.addComponent(IcoTextComponent())
				}
				break
			}
			var c: Component
			var l1: Text?
			var l2: Text?

			when (fstr.substring(3)) {
				"LOCKED" -> {
					l1 = Text.literal("Locked").formatted(Formatting.RED)
					l2 = when (slot) {
						3 -> Text.literal("Needs HotM 3").formatted(Formatting.GRAY)
						4 -> Text.literal("Needs HotM 4").formatted(Formatting.GRAY)
						5 -> Text.literal("Needs PotM 2").formatted(Formatting.GRAY)
						else -> Text.literal("This message should not appear").formatted(Formatting.RED, Formatting.BOLD)
					}
					c = IcoFatTextComponent(Ico.BARRIER, l1, l2)
				}

				"EMPTY" -> {
					l1 = Text.literal("Empty").formatted(Formatting.GRAY)
					c = IcoTextComponent(Ico.FURNACE, l1)
				}

				else -> {
					val parts = fstr.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
					if (parts.size != 2) {
						c = IcoFatTextComponent()
					} else {
						l1 = Text.literal(parts[0].substring(3)).formatted(Formatting.YELLOW)
						l2 = if (parts[1] == "Ready!") {
							Text.literal("Done!").formatted(Formatting.GREEN)
						} else {
							Text.literal("Done in: ").formatted(Formatting.GRAY).append(Text.literal(parts[1]).formatted(Formatting.WHITE))
						}
						c = IcoFatTextComponent(Ico.FIRE, l1, l2)
					}
				}
			}
			this.addComponent(c)
			i++
			slot++
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Forge Status").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)
	}
}
