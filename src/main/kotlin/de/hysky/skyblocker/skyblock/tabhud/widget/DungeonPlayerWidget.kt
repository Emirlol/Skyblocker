package de.hysky.skyblocker.skyblock.tabhud.widget

import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

// this widget shows info about a player in the current dungeon group
class DungeonPlayerWidget // title needs to be changeable here
	(private val player: Int) : Widget(TITLE, Formatting.DARK_PURPLE.colorValue) {
	override fun updateContent() {
		val start = 1 + (player - 1) * 4

		if (PlayerListMgr.strAt(start) == null) {
			val idx = player - 2
			val noplayer = IcoTextComponent(
				Ico.SIGN,
				Text.literal(MSGS[idx]).formatted(Formatting.GRAY)
			)
			this.addComponent(noplayer)
			return
		}
		val m = PlayerListMgr.regexAt(start, PLAYER_PATTERN)
		if (m == null) {
			this.addComponent(IcoTextComponent())
			this.addComponent(IcoTextComponent())
		} else {
			val name: Text = Text.literal("Name: ").append(Text.literal(m.group("name")).formatted(Formatting.YELLOW))
			this.addComponent(IcoTextComponent(Ico.PLAYER, name))

			var cl = m.group("class")
			val level = m.group("level")

			if (level == null) {
				val ptc = PlainTextComponent(
					Text.literal("Player is dead").formatted(Formatting.RED)
				)
				this.addComponent(ptc)
			} else {
				var clf = Formatting.GRAY
				var cli = Ico.BARRIER
				if (cl != "EMPTY") {
					cli = ICOS[cl]
					clf = Formatting.LIGHT_PURPLE
					cl += " " + m.group("level")
				}

				val clazz: Text = Text.literal("Class: ").append(Text.literal(cl).formatted(clf))
				val itclass = IcoTextComponent(cli, clazz)
				this.addComponent(itclass)
			}
		}

		this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", Formatting.GOLD, start + 1)
		this.addSimpleIcoText(Ico.POTION, "Revives:", Formatting.DARK_PURPLE, start + 2)
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Player").formatted(
			Formatting.DARK_PURPLE,
			Formatting.BOLD
		)

		// match a player entry
		// group 1: name
		// group 2: class (or literal "EMPTY" pre dungeon start)
		// group 3: level (or nothing, if pre dungeon start)
		// this regex filters out the ironman icon as well as rank prefixes and emblems
		// \[\d*\] (?:\[[A-Za-z]+\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\((?<class>\S*) ?(?<level>[LXVI]*)\)
		val PLAYER_PATTERN: Pattern = Pattern.compile("\\[\\d*] (?:\\[[A-Za-z]+] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\\((?<class>\\S*) ?(?<level>[LXVI]*)\\)")

		private val ICOS = HashMap<String, ItemStack>()
		private val MSGS = ArrayList<String>()

		init {
			ICOS["Tank"] = Ico.CHESTPLATE
			ICOS["Mage"] = Ico.B_ROD
			ICOS["Berserk"] = Ico.DIASWORD
			ICOS["Archer"] = Ico.BOW
			ICOS["Healer"] = Ico.POTION

			MSGS.add("PRESS A TO JOIN")
			MSGS.add("Invite a friend!")
			MSGS.add("But nobody came.")
			MSGS.add("More is better!")
		}
	}
}
