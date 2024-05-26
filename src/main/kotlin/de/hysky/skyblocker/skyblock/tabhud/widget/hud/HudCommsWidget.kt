package de.hysky.skyblocker.skyblock.tabhud.widget.hud

import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission
import de.hysky.skyblocker.skyblock.tabhud.util.Colors
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!
class HudCommsWidget  // another repulsive hack to make this widget-like hud element work with the new widget class
// DON'T USE WITH THE WIDGET SYSTEM, ONLY USE FOR DWARVENHUD!
	: Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	private var commissions: List<Commission>? = null
	private var isFancy = false

	fun updateData(commissions: List<Commission>?, isFancy: Boolean) {
		this.commissions = commissions
		this.isFancy = isFancy
	}

	override fun updateContent() {
		for (comm in commissions!!) {
			val c: Text = Text.literal(comm.commission)

			var p = 100f
			if (!comm.progression.contains("DONE")) {
				p = comm.progression.substring(0, comm.progression.length - 1).toFloat()
			}
			var comp = if (isFancy) {
				ProgressComponent(Ico.BOOK, c, p, Colors.pcntToCol(p))
			} else {
				PlainTextComponent(
					Text.literal(comm.commission + ": ").append(
						Text.literal(comm.progression).withColor(Colors.pcntToCol(p))
					)
				)
			}
			this.addComponent(comp)
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Commissions").formatted(Formatting.DARK_AQUA, Formatting.BOLD)

		// disgusting hack to get around text renderer issues.
		// the ctor eventually tries to get the font's height, which doesn't work
		//   when called before the client window is created (roughly).
		// the rebdering god 2 from the fabricord explained that detail, thanks!
		@JvmField
		val INSTANCE: HudCommsWidget = HudCommsWidget()
		@JvmField
		val INSTANCE_CFG: HudCommsWidget = HudCommsWidget()
	}
}
