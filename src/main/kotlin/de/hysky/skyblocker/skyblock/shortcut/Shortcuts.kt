package de.hysky.skyblocker.skyblock.shortcut

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.scheduler.Scheduler.Companion.queueOpenScreenCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ModifyCommand
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

object Shortcuts {
	private val LOGGER: Logger = LoggerFactory.getLogger(Shortcuts::class.java)
	private val SHORTCUTS_FILE: Path = SkyblockerMod.CONFIG_DIR.resolve("shortcuts.json")
	private var shortcutsLoaded: CompletableFuture<Void>? = null
	@JvmField
    val commands: MutableMap<String, String> = HashMap()
	@JvmField
    val commandArgs: MutableMap<String, String> = HashMap()

	@JvmStatic
    fun isShortcutsLoaded(): Boolean {
		return shortcutsLoaded != null && shortcutsLoaded!!.isDone
	}

	fun init() {
		loadShortcuts()
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { obj: MinecraftClient? -> saveShortcuts() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
		ClientSendMessageEvents.MODIFY_COMMAND.register(ModifyCommand { obj: String? -> modifyCommand() })
	}

	internal fun loadShortcuts() {
		if (shortcutsLoaded != null && !isShortcutsLoaded()) {
			return
		}
		shortcutsLoaded = CompletableFuture.runAsync {
			try {
				Files.newBufferedReader(SHORTCUTS_FILE).use { reader ->
					val shortcutsType = object : TypeToken<Map<String?, Map<String?, String?>?>?>() {
					}.type
					val shortcuts = SkyblockerMod.GSON.fromJson<Map<String, Map<String, String>>>(reader, shortcutsType)
					commands.clear()
					commandArgs.clear()
					commands.putAll(shortcuts["commands"]!!)
					commandArgs.putAll(shortcuts["commandArgs"]!!)
					LOGGER.info("[Skyblocker] Loaded {} command shortcuts and {} command argument shortcuts", commands.size, commandArgs.size)
				}
			} catch (e: NoSuchFileException) {
				registerDefaultShortcuts()
				LOGGER.warn("[Skyblocker] Shortcuts file not found, using default shortcuts. This is normal when using for the first time.")
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to load shortcuts file", e)
			}
		}
	}

	private fun registerDefaultShortcuts() {
		commands.clear()
		commandArgs.clear()

		// Skyblock
		commands["/s"] = "/skyblock"
		commands["/i"] = "/is"
		commands["/h"] = "/hub"

		// Dungeon
		commands["/d"] = "/warp dungeon_hub"

		// Chat channels
		commands["/ca"] = "/chat all"
		commands["/cp"] = "/chat party"
		commands["/cg"] = "/chat guild"
		commands["/co"] = "/chat officer"
		commands["/cc"] = "/chat coop"

		// Message
		commandArgs["/m"] = "/msg"

		// Party
		commandArgs["/pa"] = "/p accept"
		commands["/pv"] = "/p leave"
		commands["/pd"] = "/p disband"
		commands["/rp"] = "/reparty"

		// Visit
		commandArgs["/v"] = "/visit"
		commands["/vp"] = "/visit portalhub"
	}

	@Suppress("unused")
	private fun registerMoreDefaultShortcuts() {
		// Combat
		commands["/spider"] = "/warp spider"
		commands["/crimson"] = "/warp nether"
		commands["/end"] = "/warp end"

		// Mining
		commands["/gold"] = "/warp gold"
		commands["/cavern"] = "/warp deep"
		commands["/dwarven"] = "/warp mines"
		commands["/fo"] = "/warp forge"
		commands["/ch"] = "/warp crystals"

		// Foraging & Farming
		commands["/park"] = "/warp park"
		commands["/barn"] = "/warp barn"
		commands["/desert"] = "/warp desert"
		commands["/ga"] = "/warp garden"

		// Other warps
		commands["/castle"] = "/warp castle"
		commands["/museum"] = "/warp museum"
		commands["/da"] = "/warp da"
		commands["/crypt"] = "/warp crypt"
		commands["/nest"] = "/warp nest"
		commands["/magma"] = "/warp magma"
		commands["/void"] = "/warp void"
		commands["/drag"] = "/warp drag"
		commands["/jungle"] = "/warp jungle"
		commands["/howl"] = "/warp howl"
	}

	@JvmStatic
    fun saveShortcuts(client: MinecraftClient?) {
		val shortcutsJson = JsonObject()
		shortcutsJson.add("commands", SkyblockerMod.GSON.toJsonTree(commands))
		shortcutsJson.add("commandArgs", SkyblockerMod.GSON.toJsonTree(commandArgs))
		try {
			Files.newBufferedWriter(SHORTCUTS_FILE).use { writer ->
				SkyblockerMod.GSON.toJson(shortcutsJson, writer)
				LOGGER.info("[Skyblocker] Saved {} command shortcuts and {} command argument shortcuts", commands.size, commandArgs.size)
			}
		} catch (e: IOException) {
			LOGGER.error("[Skyblocker] Failed to save shortcuts file", e)
		}
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		for (key in commands.keys) {
			if (key.startsWith("/")) {
				dispatcher.register(ClientCommandManager.literal(key.substring(1)))
			}
		}
		for (key in commandArgs.keys) {
			if (key.startsWith("/")) {
				dispatcher.register(ClientCommandManager.literal(key.substring(1)).then(ClientCommandManager.argument("args", StringArgumentType.greedyString())))
			}
		}
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("help").executes { context: CommandContext<FabricClientCommandSource> ->
			val source = context.source
			var status = if (SkyblockerConfigManager.config.general.shortcuts.enableShortcuts && SkyblockerConfigManager.config.general.shortcuts.enableCommandShortcuts) "§a§l (Enabled)" else "§c§l (Disabled)"
			source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Shortcuts$status"))
			if (!isShortcutsLoaded()) {
				source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"))
			} else for ((key, value) in commands) {
				source.sendFeedback(Text.of("§7$key §f→ §7$value"))
			}
			status = if (SkyblockerConfigManager.config.general.shortcuts.enableShortcuts && SkyblockerConfigManager.config.general.shortcuts.enableCommandArgShortcuts) "§a§l (Enabled)" else "§c§l (Disabled)"
			source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Argument Shortcuts$status"))
			if (!isShortcutsLoaded()) {
				source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"))
			} else for ((key, value) in commandArgs) {
				source.sendFeedback(Text.of("§7$key §f→ §7$value"))
			}
			source.sendFeedback(Text.of("§e§lSkyblocker §fCommands"))
			for (command in dispatcher.getSmartUsage(dispatcher.root.getChild(SkyblockerMod.NAMESPACE), source).values) {
				source.sendFeedback(Text.of("§7/" + SkyblockerMod.NAMESPACE + " " + command))
			}
			Command.SINGLE_SUCCESS
		}).then(ClientCommandManager.literal("shortcuts").executes(queueOpenScreenCommand { ShortcutsConfigScreen() })))
	}

	private fun modifyCommand(command: String): String {
		var command = command
		if (SkyblockerConfigManager.config.general.shortcuts.enableShortcuts) {
			if (!isShortcutsLoaded()) {
				LOGGER.warn("[Skyblocker] Shortcuts not loaded yet, skipping shortcut for command: {}", command)
				return command
			}
			command = "/$command"
			if (SkyblockerConfigManager.config.general.shortcuts.enableCommandShortcuts) {
				command = commands.getOrDefault(command, command)
			}
			if (SkyblockerConfigManager.config.general.shortcuts.enableCommandArgShortcuts) {
				val messageArgs = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				for (i in messageArgs.indices) {
					messageArgs[i] = commandArgs.getOrDefault(messageArgs[i], messageArgs[i])
				}
				command = java.lang.String.join(" ", *messageArgs)
			}
			return command.substring(1)
		}
		return command
	}
}
