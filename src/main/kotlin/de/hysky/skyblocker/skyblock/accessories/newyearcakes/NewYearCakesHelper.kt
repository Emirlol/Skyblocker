package de.hysky.skyblocker.skyblock.accessories.newyearcakes

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils.profile
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

object NewYearCakesHelper : ContainerSolver("Auctions: \".*\"") {
	private val cakes: MutableMap<String, IntSet> = HashMap()

	init {
		ClientReceiveMessageEvents.GAME.register { message, _ ->
			if (isEnabled) {
				addCake(getCakeYear(NEW_YEAR_CAKE_PURCHASE, message.string))
			}
		}
	}

	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.config.helpers.enableNewYearCakesHelper

	fun addCake(year: Int?) {
		year ?: return //Easier to check nullability here than in the caller
		cakes.computeIfAbsent(profile) { IntOpenHashSet() }.add(year)
	}

	override fun getColors(groups: Array<String>, slots: List<Slot>): List<ColorHighlight> {
		val profile = profile
		if (cakes.isEmpty() || !cakes.containsKey(profile) || cakes.containsKey(profile) && cakes[profile]!!.isEmpty()) returnemptyList()
		val highlights: MutableList<ColorHighlight> = arrayListOf()
		for (entry in slots) {
			val year = getCakeYear(entry.stack) ?: continue
			if (cakes.containsKey(profile)) {
				highlights.add(if (cakes[profile]!!.contains(year)) red(entry.id) else green(entry.id))
			}
		}
		return highlights
	}

	private val NEW_YEAR_CAKE = Regex("New Year Cake \\(Year (?<year>\\d+)\\)")
	private val NEW_YEAR_CAKE_PURCHASE = Regex("You purchased New Year Cake \\(Year (?<year>\\d+)\\) for .+ coins!")
	private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)

	fun getCakeYear(stack: ItemStack) = getCakeYear(NEW_YEAR_CAKE, stack.name.string)

	private fun getCakeYear(regex: Regex, name: String): Int? {
		val result = regex.matchEntire(name) ?: return null
		return try {
			NUMBER_FORMAT.parse(result.groups["year"]!!.value).toInt()
		} catch (e: ParseException) {
			TextHandler.error("Failed to parse year from New Year Cake: $name", e)
			null
		}
	}
}
