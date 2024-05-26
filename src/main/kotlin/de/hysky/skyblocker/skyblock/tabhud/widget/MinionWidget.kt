package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about minions placed on the home island
class MinionWidget : Widget(TITLE, Formatting.DARK_AQUA.colorValue) {
	override fun updateContent() {
		// this looks a bit weird because if we used regex mismatch as a stop condition,
		//   it'd spam the chat.
		// not sure if not having that debug output is worth the cleaner solution here...

		for (i in 48..58) {
			if (!this.addMinionComponent(i)) {
				break
			}
		}

		// if more minions are placed than the tab menu can display,
		// a "And X more..." text is shown
		// look for that and add it to the widget
		val more = PlayerListMgr.strAt(59)
		if (more == null) {
			return
		} else if (more.startsWith("And ")) {
			this.addComponent(PlainTextComponent(Text.of(more)))
		} else {
			this.addMinionComponent(59)
		}
	}

	fun addMinionComponent(i: Int): Boolean {
		val m = PlayerListMgr.regexAt(i, MINION_PATTERN)
		if (m != null) {
			val min = m.group("name")
			val lvl = m.group("level")
			val stat = m.group("status")

			val mt = Text.literal("$min $lvl").append(Text.literal(": "))

			var format = Formatting.RED
			if (stat == "ACTIVE") {
				format = Formatting.GREEN
			} else if (stat == "SLOW") {
				format = Formatting.YELLOW
			}
			// makes "BLOCKED" also red. in reality, it's some kind of crimson
			mt.append(Text.literal(stat).formatted(format))

			val itc = IcoTextComponent(MIN_ICOS[min], mt)
			this.addComponent(itc)
			return true
		} else {
			return false
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Minions").formatted(
			Formatting.DARK_AQUA,
			Formatting.BOLD
		)

		private val MIN_ICOS = HashMap<String, ItemStack>()

		// hmm...
		init {
			MIN_ICOS["Blaze"] = ItemStack(Items.BLAZE_ROD)
			MIN_ICOS["Cave Spider"] = ItemStack(Items.SPIDER_EYE)
			MIN_ICOS["Creeper"] = ItemStack(Items.GUNPOWDER)
			MIN_ICOS["Enderman"] = ItemStack(Items.ENDER_PEARL)
			MIN_ICOS["Ghast"] = ItemStack(Items.GHAST_TEAR)
			MIN_ICOS["Magma Cube"] = ItemStack(Items.MAGMA_CREAM)
			MIN_ICOS["Skeleton"] = ItemStack(Items.BONE)
			MIN_ICOS["Slime"] = ItemStack(Items.SLIME_BALL)
			MIN_ICOS["Spider"] = ItemStack(Items.STRING)
			MIN_ICOS["Zombie"] = ItemStack(Items.ROTTEN_FLESH)
			MIN_ICOS["Cactus"] = ItemStack(Items.CACTUS)
			MIN_ICOS["Carrot"] = ItemStack(Items.CARROT)
			MIN_ICOS["Chicken"] = ItemStack(Items.CHICKEN)
			MIN_ICOS["Cocoa Beans"] = ItemStack(Items.COCOA_BEANS)
			MIN_ICOS["Cow"] = ItemStack(Items.BEEF)
			MIN_ICOS["Melon"] = ItemStack(Items.MELON_SLICE)
			MIN_ICOS["Mushroom"] = ItemStack(Items.RED_MUSHROOM)
			MIN_ICOS["Nether Wart"] = ItemStack(Items.NETHER_WART)
			MIN_ICOS["Pig"] = ItemStack(Items.PORKCHOP)
			MIN_ICOS["Potato"] = ItemStack(Items.POTATO)
			MIN_ICOS["Pumpkin"] = ItemStack(Items.PUMPKIN)
			MIN_ICOS["Rabbit"] = ItemStack(Items.RABBIT)
			MIN_ICOS["Sheep"] = ItemStack(Items.WHITE_WOOL)
			MIN_ICOS["Sugar Cane"] = ItemStack(Items.SUGAR_CANE)
			MIN_ICOS["Wheat"] = ItemStack(Items.WHEAT)
			MIN_ICOS["Clay"] = ItemStack(Items.CLAY)
			MIN_ICOS["Fishing"] = ItemStack(Items.FISHING_ROD)
			MIN_ICOS["Coal"] = ItemStack(Items.COAL)
			MIN_ICOS["Cobblestone"] = ItemStack(Items.COBBLESTONE)
			MIN_ICOS["Diamond"] = ItemStack(Items.DIAMOND)
			MIN_ICOS["Emerald"] = ItemStack(Items.EMERALD)
			MIN_ICOS["End Stone"] = ItemStack(Items.END_STONE)
			MIN_ICOS["Glowstone"] = ItemStack(Items.GLOWSTONE_DUST)
			MIN_ICOS["Gold"] = ItemStack(Items.GOLD_INGOT)
			MIN_ICOS["Gravel"] = ItemStack(Items.GRAVEL)
			MIN_ICOS["Hard Stone"] = ItemStack(Items.STONE)
			MIN_ICOS["Ice"] = ItemStack(Items.ICE)
			MIN_ICOS["Iron"] = ItemStack(Items.IRON_INGOT)
			MIN_ICOS["Lapis"] = ItemStack(Items.LAPIS_LAZULI)
			MIN_ICOS["Mithril"] = ItemStack(Items.PRISMARINE_CRYSTALS)
			MIN_ICOS["Mycelium"] = ItemStack(Items.MYCELIUM)
			MIN_ICOS["Obsidian"] = ItemStack(Items.OBSIDIAN)
			MIN_ICOS["Quartz"] = ItemStack(Items.QUARTZ)
			MIN_ICOS["Red Sand"] = ItemStack(Items.RED_SAND)
			MIN_ICOS["Redstone"] = ItemStack(Items.REDSTONE)
			MIN_ICOS["Sand"] = ItemStack(Items.SAND)
			MIN_ICOS["Snow"] = ItemStack(Items.SNOWBALL)
			MIN_ICOS["Inferno"] = ItemStack(Items.BLAZE_SPAWN_EGG)
			MIN_ICOS["Revenant"] = ItemStack(Items.ZOMBIE_SPAWN_EGG)
			MIN_ICOS["Tarantula"] = ItemStack(Items.SPIDER_SPAWN_EGG)
			MIN_ICOS["Vampire"] = ItemStack(Items.REDSTONE)
			MIN_ICOS["Voidling"] = ItemStack(Items.ENDERMAN_SPAWN_EGG)
			MIN_ICOS["Acacia"] = ItemStack(Items.ACACIA_LOG)
			MIN_ICOS["Birch"] = ItemStack(Items.BIRCH_LOG)
			MIN_ICOS["Dark Oak"] = ItemStack(Items.DARK_OAK_LOG)
			MIN_ICOS["Flower"] = ItemStack(Items.POPPY)
			MIN_ICOS["Jungle"] = ItemStack(Items.JUNGLE_LOG)
			MIN_ICOS["Oak"] = ItemStack(Items.OAK_LOG)
			MIN_ICOS["Spruce"] = ItemStack(Items.SPRUCE_LOG)
		}

		// matches a minion entry
		// group 1: name
		// group 2: level
		// group 3: status
		val MINION_PATTERN: Pattern = Pattern.compile("(?<name>.*) (?<level>[XVI]*) \\[(?<status>.*)\\]")
	}
}
