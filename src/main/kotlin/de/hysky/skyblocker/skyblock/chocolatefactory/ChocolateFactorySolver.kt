package de.hysky.skyblocker.skyblock.chocolatefactory

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip.createSmoothLine
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.RegexUtils
import de.hysky.skyblocker.utils.RegexUtils.getDoubleFromMatcher
import de.hysky.skyblocker.utils.RegexUtils.getIntFromMatcher
import de.hysky.skyblocker.utils.RegexUtils.getLongFromMatcher
import de.hysky.skyblocker.utils.RomanNumerals.romanToDecimal
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.yellow
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil

object ChocolateFactorySolver : ContainerSolver("^Chocolate Factory$") {
	private val CPS_PATTERN: Pattern = Pattern.compile("([\\d,.]+) Chocolate per second")
	private val CPS_INCREASE_PATTERN: Pattern = Pattern.compile("\\+([\\d,]+) Chocolate per second")
	private val COST_PATTERN: Pattern = Pattern.compile("Cost ([\\d,]+) Chocolate")
	private val TOTAL_MULTIPLIER_PATTERN: Pattern = Pattern.compile("Total Multiplier: ([\\d.]+)x")
	private val MULTIPLIER_INCREASE_PATTERN: Pattern = Pattern.compile("\\+([\\d.]+)x Chocolate per second")
	private val CHOCOLATE_PATTERN: Pattern = Pattern.compile("^([\\d,]+) Chocolate$")
	private val PRESTIGE_REQUIREMENT_PATTERN: Pattern = Pattern.compile("Chocolate this Prestige: ([\\d,]+) +Requires (\\S+) Chocolate this Prestige!")
	private val TIME_TOWER_STATUS_PATTERN: Pattern = Pattern.compile("Status: (ACTIVE|INACTIVE)")

	private val cpsIncreaseFactors = mutableListOf<Rabbit>()
	private var totalChocolate: Long? = null
	private var totalCps: Double? = null
	private var totalCpsMultiplier: Double? = null
	private var requiredUntilNextPrestige: Long? = null
	private var timeTowerMultiplier: Double? = null
	private var isTimeTowerActive = false
	private val DECIMAL_FORMAT = DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
	private var bestUpgrade: Int? = null
	private var bestAffordableUpgrade: Int? = null

	//Slots, for ease of maintenance rather than using magic numbers everywhere.
	private const val RABBITS_START = 28;
	private const val RABBITS_END = 34;
	private const val COACH_SLOT = 42;
	private const val CHOCOLATE_SLOT = 13;
	private const val CPS_SLOT = 45;
	private const val PRESTIGE_SLOT = 27;
	private const val TIME_TOWER_SLOT = 39;
	private const val STRAY_RABBIT_START = 0;
	private const val STRAY_RABBIT_END = 26;

	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.config.helpers.chocolateFactory.enableChocolateFactoryHelper

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		updateFactoryInfo(slots)
		val highlights = arrayListOf<ColorHighlight>()

		getPrestigeHighlight(slots[28].stack)?.let { highlights.add(it) }

		if (totalChocolate == null || cpsIncreaseFactors.isEmpty()) return highlights //Something went wrong or there's nothing we can afford.

		val bestRabbit = cpsIncreaseFactors.first()
		bestUpgrade = bestRabbit.itemStack
		if (bestRabbit.cost <= totalChocolate!!) {
			highlights += green(bestRabbit.slot)
			return highlights
		}
		highlights += yellow(bestRabbit.slot)

		cpsIncreaseFactors.subList(1, cpsIncreaseFactors.size).find { it.cost <= totalChocolate!! }?.let {
			bestUpgrade = it.itemStack
			highlights += green(it.slot)
		}

		return highlights
	}

	private data class Rabbit(val cpsIncrease: Double, val cost: Int, val slot: Int)

	private fun updateFactoryInfo(slots: List<Slot>) {
		cpsIncreaseFactors.clear()

		for (i in RABBITS_START..RABBITS_END) { // The 7 rabbits slots are in 28, 29, 30, 31, 32, 33 and 34.
			val item = slots[i].stack
			if (item.isOf(Items.PLAYER_HEAD)) {
				getRabbit(item, i)?.let { cpsIncreaseFactors += it }
			}
		}

		//Coach is in slot 42
		getCoach(slots[COACH_SLOT].stack)?.let { cpsIncreaseFactors += it }

		//The clickable chocolate is in slot 13, holds the total chocolate
		totalChocolate = getLongFromMatcher(CHOCOLATE_PATTERN.matcher(slots[CHOCOLATE_SLOT].stack.name.string))

		//Cps item (cocoa bean) is in slot 45
		val cpsItemLore = concatenateLore(slots[CPS_SLOT].stack)
		val cpsMatcher = CPS_PATTERN.matcher(cpsItemLore)
		totalCps = getDoubleFromMatcher(cpsMatcher)
		val multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore)
		totalCpsMultiplier = getDoubleFromMatcher(multiplierMatcher, if (cpsMatcher.hasMatch()) cpsMatcher.end() else 0)

		//Prestige item is in slot 28
		val prestigeMatcher = PRESTIGE_REQUIREMENT_PATTERN.matcher(concatenateLore(slots[PRESTIGE_SLOT].stack))
		val currentChocolate = getLongFromMatcher(prestigeMatcher)
		if (currentChocolate != null) {
			val requirement = prestigeMatcher.group(2) //If the first one matched, we can assume the 2nd one is also matched since it's one whole regex
			//Since the last character is either M or B we can just try to replace both characters. Only the correct one will actually replace anything.
			val amountString = requirement.replace("M", "000000").replace("B", "000000000")
			if (NumberUtils.isParsable(amountString)) {
				requiredUntilNextPrestige = amountString.toLong() - currentChocolate
			}
		}

		//Time Tower is in slot 39
		timeTowerMultiplier = romanToDecimal(StringUtils.substringAfterLast(slots[TIME_TOWER_SLOT].stack.name.string, ' '.code))?.div(10.0) //The name holds the level, which is multiplier * 10 in roman numerals
		val timeTowerStatusMatcher = TIME_TOWER_STATUS_PATTERN.matcher(concatenateLore(slots[TIME_TOWER_SLOT].stack))
		if (timeTowerStatusMatcher.find()) {
			isTimeTowerActive = timeTowerStatusMatcher.group(1) == "ACTIVE"
		}

		//Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
		cpsIncreaseFactors.sortBy { rabbit: Rabbit -> rabbit.cost / rabbit.cpsIncrease } //Ascending order, lower = better
	}

	private fun handleTooltip(stack: ItemStack, tooltipContext: Item.TooltipContext, tooltipType: TooltipType, lines: MutableList<Text>) {
		if (!SkyblockerConfigManager.config.helpers.chocolateFactory.enableChocolateFactoryHelper) return

		val lineIndex = lines.size
		//This boolean is used to determine if we should add a smooth line to separate the added information from the rest of the tooltip.
		//It should be set to true if there's any information added, false otherwise.
		var shouldAddLine = false

		val lore = concatenateLore(lines)
		val costMatcher = COST_PATTERN.matcher(lore)
		val cost = getLongFromMatcher(costMatcher)
		//Available on all items with a chocolate cost
		if (cost != null) shouldAddLine = addUpgradeTimerToLore(lines, cost)

		//Prestige item
		when {
			stack.isOf(Items.DROPPER) -> shouldAddLine = addPrestigeTimerToLore(lines) or shouldAddLine
			stack.isOf(Items.CLOCK) -> shouldAddLine = addTimeTowerStatsToLore(lines) or shouldAddLine
			stack.isOf(Items.PLAYER_HEAD) -> shouldAddLine = addRabbitStatsToLore(lines, stack) or shouldAddLine
		}

		//This is an ArrayList, so this operation is probably not very efficient, but logically it's pretty much the only way I can think of
		if (shouldAddLine) lines.add(lineIndex, createSmoothLine())
	}



	/**
	 * Utility method.
	 */
	private fun concatenateLore(item: ItemStack) = concatenateLore(getLore(item))

	/**
	 * Concatenates the lore of an item into one string.
	 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
	 */
	private fun concatenateLore(lore: List<Text>) = lore.fold("") { acc, text -> acc + " " + text.string }

	private fun getCoach(coachItem: ItemStack): Rabbit? {
		if (!coachItem.isOf(Items.PLAYER_HEAD)) return null
		val coachLore = concatenateLore(coachItem)

		totalCpsMultiplier ?: return null //We need the total multiplier to calculate the increase in cps.

		val multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore)
		var currentCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher) ?: return null

		var nextCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher)
		if (nextCpsMultiplier == null) { //This means that the coach isn't hired yet.
			nextCpsMultiplier = currentCpsMultiplier //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
			currentCpsMultiplier = 0.0 //And so, we can re-assign values to the variables to make the calculation more readable.
		}

		val costMatcher = COST_PATTERN.matcher(coachLore)
		val cost = getIntFromMatcher(costMatcher, if (multiplierIncreaseMatcher.hasMatch()) multiplierIncreaseMatcher.end() else 0) ?: return null //Cost comes after the multiplier line

		return Rabbit(totalCps!! / totalCpsMultiplier!! * (nextCpsMultiplier - currentCpsMultiplier), cost, 42, coachItem)
	}

	private fun getRabbit(item: ItemStack, slot: Int): Rabbit? {
		val lore = concatenateLore(item)
		val cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore)
		var currentCps = getIntFromMatcher(cpsMatcher) ?: return null
		var nextCps = getIntFromMatcher(cpsMatcher)
		if (nextCps == null) {
			nextCps = currentCps //This means that the rabbit isn't hired yet.
			currentCps = 0 //So the first instance of the cps is actually the amount we'll get upon hiring.
		}

		val costMatcher = COST_PATTERN.matcher(lore)
		val cost = getIntFromMatcher(costMatcher, if (cpsMatcher.hasMatch()) cpsMatcher.end() else 0) ?: return null //Cost comes after the cps line
		return Rabbit((nextCps - currentCps).toDouble(), cost, slot, item)
	}

	private fun getPrestigeHighlight(item: ItemStack): ColorHighlight? {
		val loreList = getLore(item)
		if (loreList.isEmpty()) return null

		val lore = loreList.last().string //The last line holds the text we're looking for
		if (lore == "Click to prestige!") return green(28)
		return red(28)
	}

	object Tooltip : TooltipAdder("^Chocolate Factory$", 0) {
		override fun addToTooltip(lines: List<Text>, focusedSlot: Slot) {
			if (!SkyblockerConfigManager.config.helpers.chocolateFactory.enableChocolateFactoryHelper) return;

			val lineIndex = lines.size;
			//This boolean is used to determine if we should add a smooth line to separate the added information from the rest of the tooltip.
			//It should be set to true if there's any information added, false otherwise.
			var shouldAddLine = false;

			val lore = concatenateLore(lines);
			val costMatcher = COST_PATTERN.matcher(lore);
			val cost = RegexUtils.getLongFromMatcher(costMatcher);
			//Available on all items with a chocolate cost
			if (cost != null) shouldAddLine = addUpgradeTimerToLore(lines, cost.getAsLong()) || shouldAddLine;

			val index = focusedSlot.id;

			when (index) {
				PRESTIGE_SLOT -> shouldAddLine = addPrestigeTimerToLore(lines) or shouldAddLine
				TIME_TOWER_SLOT -> shouldAddLine = addTimeTowerStatsToLore(lines) or shouldAddLine
				in RABBITS_START..RABBITS_END, COACH_SLOT-> shouldAddLine = addRabbitStatsToLore(lines, stack) or shouldAddLine
			}

			//This is an ArrayList, so this operation is probably not very efficient, but logically it's pretty much the only way I can think of
			if (shouldAddLine) lines.add(lineIndex, LineSmoothener.createSmoothLine());
		}

		private fun addUpgradeTimerToLore(lines: MutableList<Text>, cost: Long): Boolean {
			if (totalChocolate == null || totalCps == null) return false
			lines.add(
				Text.empty()
					.append(Text.literal("Time until upgrade: ").formatted(Formatting.GRAY))
					.append(formatTime((cost - totalChocolate!!) / totalCps!!))
			)
			return true
		}

		private fun addPrestigeTimerToLore(lines: MutableList<Text>): Boolean {
			if (requiredUntilNextPrestige == null || totalCps == null) return false
			lines.add(
				Text.empty()
					.append(Text.literal("Chocolate until next prestige: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(requiredUntilNextPrestige)).formatted(Formatting.GOLD))
			)
			lines.add(
				Text.empty()
					.append(Text.literal("Time until next prestige: ").formatted(Formatting.GRAY))
					.append(formatTime(requiredUntilNextPrestige!! / totalCps!!))
			)
			return true
		}

		private fun addTimeTowerStatsToLore(lines: MutableList<Text>): Boolean {
			if (totalCps == null || totalCpsMultiplier == null || timeTowerMultiplier == null) return false
			lines.add(Text.literal("Current stats:").formatted(Formatting.GRAY))
			lines.add(
				Text.empty()
					.append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(totalCps!! / totalCpsMultiplier!! * timeTowerMultiplier!!)).formatted(Formatting.GOLD))
			)
			lines.add(
				Text.empty()
					.append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(if (isTimeTowerActive) totalCps else totalCps!! / totalCpsMultiplier!! * (timeTowerMultiplier!! + totalCpsMultiplier!!))).formatted(Formatting.GOLD))
			)
			if (timeTowerMultiplier!! < 1.5) {
				lines.add(Text.literal("Stats after upgrade:").formatted(Formatting.GRAY))
				lines.add(
					Text.empty()
						.append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(totalCps!! / (totalCpsMultiplier!!) * (timeTowerMultiplier!! + 0.1))).formatted(Formatting.GOLD))
				)
				lines.add(
					Text.empty()
						.append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(if (isTimeTowerActive) totalCps!! / totalCpsMultiplier!! * (totalCpsMultiplier!! + 0.1) else totalCps!! / totalCpsMultiplier!! * (timeTowerMultiplier!! + 0.1 + totalCpsMultiplier!!))).formatted(Formatting.GOLD))
				)
			}
			return true
		}

		private fun addRabbitStatsToLore(lines: MutableList<Text>, index: Int): Boolean {
			if (cpsIncreaseFactors.isEmpty()) return false
			var changed = false
			for ((cpsIncrease, cost, slot) in cpsIncreaseFactors) {
				if (index != slot) continue
				changed = true
				lines.add(Text.empty()
					.append(Text.literal("CPS Increase: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(cpsIncrease)).formatted(Formatting.GOLD)))
				lines.add(Text.empty()
					.append(Text.literal("Cost per CPS: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(cost / cpsIncrease)).formatted(Formatting.GOLD)))

				if (index == bestUpgrade) {
					lines.add(
						if (cost <= totalChocolate!!) Text.literal("Best upgrade").formatted(Formatting.GREEN)
						else Text.literal("Best upgrade, can't afford").formatted(Formatting.YELLOW)
					)
				} else if (index == bestAffordableUpgrade && cost <= totalChocolate!!) {
					lines.add(Text.literal("Best upgrade you can afford").formatted(Formatting.GREEN))
				}
			}
			return changed
		}

		private fun formatTime(seconds: Double): MutableText {
			var seconds = seconds
			seconds = ceil(seconds)
			if (seconds <= 0) return Text.literal("Now").formatted(Formatting.GREEN)

			val builder = StringBuilder()
			if (seconds >= 86400) {
				builder.append((seconds / 86400).toInt()).append("d ")
				seconds %= 86400.0
			}
			if (seconds >= 3600) {
				builder.append((seconds / 3600).toInt()).append("h ")
				seconds %= 3600.0
			}
			if (seconds >= 60) {
				builder.append((seconds / 60).toInt()).append("m ")
				seconds %= 60.0
			}
			if (seconds >= 1) {
				builder.append(seconds.toInt()).append("s")
			}
			return Text.literal(builder.toString()).formatted(Formatting.GOLD)
		}
	}
}
