package de.hysky.skyblocker.skyblock.accessories.newyearcakes

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.profile
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.red
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern

class NewYearCakesHelper private constructor() : ContainerSolver("Auctions: \".*\"") {
	private val cakes: MutableMap<String, IntSet> = HashMap()

	init {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message: Text, overlay: Boolean -> this.onChatMessage(message, overlay) })
	}

	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().helpers.enableNewYearCakesHelper

	fun addCake(year: Int): Boolean {
		if (year < 0) return false
		return cakes.computeIfAbsent(profile) { _profile: String? -> IntOpenHashSet() }.add(year)
	}

	private fun onChatMessage(message: Text, overlay: Boolean) {
		if (isEnabled) {
			addCake(getCakeYear(NEW_YEAR_CAKE_PURCHASE, message.string))
		}
	}

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		val profile = profile
		if (cakes.isEmpty() || !cakes.containsKey(profile) || cakes.containsKey(profile) && cakes[profile]!!.isEmpty()) return listOf<ColorHighlight>()
		val highlights: MutableList<ColorHighlight?> = ArrayList()
		for (entry in slots!!.int2ObjectEntrySet()) {
			val year = getCakeYear(entry.value)
			if (year >= 0 && cakes.containsKey(profile)) {
				highlights.add(if (cakes[profile]!!.contains(year)) red(entry.intKey) else green(entry.intKey))
			}
		}
		return highlights
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(NewYearCakeBagHelper::class.java)
		private val NEW_YEAR_CAKE: Pattern = Pattern.compile("New Year Cake \\(Year (?<year>\\d+)\\)")
		private val NEW_YEAR_CAKE_PURCHASE: Pattern = Pattern.compile("You purchased New Year Cake \\(Year (?<year>\\d+)\\) for .+ coins!")
		private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)
		@JvmField
        val INSTANCE: NewYearCakesHelper = NewYearCakesHelper()
		fun getCakeYear(stack: ItemStack?): Int {
			return getCakeYear(NEW_YEAR_CAKE, stack!!.name.string)
		}

		fun getCakeYear(pattern: Pattern, name: String): Int {
			val matcher = pattern.matcher(name)
			if (matcher.matches()) {
				try {
					return NUMBER_FORMAT.parse(matcher.group("year")).toInt()
				} catch (e: ParseException) {
					LOGGER.info("Failed to parse year from New Year Cake: $name", e)
				}
			}
			return -1
		}
	}
}
