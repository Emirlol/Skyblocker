package de.hysky.skyblocker.skyblock.item

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.Utils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.TextArgumentType
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object CustomItemNames {
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(::registerCommands)
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
			.then(ClientCommandManager.literal("custom")
				.then(ClientCommandManager.literal("renameItem")
					.executes { context: CommandContext<FabricClientCommandSource> -> renameItem(context.source, null) }
					.then(ClientCommandManager.argument("textComponent", TextArgumentType.text(registryAccess))
						.executes { context: CommandContext<FabricClientCommandSource> -> renameItem(context.source, context.getArgument("textComponent", Text::class.java)) })
				)
			)
		)
	}

	private fun renameItem(source: FabricClientCommandSource, text: Text?): Int {
		if (!Utils.isOnSkyblock) {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customItemNames.unableToSetName")))
			return 0
		}

		val itemUuid = getItemUuid(source.player.mainHandStack)
		if (itemUuid.isNullOrEmpty()) {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customItemNames.noItemUuid")))
			return 0
		}

		val customItemNames = SkyblockerConfigManager.config.general.customItemNames
		if (text == null) {
			if (!customItemNames.containsKey(itemUuid)) {
				source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customItemNames.neverHad")))
				return Command.SINGLE_SUCCESS
			}

			//Remove custom item name when the text argument isn't passed
			customItemNames -= itemUuid
			SkyblockerConfigManager.save()
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customItemNames.removed")))
		} else {
			//If the text is provided then set the item's custom name to it

			//Set italic to false if it hasn't been changed (or was already false)

			val currentStyle = text.style
			(text as MutableText).setStyle(currentStyle.withItalic(currentStyle.isItalic))

			customItemNames[itemUuid] = text
			SkyblockerConfigManager.save()
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customItemNames.added")))
		}

		return Command.SINGLE_SUCCESS
	}
}
