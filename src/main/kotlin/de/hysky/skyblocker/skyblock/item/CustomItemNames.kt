package de.hysky.skyblocker.skyblock.item

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.TextArgumentType
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object CustomItemNames {
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
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
		if (isOnSkyblock) {
			val itemUuid = getItemUuid(source.player.mainHandStack)

			if (!itemUuid.isEmpty()) {
				val customItemNames = SkyblockerConfigManager.get().general.customItemNames

				if (text == null) {
					if (customItemNames.containsKey(itemUuid)) {
						//Remove custom item name when the text argument isn't passed
						customItemNames.remove(itemUuid)
						SkyblockerConfigManager.save()
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.removed")))
					} else {
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.neverHad")))
					}
				} else {
					//If the text is provided then set the item's custom name to it

					//Set italic to false if it hasn't been changed (or was already false)

					val currentStyle = text.style
					(text as MutableText).setStyle(currentStyle.withItalic(currentStyle.isItalic))

					customItemNames[itemUuid] = text
					SkyblockerConfigManager.save()
					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.added")))
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.noItemUuid")))
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customItemNames.unableToSetName")))
		}

		return Command.SINGLE_SUCCESS
	}
}
