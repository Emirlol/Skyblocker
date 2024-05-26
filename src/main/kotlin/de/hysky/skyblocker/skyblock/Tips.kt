package de.hysky.skyblocker.skyblock

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.utils.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.*

object Tips {
	private val RANDOM = Random()
	private var previousTipIndex = -1
	private val TIPS = listOf(
		getTipFactory("skyblocker.tips.customItemNames", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom renameItem"),
		getTipFactory("skyblocker.tips.customArmorDyeColors", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom dyeColor"),
		getTipFactory("skyblocker.tips.customArmorTrims", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom armorTrim"),
		getTipFactory("skyblocker.tips.customAnimatedDyes", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom animatedDye"),
		getTipFactory("skyblocker.tips.fancyTabExtraInfo"),
		getTipFactory("skyblocker.tips.helpCommand", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker help"),
		getTipFactory("skyblocker.tips.discordRichPresence", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
		getTipFactory("skyblocker.tips.customDungeonSecretWaypoints", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker dungeons secrets addWaypoint"),
		getTipFactory("skyblocker.tips.shortcuts", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker shortcuts"),
		getTipFactory("skyblocker.tips.gallery", ClickEvent.Action.OPEN_URL, "https://hysky.de/skyblocker/gallery"),
		getTipFactory("skyblocker.tips.itemRarityBackground", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
		getTipFactory("skyblocker.tips.modMenuUpdate"),
		getTipFactory("skyblocker.tips.issues", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker"),
		getTipFactory("skyblocker.tips.beta", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker/actions"),
		getTipFactory("skyblocker.tips.discord", ClickEvent.Action.OPEN_URL, "https://discord.gg/aNNJHQykck"),
		getTipFactory("skyblocker.tips.flameOverlay", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
		getTipFactory("skyblocker.tips.wikiLookup", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
		getTipFactory("skyblocker.tips.protectItem", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker protectItem"),
		getTipFactory("skyblocker.tips.fairySoulsEnigmaSoulsRelics", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker fairySouls"),
		getTipFactory("skyblocker.tips.quickNav", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config")
	)

	private var sentTip = false

	private fun getTipFactory(key: String): () -> Text = { Text.translatable(key) }

	private fun getTipFactory(key: String, clickAction: ClickEvent.Action, value: String): () -> Text = {
		Text.translatable(key).styled { style: Style -> style.withClickEvent(ClickEvent(clickAction, value)) }
	}

	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(::registerTipsCommand)
		SkyblockEvents.JOIN.register { sendNextTip() }
	}

	private fun registerTipsCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(
			ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(
				ClientCommandManager.literal("tips")
					.then(ClientCommandManager.literal("enable").executes { enableTips(it) })
					.then(ClientCommandManager.literal("disable").executes { disableTips(it) })
					.then(ClientCommandManager.literal("next").executes { nextTip(it) })
			)
		)
	}

	private fun sendNextTip() {
		if (SkyblockerConfigManager.get().general.enableTips && !sentTip) {
			MinecraftClient.getInstance()?.player?.sendMessage(nextTip(), false)
			sentTip = true
		}
	}

	private fun enableTips(context: CommandContext<FabricClientCommandSource>): Int {
		SkyblockerConfigManager.get().general.enableTips = true
		SkyblockerConfigManager.save()
		context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.tips.enabled")).append(" ").append(Text.translatable("skyblocker.tips.clickDisable").styled { style: Style -> style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable")) }))
		return Command.SINGLE_SUCCESS
	}

	private fun disableTips(context: CommandContext<FabricClientCommandSource>): Int {
		SkyblockerConfigManager.get().general.enableTips = false
		SkyblockerConfigManager.save()
		context.source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.tips.disabled")).append(" ").append(Text.translatable("skyblocker.tips.clickEnable").styled { style: Style -> style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips enable")) }))
		return Command.SINGLE_SUCCESS
	}

	private fun nextTip(context: CommandContext<FabricClientCommandSource>): Int {
		context.source.sendFeedback(nextTip())
		return Command.SINGLE_SUCCESS
	}

	private fun nextTip(): Text {
		return Constants.PREFIX.append(Text.translatable("skyblocker.tips.tip", nextTipInternal()))
			.append(Text.translatable("skyblocker.tips.clickNextTip").styled { style -> style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips next")) })
			.append(" ")
			.append(Text.translatable("skyblocker.tips.clickDisable").styled { style -> style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable")) })
	}

	fun nextTipInternal(): Text {
		var randomInt = RANDOM.nextInt(TIPS.size)
		while (randomInt == previousTipIndex) randomInt = RANDOM.nextInt(TIPS.size)
		previousTipIndex = randomInt
		return TIPS[randomInt].invoke()
	}
}
