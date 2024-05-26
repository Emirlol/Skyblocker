package de.hysky.skyblocker.skyblock.item.tooltip

import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository.getItemStack
import de.hysky.skyblocker.utils.ItemUtils.getCustomData
import it.unimi.dsi.fastutil.ints.IntIntPair
import it.unimi.dsi.fastutil.ints.IntObjectPair
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

object CompactorDeletorPreview {
	/**
	 * The width and height in slots of the compactor/deletor
	 */
	private val DIMENSIONS: Map<String, IntIntPair> = java.util.Map.of(
		"4000", IntIntPair.of(1, 1),
		"5000", IntIntPair.of(1, 3),
		"6000", IntIntPair.of(1, 7),
		"7000", IntIntPair.of(2, 6)
	)
	private val DEFAULT_DIMENSION: IntIntPair = IntIntPair.of(1, 6)
	@JvmField
    val NAME: Pattern = Pattern.compile("PERSONAL_(?<type>COMPACTOR|DELETOR)_(?<size>\\d+)")
	private val client: MinecraftClient = MinecraftClient.getInstance()

	@JvmStatic
    fun drawPreview(context: DrawContext, stack: ItemStack?, type: String, size: String, x: Int, y: Int): Boolean {
		val tooltips = Screen.getTooltipFromItem(client, stack)
		val targetIndex = getTargetIndex(tooltips)
		if (targetIndex == -1) return false

		// Get items in compactor or deletor
		val customData = getCustomData(stack!!)
		// Get the slots and their items from the nbt, which is in the format personal_compact_<slot_number> or personal_deletor_<slot_number>
		val slots = customData.keys.stream().filter { slot: String -> slot.contains(type.lowercase(Locale.getDefault()).substring(0, 7)) }.map { slot: String -> IntObjectPair.of(slot.substring(17).toInt(), getItemStack(customData.getString(slot))) }.toList()

		val components = tooltips.stream().map { obj: Text -> obj.asOrderedText() }.map { text: OrderedText? -> TooltipComponent.of(text) }.collect(Collectors.toList())
		val dimensions = DIMENSIONS.getOrDefault(size, DEFAULT_DIMENSION)

		// If there are no items in compactor or deletor
		if (slots.isEmpty()) {
			val slotsCount = dimensions.leftInt() * dimensions.rightInt()
			components.add(targetIndex, TooltipComponent.of(Text.literal(slotsCount.toString() + (if (slotsCount == 1) " slot" else " slots")).formatted(Formatting.GRAY).asOrderedText()))

			(context as DrawContextInvoker).invokeDrawTooltip(client.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE)
			return true
		}

		// Add the preview tooltip component
		components.add(targetIndex, CompactorPreviewTooltipComponent(slots, dimensions))

		if (customData.contains("PERSONAL_DELETOR_ACTIVE")) {
			components.add(
				targetIndex, TooltipComponent.of(
					Text.literal("Active: ")
						.append(if (customData.getBoolean("PERSONAL_DELETOR_ACTIVE")) Text.literal("YES").formatted(Formatting.BOLD).formatted(Formatting.GREEN) else Text.literal("NO").formatted(Formatting.BOLD).formatted(Formatting.RED)).asOrderedText()
				)
			)
		}
		(context as DrawContextInvoker).invokeDrawTooltip(client.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE)
		return true
	}

	/**
	 * Finds the target index to insert the preview component, which is the second empty line
	 */
	private fun getTargetIndex(tooltips: List<Text>): Int {
		var targetIndex = -1
		var lineCount = 0
		for (i in tooltips.indices) {
			if (tooltips[i].string.isEmpty()) {
				lineCount += 1
			}
			if (lineCount == 2) {
				targetIndex = i
				break
			}
		}
		return targetIndex
	}
}
