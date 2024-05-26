package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Pair

// shows the volcano status (crimson isle)
class VolcanoWidget : Widget(TITLE, Formatting.AQUA.colorValue) {
	override fun updateContent() {
		val s = PlayerListMgr.strAt(58)
		if (s == null) {
			this.addComponent(IcoTextComponent())
		} else {
			val p = BOOM_TYPE[s]!!
			this.addComponent(IcoTextComponent(p.left, Text.literal(s).formatted(p.right)))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Volcano Status").formatted(
			Formatting.AQUA,
			Formatting.BOLD
		)

		private val BOOM_TYPE = HashMap<String, Pair<ItemStack, Formatting>>()

		init {
			BOOM_TYPE["INACTIVE"] = Pair(ItemStack(Items.BARRIER), Formatting.DARK_GRAY)
			BOOM_TYPE["CHILL"] = Pair(ItemStack(Items.ICE), Formatting.AQUA)
			BOOM_TYPE["LOW"] = Pair(ItemStack(Items.FLINT_AND_STEEL), Formatting.GRAY)
			BOOM_TYPE["DISRUPTIVE"] = Pair(ItemStack(Items.CAMPFIRE), Formatting.WHITE)
			BOOM_TYPE["MEDIUM"] = Pair(ItemStack(Items.LAVA_BUCKET), Formatting.YELLOW)
			BOOM_TYPE["HIGH"] = Pair(ItemStack(Items.FIRE_CHARGE), Formatting.GOLD)
			BOOM_TYPE["EXPLOSIVE"] = Pair(ItemStack(Items.TNT), Formatting.RED)
			BOOM_TYPE["CATACLYSMIC"] = Pair(ItemStack(Items.SKELETON_SKULL), Formatting.DARK_RED)
		}
	}
}
