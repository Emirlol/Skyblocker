package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about the current jacob's contest (garden only)
class JacobsContestWidget : Widget(TITLE, Formatting.YELLOW.colorValue) {
	override fun updateContent() {
		val jacobStatus = PlayerListMgr.textAt(76)

		if (jacobStatus!!.string == "ACTIVE") {
			this.addComponent(IcoTextComponent(Ico.CLOCK, jacobStatus))
		} else {
			this.addSimpleIcoText(Ico.CLOCK, "Starts in:", Formatting.GOLD, 76)
		}

		val tc = TableComponent(1, 3, Formatting.YELLOW.colorValue!!)

		for (i in 77..79) {
			val item = PlayerListMgr.regexAt(i, CROP_PATTERN)
			var itc: IcoTextComponent
			if (item == null) {
				itc = IcoTextComponent()
			} else {
				val cropName = item.group("crop").trim { it <= ' ' } //Trimming is needed because during a contest the space separator will be caught
				itc = if (item.group("fortune") == "☘") {
					IcoTextComponent(FARM_DATA[cropName], Text.literal(cropName).append(Text.literal(" ☘").formatted(Formatting.GOLD)))
				} else {
					IcoTextComponent(FARM_DATA[cropName], Text.of(cropName))
				}
			}
			tc.addToCell(0, i - 77, itc)
		}
		this.addComponent(tc)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Jacob's Contest").formatted(
			Formatting.YELLOW,
			Formatting.BOLD
		)

		//TODO Properly match the contest placement and display it
		private val CROP_PATTERN: Pattern = Pattern.compile("(?<fortune>[☘○]) (?<crop>[A-Za-z ]+).*")

		private val FARM_DATA: Map<String, ItemStack> = java.util.Map.ofEntries(
			java.util.Map.entry("Wheat", ItemStack(Items.WHEAT)),
			java.util.Map.entry("Sugar Cane", ItemStack(Items.SUGAR_CANE)),
			java.util.Map.entry("Carrot", ItemStack(Items.CARROT)),
			java.util.Map.entry("Potato", ItemStack(Items.POTATO)),
			java.util.Map.entry("Melon", ItemStack(Items.MELON_SLICE)),
			java.util.Map.entry("Pumpkin", ItemStack(Items.PUMPKIN)),
			java.util.Map.entry("Cocoa Beans", ItemStack(Items.COCOA_BEANS)),
			java.util.Map.entry("Nether Wart", ItemStack(Items.NETHER_WART)),
			java.util.Map.entry("Cactus", ItemStack(Items.CACTUS)),
			java.util.Map.entry("Mushroom", ItemStack(Items.RED_MUSHROOM))
		)
	}
}
