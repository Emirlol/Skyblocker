package de.hysky.skyblocker.skyblock.quicknav

import com.mojang.brigadier.exceptions.CommandSyntaxException
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.QuickNavigationConfig.QuickNavItem
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer.fromComponentsString
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.PatternSyntaxException

object QuickNav {
	private val LOGGER: Logger = LoggerFactory.getLogger(QuickNav::class.java)

	@JvmStatic
	fun init(screenTitle: String): List<QuickNavButton> {
		val buttons: MutableList<QuickNavButton> = ArrayList()
		val data = SkyblockerConfigManager.config.quickNav
		try {
			if (data.button1.render) buttons.add(parseButton(data.button1, screenTitle, 0))
			if (data.button2.render) buttons.add(parseButton(data.button2, screenTitle, 1))
			if (data.button3.render) buttons.add(parseButton(data.button3, screenTitle, 2))
			if (data.button4.render) buttons.add(parseButton(data.button4, screenTitle, 3))
			if (data.button5.render) buttons.add(parseButton(data.button5, screenTitle, 4))
			if (data.button6.render) buttons.add(parseButton(data.button6, screenTitle, 5))
			if (data.button7.render) buttons.add(parseButton(data.button7, screenTitle, 6))
			if (data.button8.render) buttons.add(parseButton(data.button8, screenTitle, 7))
			if (data.button9.render) buttons.add(parseButton(data.button9, screenTitle, 8))
			if (data.button10.render) buttons.add(parseButton(data.button10, screenTitle, 9))
			if (data.button11.render) buttons.add(parseButton(data.button11, screenTitle, 10))
			if (data.button12.render) buttons.add(parseButton(data.button12, screenTitle, 11))
			if (data.button13.render) buttons.add(parseButton(data.button13, screenTitle, 12))
			if (data.button14.render) buttons.add(parseButton(data.button14, screenTitle, 13))
		} catch (e: CommandSyntaxException) {
			LOGGER.error("[Skyblocker] Failed to initialize Quick Nav Button", e)
		}
		return buttons
	}

	@Throws(CommandSyntaxException::class)
	private fun parseButton(buttonInfo: QuickNavItem, screenTitle: String, id: Int): QuickNavButton {
		val itemData = buttonInfo.itemData
		val stack = if (itemData?.item != null && itemData.components != null) fromComponentsString(itemData.item.toString(), Math.clamp(itemData.count.toLong(), 1, 99), itemData.components) else ItemStack(Items.BARRIER)

		var uiTitleMatches = false
		try {
			uiTitleMatches = screenTitle.matches(buttonInfo.uiTitle.toRegex())
		} catch (e: PatternSyntaxException) {
			LOGGER.error("[Skyblocker] Failed to parse Quick Nav Button with regex: {}", buttonInfo.uiTitle, e)
			val player = MinecraftClient.getInstance().player
			player?.sendMessage(Constants.PREFIX.get().append(Text.literal("Invalid regex in Quick Nav Button " + (id + 1) + "!").formatted(Formatting.RED)), false)
		}
		return QuickNavButton(id, uiTitleMatches, buttonInfo.clickEvent, stack)
	}
}
