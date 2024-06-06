package de.hysky.skyblocker.skyblock.item.tooltip

import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver
import de.hysky.skyblocker.skyblock.item.tooltip.adders.*
import de.hysky.skyblocker.utils.Utils
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text

object TooltipManager {
	private val adders = arrayOf<TooltipAdder>(
		LineSmoothener(),  // Applies before anything else
		SupercraftReminder(),
		ChocolateFactorySolver.Tooltip(),
		NpcPriceTooltip(1),
		BazaarPriceTooltip(2),
		LBinTooltip(3),
		AvgBinTooltip(4),
		DungeonQualityTooltip(5),
		MotesTooltip(6),
		ObtainedDateTooltip(7),
		MuseumTooltip(8),
		ColorTooltip(9),
		AccessoryTooltip(10),
	)
	private val currentScreenAdders = arrayListOf<TooltipAdder>()

	fun init() {
		ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
			onScreenChange(screen)
			ScreenEvents.remove(screen).register{ _ -> currentScreenAdders.clear() }
		}
	}

	private fun onScreenChange(screen: Screen) {
		val title = screen.title.string
		for (adder in adders) {
			if (adder.titleRegex == null || adder.titleRegex.containsMatchIn(title)) {
				currentScreenAdders += adder
			}
		}
		currentScreenAdders.sortBy { it.priority }
	}

	/**
	 *
	 * Adds additional text from all adders that are applicable to the current screen.
	 * This method is run on each tooltip render, so don't do any heavy calculations here.
	 *
	 *
	 * If you want to add info to the tooltips of multiple items, consider using a switch statement with `focusedSlot.getIndex()`
	 *
	 * @param lines The tooltip lines of the focused item. This includes the display name, as it's a part of the tooltip (at index 0).
	 * @param focusedSlot The slot that is currently focused by the cursor.
	 * @return The lines list itself after all adders have added their text.
	 */
	@Deprecated("This method is public only for the sake of the mixin. Don't call directly, not that there is any point to it.")
	fun addToTooltip(lines: List<Text>, focusedSlot: Slot): List<Text> {
		if (!isOnSkyblock) return lines
		for (adder in currentScreenAdders) {
			adder.addToTooltip(lines, focusedSlot)
		}
		return lines
	}
}
