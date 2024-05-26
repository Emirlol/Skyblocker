package de.hysky.skyblocker.skyblock.chocolatefactory

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip.createSmoothLine
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.RegexUtils.getDoubleFromMatcher
import de.hysky.skyblocker.utils.RegexUtils.getIntFromMatcher
import de.hysky.skyblocker.utils.RegexUtils.getLongFromMatcher
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.yellow
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
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

class ChocolateFactorySolver : ContainerSolver("^Chocolate Factory$") {
	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		updateFactoryInfo(slots)
		val highlights: MutableList<ColorHighlight?> = ArrayList()

		getPrestigeHighlight(slots!![28]).ifPresent { e: ColorHighlight? -> highlights.add(e) }

		if (totalChocolate <= 0 || cpsIncreaseFactors.isEmpty) return highlights //Something went wrong or there's nothing we can afford.

		val bestRabbit = cpsIncreaseFactors.first
		bestUpgrade = bestRabbit.itemStack
		if (bestRabbit.cost <= totalChocolate) {
			highlights.add(green(bestRabbit.slot))
			return highlights
		}
		highlights.add(yellow(bestRabbit.slot))

		for ((_, cost, slot, itemStack) in cpsIncreaseFactors.subList(1, cpsIncreaseFactors.size)) {
			if (cost <= totalChocolate) {
				bestAffordableUpgrade = itemStack
				highlights.add(green(slot))
				break
			}
		}

		return highlights
	}

	@JvmRecord
	private data class Rabbit(val cpsIncrease: Double, val cost: Int, val slot: Int, val itemStack: ItemStack?)

	init {
		ItemTooltipCallback.EVENT.register(ItemTooltipCallback { stack: ItemStack, tooltipContext: Item.TooltipContext, tooltipType: TooltipType, lines: MutableList<Text> -> handleTooltip(stack, tooltipContext, tooltipType, lines) })
	}

	companion object {
		private val CPS_PATTERN: Pattern = Pattern.compile("([\\d,.]+) Chocolate per second")
		private val CPS_INCREASE_PATTERN: Pattern = Pattern.compile("\\+([\\d,]+) Chocolate per second")
		private val COST_PATTERN: Pattern = Pattern.compile("Cost ([\\d,]+) Chocolate")
		private val TOTAL_MULTIPLIER_PATTERN: Pattern = Pattern.compile("Total Multiplier: ([\\d.]+)x")
		private val MULTIPLIER_INCREASE_PATTERN: Pattern = Pattern.compile("\\+([\\d.]+)x Chocolate per second")
		private val CHOCOLATE_PATTERN: Pattern = Pattern.compile("^([\\d,]+) Chocolate$")
		private val PRESTIGE_REQUIREMENT_PATTERN: Pattern = Pattern.compile("Chocolate this Prestige: ([\\d,]+) +Requires (\\S+) Chocolate this Prestige!")
		private val TIME_TOWER_STATUS_PATTERN: Pattern = Pattern.compile("Status: (ACTIVE|INACTIVE)")
		private val cpsIncreaseFactors = ObjectArrayList<Rabbit>(6)
		private var totalChocolate = -1L
		private var totalCps = -1.0
		private var totalCpsMultiplier = -1.0
		private var requiredUntilNextPrestige = -1L
		private var timeTowerMultiplier = -1.0
		private var isTimeTowerActive = false
		private val DECIMAL_FORMAT = DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
		private var bestUpgrade: ItemStack? = null
		private var bestAffordableUpgrade: ItemStack? = null

		private fun updateFactoryInfo(slots: Int2ObjectMap<ItemStack?>?) {
			cpsIncreaseFactors.clear()

			for (i in 29..33) { // The 5 rabbits slots are in 29, 30, 31, 32 and 33.
				val item = slots!![i]
				if (item!!.isOf(Items.PLAYER_HEAD)) {
					getRabbit(item, i).ifPresent { k: Rabbit -> cpsIncreaseFactors.add(k) }
				}
			}

			//Coach is in slot 42
			getCoach(slots!![42]).ifPresent { k: Rabbit -> cpsIncreaseFactors.add(k) }

			//The clickable chocolate is in slot 13, holds the total chocolate
			getLongFromMatcher(CHOCOLATE_PATTERN.matcher(slots[13]!!.name.string)).ifPresent { l: Long -> totalChocolate = l }

			//Cps item (cocoa bean) is in slot 45
			val cpsItemLore = getConcatenatedLore(slots[45])
			val cpsMatcher = CPS_PATTERN.matcher(cpsItemLore)
			getDoubleFromMatcher(cpsMatcher).ifPresent { d: Double -> totalCps = d }
			val multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore)
			getDoubleFromMatcher(multiplierMatcher, if (cpsMatcher.hasMatch()) cpsMatcher.end() else 0).ifPresent { d: Double -> totalCpsMultiplier = d }

			//Prestige item is in slot 28
			val prestigeMatcher = PRESTIGE_REQUIREMENT_PATTERN.matcher(getConcatenatedLore(slots[28]))
			val currentChocolate = getLongFromMatcher(prestigeMatcher)
			if (currentChocolate.isPresent) {
				val requirement = prestigeMatcher.group(2) //If the first one matched, we can assume the 2nd one is also matched since it's one whole regex
				//Since the last character is either M or B we can just try to replace both characters. Only the correct one will actually replace anything.
				val amountString = requirement.replace("M", "000000").replace("B", "000000000")
				if (NumberUtils.isParsable(amountString)) {
					requiredUntilNextPrestige = amountString.toLong() - currentChocolate.asLong
				}
			}

			//Time Tower is in slot 39
			timeTowerMultiplier = romanToDecimal(StringUtils.substringAfterLast(slots[39]!!.name.string, ' '.code)) / 10.0 //The name holds the level, which is multiplier * 10 in roman numerals
			val timeTowerStatusMatcher = TIME_TOWER_STATUS_PATTERN.matcher(getConcatenatedLore(slots[39]))
			if (timeTowerStatusMatcher.find()) {
				isTimeTowerActive = timeTowerStatusMatcher.group(1) == "ACTIVE"
			}

			//Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
			cpsIncreaseFactors.sort(Comparator.comparingDouble { rabbit: Rabbit -> rabbit.cost / rabbit.cpsIncrease }) //Ascending order, lower = better
		}

		private fun handleTooltip(stack: ItemStack, tooltipContext: Item.TooltipContext, tooltipType: TooltipType, lines: MutableList<Text>) {
			if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper) return
			if (MinecraftClient.getInstance().currentScreen !is GenericContainerScreen || screen.getTitle().getString() != "Chocolate Factory") return

			val lineIndex = lines.size
			//This boolean is used to determine if we should add a smooth line to separate the added information from the rest of the tooltip.
			//It should be set to true if there's any information added, false otherwise.
			var shouldAddLine = false

			val lore = concatenateLore(lines)
			val costMatcher = COST_PATTERN.matcher(lore)
			val cost = getLongFromMatcher(costMatcher)
			//Available on all items with a chocolate cost
			if (cost.isPresent) shouldAddLine = addUpgradeTimerToLore(lines, cost.asLong)

			//Prestige item
			if (stack.isOf(Items.DROPPER) && requiredUntilNextPrestige != -1L) {
				shouldAddLine = addPrestigeTimerToLore(lines) || shouldAddLine
			} else if (stack.isOf(Items.CLOCK)) {
				shouldAddLine = addTimeTowerStatsToLore(lines) || shouldAddLine
			} else if (stack.isOf(Items.PLAYER_HEAD)) {
				shouldAddLine = addRabbitStatsToLore(lines, stack) || shouldAddLine
			}

			//This is an ArrayList, so this operation is probably not very efficient, but logically it's pretty much the only way I can think of
			if (shouldAddLine) lines.add(lineIndex, createSmoothLine())
		}

		private fun addUpgradeTimerToLore(lines: MutableList<Text>, cost: Long): Boolean {
			if (totalChocolate < 0L || totalCps < 0.0) return false
			lines.add(
				Text.empty()
					.append(Text.literal("Time until upgrade: ").formatted(Formatting.GRAY))
					.append(formatTime((cost - totalChocolate) / totalCps))
			)
			return true
		}

		private fun addPrestigeTimerToLore(lines: MutableList<Text>): Boolean {
			if (requiredUntilNextPrestige == -1L || totalCps == -1.0) return false
			lines.add(
				Text.empty()
					.append(Text.literal("Chocolate until next prestige: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(requiredUntilNextPrestige)).formatted(Formatting.GOLD))
			)
			lines.add(
				Text.empty()
					.append(Text.literal("Time until next prestige: ").formatted(Formatting.GRAY))
					.append(formatTime(requiredUntilNextPrestige / totalCps))
			)
			return true
		}

		private fun addTimeTowerStatsToLore(lines: MutableList<Text>): Boolean {
			if (totalCps < 0.0 || totalCpsMultiplier < 0.0 || timeTowerMultiplier < 0.0) return false
			lines.add(Text.literal("Current stats:").formatted(Formatting.GRAY))
			lines.add(
				Text.empty()
					.append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(totalCps / totalCpsMultiplier * timeTowerMultiplier)).formatted(Formatting.GOLD))
			)
			lines.add(
				Text.empty()
					.append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(if (isTimeTowerActive) totalCps else totalCps / totalCpsMultiplier * (timeTowerMultiplier + totalCpsMultiplier))).formatted(Formatting.GOLD))
			)
			if (timeTowerMultiplier < 1.5) {
				lines.add(Text.literal("Stats after upgrade:").formatted(Formatting.GRAY))
				lines.add(
					Text.empty()
						.append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(totalCps / (totalCpsMultiplier) * (timeTowerMultiplier + 0.1))).formatted(Formatting.GOLD))
				)
				lines.add(
					Text.empty()
						.append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(if (isTimeTowerActive) totalCps / totalCpsMultiplier * (totalCpsMultiplier + 0.1) else totalCps / totalCpsMultiplier * (timeTowerMultiplier + 0.1 + totalCpsMultiplier))).formatted(Formatting.GOLD))
				)
			}
			return true
		}

		private fun addRabbitStatsToLore(lines: MutableList<Text>, stack: ItemStack): Boolean {
			if (cpsIncreaseFactors.isEmpty) return false
			var changed = false
			for ((cpsIncrease, cost, _, itemStack) in cpsIncreaseFactors) {
				if (itemStack != stack) continue
				changed = true
				lines.add(
					Text.empty()
						.append(Text.literal("CPS Increase: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(cpsIncrease)).formatted(Formatting.GOLD))
				)

				lines.add(
					Text.empty()
						.append(Text.literal("Cost per CPS: ").formatted(Formatting.GRAY))
						.append(Text.literal(DECIMAL_FORMAT.format(cost / cpsIncrease)).formatted(Formatting.GOLD))
				)

				if (itemStack == bestUpgrade) {
					if (cost <= totalChocolate) {
						lines.add(Text.literal("Best upgrade").formatted(Formatting.GREEN))
					} else {
						lines.add(Text.literal("Best upgrade, can't afford").formatted(Formatting.YELLOW))
					}
				} else if (itemStack == bestAffordableUpgrade && cost <= totalChocolate) {
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

		/**
		 * Utility method.
		 */
		private fun getConcatenatedLore(item: ItemStack?): String {
			return concatenateLore(getLore(item!!))
		}

		/**
		 * Concatenates the lore of an item into one string.
		 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
		 */
		private fun concatenateLore(lore: List<Text>): String {
			val stringBuilder = StringBuilder()
			for (i in lore.indices) {
				stringBuilder.append(lore[i].string)
				if (i != lore.size - 1) stringBuilder.append(" ")
			}
			return stringBuilder.toString()
		}

		private fun getCoach(coachItem: ItemStack?): Optional<Rabbit> {
			if (!coachItem!!.isOf(Items.PLAYER_HEAD)) return Optional.empty()
			val coachLore = getConcatenatedLore(coachItem)

			if (totalCpsMultiplier == -1.0) return Optional.empty() //We need the total multiplier to calculate the increase in cps.


			val multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore)
			var currentCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher)
			if (currentCpsMultiplier.isEmpty) return Optional.empty()

			var nextCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher)
			if (nextCpsMultiplier.isEmpty) { //This means that the coach isn't hired yet.
				nextCpsMultiplier = currentCpsMultiplier //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
				currentCpsMultiplier = OptionalDouble.of(0.0) //And so, we can re-assign values to the variables to make the calculation more readable.
			}

			val costMatcher = COST_PATTERN.matcher(coachLore)
			val cost = getIntFromMatcher(costMatcher, if (multiplierIncreaseMatcher.hasMatch()) multiplierIncreaseMatcher.end() else 0) //Cost comes after the multiplier line
			if (cost.isEmpty) return Optional.empty()

			return Optional.of(Rabbit(totalCps / totalCpsMultiplier * (nextCpsMultiplier.asDouble - currentCpsMultiplier.asDouble), cost.asInt, 42, coachItem))
		}

		private fun getRabbit(item: ItemStack?, slot: Int): Optional<Rabbit> {
			val lore = getConcatenatedLore(item)
			val cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore)
			var currentCps = getIntFromMatcher(cpsMatcher)
			if (currentCps.isEmpty) return Optional.empty()
			var nextCps = getIntFromMatcher(cpsMatcher)
			if (nextCps.isEmpty) {
				nextCps = currentCps //This means that the rabbit isn't hired yet.
				currentCps = OptionalInt.of(0) //So the first instance of the cps is actually the amount we'll get upon hiring.
			}

			val costMatcher = COST_PATTERN.matcher(lore)
			val cost = getIntFromMatcher(costMatcher, if (cpsMatcher.hasMatch()) cpsMatcher.end() else 0) //Cost comes after the cps line
			if (cost.isEmpty) return Optional.empty()
			return Optional.of(Rabbit((nextCps.asInt - currentCps.asInt).toDouble(), cost.asInt, slot, item))
		}

		private fun getPrestigeHighlight(item: ItemStack?): Optional<ColorHighlight> {
			val loreList = getLore(item!!)
			if (loreList.isEmpty()) return Optional.empty()

			val lore = loreList.last.string //The last line holds the text we're looking for
			if (lore == "Click to prestige!") return Optional.of(green(28))
			return Optional.of(red(28))
		}

		//Perhaps the part below can go to a separate file later on, but I couldn't find a proper name for the class, so they're staying here.
		private val romanMap: Map<Char, Int> = java.util.Map.of(
			'I', 1,
			'V', 5,
			'X', 10,
			'L', 50,
			'C', 100,
			'D', 500,
			'M', 1000
		)

		fun romanToDecimal(romanNumeral: String): Int {
			var decimal = 0
			var lastNumber = 0
			for (i in romanNumeral.length - 1 downTo 0) {
				val ch = romanNumeral[i]
				decimal = if (romanMap[ch]!! >= lastNumber) decimal + romanMap[ch]!! else decimal - romanMap[ch]!!
				lastNumber = romanMap[ch]!!
			}
			return decimal
		}
	}
}
