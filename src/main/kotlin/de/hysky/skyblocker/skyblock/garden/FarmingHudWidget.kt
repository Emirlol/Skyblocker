package de.hysky.skyblocker.skyblock.garden

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.tabhud.util.Ico
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper

class FarmingHudWidget : Widget(TITLE, Formatting.YELLOW.colorValue) {
	private val client: MinecraftClient = MinecraftClient.getInstance()

	init {
		x = SkyblockerConfigManager.get().farming.garden.farmingHud.x
		y = SkyblockerConfigManager.get().farming.garden.farmingHud.y
		update()
	}

	override fun updateContent() {
		val icon = if (client.player == null) Ico.HOE else FARMING_TOOLS.getOrDefault(getItemId(client.player!!.mainHandStack), Ico.HOE)
		addSimpleIcoText(icon, "Counter: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format(FarmingHud.counter().toLong()))
		addSimpleIcoText(icon, "Crops/min: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((FarmingHud.cropsPerMinute().toInt() / 100 * 100).toLong()))
		addSimpleIcoText(icon, "Blocks/s: ", Formatting.YELLOW, FarmingHud.blockBreaks().toString())
		addComponent(ProgressComponent(Ico.LANTERN, Text.literal("Farming Level: "), FarmingHud.farmingXpPercentProgress(), Formatting.GOLD.colorValue!!))
		addSimpleIcoText(Ico.LIME_DYE, "Farming XP/h: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format(FarmingHud.farmingXpPerHour().toInt().toLong()))

		val cameraEntity = client.getCameraEntity()
		val yaw = cameraEntity?.yaw?.toDouble() ?: 0.0
		val pitch = cameraEntity?.pitch?.toDouble() ?: 0.0
		addComponent(PlainTextComponent(Text.literal("Yaw: " + String.format("%.3f", MathHelper.wrapDegrees(yaw))).formatted(Formatting.YELLOW)))
		addComponent(PlainTextComponent(Text.literal("Pitch: " + String.format("%.3f", MathHelper.wrapDegrees(pitch))).formatted(Formatting.YELLOW)))
		if (LowerSensitivity.isSensitivityLowered()) {
			addComponent(PlainTextComponent(Text.translatable("skyblocker.garden.hud.mouseLocked").formatted(Formatting.ITALIC)))
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("Farming").formatted(Formatting.YELLOW, Formatting.BOLD)
		val FARMING_TOOLS: Map<String, ItemStack> = java.util.Map.ofEntries(
			java.util.Map.entry("THEORETICAL_HOE_WHEAT_1", Ico.WHEAT),
			java.util.Map.entry("THEORETICAL_HOE_WHEAT_2", Ico.WHEAT),
			java.util.Map.entry("THEORETICAL_HOE_WHEAT_3", Ico.WHEAT),
			java.util.Map.entry("THEORETICAL_HOE_CARROT_1", Ico.CARROT),
			java.util.Map.entry("THEORETICAL_HOE_CARROT_2", Ico.CARROT),
			java.util.Map.entry("THEORETICAL_HOE_CARROT_3", Ico.CARROT),
			java.util.Map.entry("THEORETICAL_HOE_POTATO_1", Ico.POTATO),
			java.util.Map.entry("THEORETICAL_HOE_POTATO_2", Ico.POTATO),
			java.util.Map.entry("THEORETICAL_HOE_POTATO_3", Ico.POTATO),
			java.util.Map.entry("THEORETICAL_HOE_CANE_1", Ico.SUGAR_CANE),
			java.util.Map.entry("THEORETICAL_HOE_CANE_2", Ico.SUGAR_CANE),
			java.util.Map.entry("THEORETICAL_HOE_CANE_3", Ico.SUGAR_CANE),
			java.util.Map.entry("THEORETICAL_HOE_WARTS_1", Ico.NETHER_WART),
			java.util.Map.entry("THEORETICAL_HOE_WARTS_2", Ico.NETHER_WART),
			java.util.Map.entry("THEORETICAL_HOE_WARTS_3", Ico.NETHER_WART),
			java.util.Map.entry("FUNGI_CUTTER", Ico.MUSHROOM),
			java.util.Map.entry("CACTUS_KNIFE", Ico.CACTUS),
			java.util.Map.entry("MELON_DICER", Ico.MELON),
			java.util.Map.entry("MELON_DICER_2", Ico.MELON),
			java.util.Map.entry("MELON_DICER_3", Ico.MELON),
			java.util.Map.entry("PUMPKIN_DICER", Ico.PUMPKIN),
			java.util.Map.entry("PUMPKIN_DICER_2", Ico.PUMPKIN),
			java.util.Map.entry("PUMPKIN_DICER_3", Ico.PUMPKIN),
			java.util.Map.entry("COCO_CHOPPER", Ico.COCOA_BEANS)
		)
		val INSTANCE: FarmingHudWidget = FarmingHudWidget()
	}
}
