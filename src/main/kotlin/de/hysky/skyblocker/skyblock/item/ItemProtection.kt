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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

object ItemProtection {
	@JvmField
	var itemProtection: KeyBinding? = null

	fun init() {
		itemProtection = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.itemProtection",
				GLFW.GLFW_KEY_V,
				"key.categories.skyblocker"
			)
		)
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommand(dispatcher) })
	}

	@JvmStatic
	fun isItemProtected(stack: ItemStack?): Boolean {
		if (stack == null) return false
		val itemUuid = getItemUuid(stack)
		return SkyblockerConfigManager.config.general.protectedItems.contains(itemUuid)
	}

	private fun registerCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(
			ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("protectItem")
					.executes { context: CommandContext<FabricClientCommandSource> -> protectMyItem(context.source) })
		)
	}

	private fun protectMyItem(source: FabricClientCommandSource): Int {
		val heldItem = source.player.mainHandStack

		if (isOnSkyblock) {
			val itemUuid = getItemUuid(heldItem)

			if (!itemUuid.isEmpty()) {
				val protectedItems = SkyblockerConfigManager.config.general.protectedItems

				if (!protectedItems.contains(itemUuid)) {
					protectedItems.add(itemUuid)
					SkyblockerConfigManager.save()

					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.added", heldItem.name)))
				} else {
					protectedItems.remove(itemUuid)
					SkyblockerConfigManager.save()

					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.removed", heldItem.name)))
				}
			} else {
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")))
			}
		} else {
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.unableToProtect")))
		}

		return Command.SINGLE_SUCCESS
	}

	@JvmStatic
	fun handleKeyPressed(heldItem: ItemStack) {
		val playerEntity = MinecraftClient.getInstance().player ?: return
		if (!isOnSkyblock) {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.unableToProtect")))
			return
		}

		if (heldItem.isEmpty) {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")))
			return
		}

		val itemUuid = getItemUuid(heldItem)
		if (!itemUuid.isEmpty()) {
			val protectedItems = SkyblockerConfigManager.config.general.protectedItems

			if (!protectedItems.contains(itemUuid)) {
				protectedItems.add(itemUuid)
				SkyblockerConfigManager.save()

				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.added", heldItem.name)))
			} else {
				protectedItems.remove(itemUuid)
				SkyblockerConfigManager.save()

				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.removed", heldItem.name)))
			}
		} else {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")))
		}
	}

	@JvmStatic
	fun handleHotbarKeyPressed(player: ClientPlayerEntity) {
		while (itemProtection!!.wasPressed()) {
			val heldItem = player.mainHandStack
			handleKeyPressed(heldItem)
		}
	}
}
