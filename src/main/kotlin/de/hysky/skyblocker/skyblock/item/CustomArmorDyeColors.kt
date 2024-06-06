package de.hysky.skyblocker.skyblock.item

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text

object CustomArmorDyeColors {
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
			.then(ClientCommandManager.literal("custom")
				.then(ClientCommandManager.literal("dyeColor")
					.executes { context: CommandContext<FabricClientCommandSource> -> customizeDyeColor(context.source, null) }
					.then(ClientCommandManager.argument("hexCode", StringArgumentType.string())
						.executes { context: CommandContext<FabricClientCommandSource> -> customizeDyeColor(context.source, StringArgumentType.getString(context, "hexCode")) })
				)
			)
		)
	}

	private fun customizeDyeColor(source: FabricClientCommandSource, hex: String?): Int {
		val heldItem = source.player.mainHandStack

		if (hex != null && !isHexadecimalColor(hex)) {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.invalidHex")))
			return Command.SINGLE_SUCCESS
		}

		if (isOnSkyblock && heldItem != null) {
			if (heldItem.isIn(ItemTags.DYEABLE)) {
				val itemUuid = getItemUuid(heldItem)

				if (!itemUuid.isEmpty()) {
					val customDyeColors = SkyblockerConfigManager.config.general.customDyeColors

					if (hex == null) {
						if (customDyeColors.containsKey(itemUuid)) {
							customDyeColors.removeInt(itemUuid)
							SkyblockerConfigManager.save()
							source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.removed")))
						} else {
							source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.neverHad")))
						}
					} else {
						customDyeColors.put(itemUuid, Integer.decode("0x" + hex.replace("#", "")))
						SkyblockerConfigManager.save()
						source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.added")))
					}
				} else {
					source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.noItemUuid")))
				}
			} else {
				source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.notDyeable")))
				return Command.SINGLE_SUCCESS
			}
		} else {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.customDyeColors.unableToSetColor")))
		}

		return Command.SINGLE_SUCCESS
	}

	fun isHexadecimalColor(s: String): Boolean {
		return s.replace("#", "").chars().allMatch { c: Int -> "0123456789ABCDEFabcdef".indexOf(c.toChar()) >= 0 } && s.replace("#", "").length == 6
	}
}
