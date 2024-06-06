package de.hysky.skyblocker.skyblock.end

import com.mojang.authlib.properties.PropertyMap
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat
import java.util.*

class EndHudWidget(title: MutableText?, colorValue: Int?) : Widget(title!!, colorValue) {
	init {
		this.x = 5
		this.y = 5
		this.update()
	}

	override fun updateContent() {
		// Zealots
		if (SkyblockerConfigManager.config.otherLocations.end.zealotKillsEnabled) {
			addComponent(IcoTextComponent(ENDERMAN_HEAD, Text.literal("Zealots").formatted(Formatting.BOLD)))
			addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsSinceLastEye", TheEnd.zealotsSinceLastEye)))
			addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsTotalKills", TheEnd.zealotsKilled)))
			val instance = NumberFormat.getInstance()
			instance.minimumFractionDigits = 0
			instance.maximumFractionDigits = 2
			val avg = if (TheEnd.eyes == 0) "???" else instance.format((TheEnd.zealotsKilled.toFloat() / TheEnd.eyes).toDouble())
			addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.avgKillsPerEye", avg)))
		}

		// Endstone protector
		if (SkyblockerConfigManager.config.otherLocations.end.protectorLocationEnabled) {
			addComponent(IcoTextComponent(POPPY, Text.literal("Endstone Protector").formatted(Formatting.BOLD)))
			if (TheEnd.stage == 5) {
				addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.stage", "IMMINENT")))
			} else {
				addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.stage", TheEnd.stage.toString())))
			}
			if (TheEnd.currentProtectorLocation == null) {
				addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.location", "?")))
			} else {
				addComponent(PlainTextComponent(Text.translatable("skyblocker.end.hud.location", TheEnd.currentProtectorLocation.name)))
			}
		}
	}

	companion object {
		private val TITLE: MutableText = Text.literal("The End").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)

		@JvmField
        val INSTANCE: EndHudWidget = EndHudWidget(TITLE, Formatting.DARK_PURPLE.colorValue)

		private val ENDERMAN_HEAD = ItemStack(Items.PLAYER_HEAD)
		private val POPPY = ItemStack(Items.POPPY)

		init {
			ENDERMAN_HEAD.set(DataComponentTypes.PROFILE, ProfileComponent(Optional.of("MHF_Enderman"), Optional.empty(), PropertyMap()))
			POPPY.addEnchantment(Enchantments.INFINITY, 1)

			INSTANCE.x = SkyblockerConfigManager.config.otherLocations.end.x
			INSTANCE.y = SkyblockerConfigManager.config.otherLocations.end.y
		}
	}
}
