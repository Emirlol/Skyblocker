package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.MiningConfig.DwarvenHudStyle
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor
import de.hysky.skyblocker.skyblock.tabhud.util.Colors.pcntToCol
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget
import de.hysky.skyblocker.utils.Utils.isInCrystalHollows
import de.hysky.skyblocker.utils.Utils.isInDwarvenMines
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

object DwarvenHud {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private var commissionList: MutableList<Commission> = ArrayList()

	var mithrilPowder: String = "0"
	var gemStonePowder: String = "0"
	var glacitePowder: String = "0"

	@Language("RegExp")
	private val COMMISSIONS: List<Pattern> = sequenceOf(
		"(?:Titanium|Mithril|Hard Stone) Miner",
		"(?:Glacite Walker|Golden Goblin|(?<!Golden )Goblin|Goblin Raid|Treasure Hoarder|Automaton|Sludge|Team Treasurite Member|Yog|Boss Corleone|Thyst|Maniac|Mines) Slayer",
		"(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Mithril",
		"(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Titanium",
		"Goblin Raid",
		"(?:Star Sentry|Treasure Hoarder) Puncher",
		"(?<!Lucky )Raffle",
		"Lucky Raffle",
		"2x Mithril Powder Collector",
		"First Event",
		"(?:Ruby|Amber|Sapphire|Jade|Amethyst|Topaz|Onyx|Aquamarine|Citrine|Peridot) Gemstone Collector",
		"(?:Amber|Sapphire|Jade|Amethyst|Topaz) Crystal Hunter",
		"(?:Umber|Tungsten|Glacite|Scrap) Collector",
		"Mineshaft Explorer",
		"(?:Chest|Corpse) Looter"
	).map { Pattern.compile("($it): (\\d+\\.?\\d*%|DONE)") }.toList()
	private val MITHRIL_PATTERN: Pattern = Pattern.compile("Mithril: [0-9,]+")
	private val GEMSTONE_PATTERN: Pattern = Pattern.compile("Gemstone: [0-9,]+")
	private val GLACITE_PATTERN: Pattern = Pattern.compile("Glacite: [0-9,]+")

	fun init() {
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				ClientCommandManager.literal("skyblocker")
					.then(
						ClientCommandManager.literal("hud")
							.then(
								ClientCommandManager.literal("dwarven")
									.executes(Scheduler.queueOpenScreenCommand { DwarvenHudConfigScreen() })
							)
					)
			)
		}

		HudRenderEvents.AFTER_MAIN_HUD.register{ context: DrawContext, _ ->
			if (!SkyblockerConfigManager.config.mining.dwarvenHud.enabledCommissions && !SkyblockerConfigManager.config.mining.dwarvenHud.enabledPowder || CLIENT.options.playerListKey.isPressed || CLIENT.player == null || (!isInDwarvenMines && !isInCrystalHollows)) {
				return@register
			}
			render(
				HudCommsWidget.INSTANCE, HudPowderWidget.INSTANCE, context,
				SkyblockerConfigManager.config.mining.dwarvenHud.commissionsX,
				SkyblockerConfigManager.config.mining.dwarvenHud.commissionsY,
				SkyblockerConfigManager.config.mining.dwarvenHud.powderX,
				SkyblockerConfigManager.config.mining.dwarvenHud.powderY,
				commissionList
			)
		}
	}

	fun render(hcw: HudCommsWidget, hpw: HudPowderWidget, context: DrawContext, comHudX: Int, comHudY: Int, powderHudX: Int, powderHudY: Int, commissions: List<Commission>) {
		when (SkyblockerConfigManager.config.mining.dwarvenHud.style) {
			DwarvenHudStyle.SIMPLE -> renderSimple(hcw, hpw, context, comHudX, comHudY, powderHudX, powderHudY, commissions)
			DwarvenHudStyle.FANCY -> renderFancy(hcw, hpw, context, comHudX, comHudY, powderHudX, powderHudY, commissions)
			DwarvenHudStyle.CLASSIC -> renderClassic(context, comHudX, comHudY, powderHudX, powderHudY, commissions)
		}
	}

	/**
	 * Renders hud to window without using the widget rendering
	 * @param context DrawContext to draw the hud to
	 * @param comHudX X coordinate of the commissions hud
	 * @param comHudY Y coordinate of the commissions hud
	 * @param powderHudX X coordinate of the powder hud
	 * @param powderHudY Y coordinate of the powder hud
	 * @param commissions the commissions to render to the commissions hud
	 */
	@Deprecated("")
	private fun renderClassic(context: DrawContext, comHudX: Int, comHudY: Int, powderHudX: Int, powderHudY: Int, commissions: List<Commission>) {
		if (SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground) {
			context.fill(comHudX, comHudY, comHudX + 200, comHudY + (20 * commissions.size), 0x64000000)
			context.fill(powderHudX, powderHudY, powderHudX + 200, powderHudY + 40, 0x64000000)
		}
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledCommissions) {
			var y = 0
			for ((commission1, progression) in commissions) {
				val percentage = if (!progression.contains("DONE")) progression.substring(0, progression.length - 1).toFloat() else 100f

				context.drawTextWithShadow(
					CLIENT.textRenderer,
					Text.literal("$commission1: ").formatted(Formatting.AQUA)
						.append(Text.literal(progression).withColor(pcntToCol(percentage))),
					comHudX + 5, comHudY + y + 5, -0x1
				)
				y += 20
			}
		}
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledPowder) {
			//render mithril powder then gemstone
			context.drawTextWithShadow(
				CLIENT.textRenderer,
				Text.literal("Mithril: $mithrilPowder").formatted(Formatting.AQUA),
				powderHudX + 5, powderHudY + 5, -0x1
			)
			context.drawTextWithShadow(
				CLIENT.textRenderer,
				Text.literal("Gemstone: $gemStonePowder").formatted(Formatting.DARK_PURPLE),
				powderHudX + 5, powderHudY + 25, -0x1
			)
		}
	}

	private fun renderSimple(hcw: HudCommsWidget, hpw: HudPowderWidget, context: DrawContext, comHudX: Int, comHudY: Int, powderHudX: Int, powderHudY: Int, commissions: List<Commission>) {
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledCommissions) {
			hcw.updateData(commissions, false)
			hcw.update()
			hcw.x = comHudX
			hcw.y = comHudY
			hcw.render(context, SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground)
		}
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledPowder) {
			hpw.update()
			hpw.x = powderHudX
			hpw.y = powderHudY
			hpw.render(context, SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground)
		}
	}

	private fun renderFancy(hcw: HudCommsWidget, hpw: HudPowderWidget, context: DrawContext, comHudX: Int, comHudY: Int, powderHudX: Int, powderHudY: Int, commissions: List<Commission>) {
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledCommissions) {
			hcw.updateData(commissions, true)
			hcw.update()
			hcw.x = comHudX
			hcw.y = comHudY
			hcw.render(context, SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground)
		}
		if (SkyblockerConfigManager.config.mining.dwarvenHud.enabledPowder) {
			hpw.update()
			hpw.x = powderHudX
			hpw.y = powderHudY
			hpw.render(context, SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground)
		}
	}

	fun update() {
		if (CLIENT.player == null || CLIENT.networkHandler == null || !SkyblockerConfigManager.config.mining.dwarvenHud.enabledCommissions && !SkyblockerConfigManager.config.mining.dwarvenHud.enabledPowder || !isInCrystalHollows && !isInDwarvenMines) {
			return
		}
		val oldCommissionNames = commissionList.map(Commission::commission)
		val oldCompleted = commissionList.any { it.progression == "DONE" }
		commissionList = ArrayList()
		for (playerListEntry in CLIENT.networkHandler!!.playerList.asSequence().sortedWith(PlayerListHudAccessor.getOrdering())) {
			playerListEntry.displayName ?: continue

			//find commissions
			val name = playerListEntry.displayName!!.string.trim()
			for (pattern in COMMISSIONS) {
				val matcher = pattern.matcher(name)
				if (matcher.matches()) {
					commissionList += Commission(matcher.group(1), matcher.group(2))
				}
			}
			//find powder
			val mithrilMatcher = MITHRIL_PATTERN.matcher(name)
			if (mithrilMatcher.matches()) {
				mithrilPowder = mithrilMatcher.group(0).split(": ").dropLastWhile { it.isEmpty() }[1]
			}
			val gemstoneMatcher = GEMSTONE_PATTERN.matcher(name)
			if (gemstoneMatcher.matches()) {
				gemStonePowder = gemstoneMatcher.group(0).split(": ").dropLastWhile { it.isEmpty() }[1]
			}
			val glaciteMatcher = GLACITE_PATTERN.matcher(name)
			if (glaciteMatcher.matches()) {
				glacitePowder = glaciteMatcher.group(0).split(": ").dropLastWhile { it.isEmpty() }[1]
			}
		}
		val newCommissionNames = commissionList.map(Commission::commission)
		val newCompleted = commissionList.any { it.progression == "DONE" }
		if (oldCommissionNames != newCommissionNames || oldCompleted != newCompleted) {
			CommissionLabels.update(newCommissionNames, newCompleted)
		}
	}

	// steamroller tactics to get visibility from outside classes (HudCommsWidget)
	data class Commission(val commission: String, val progression: String)
}