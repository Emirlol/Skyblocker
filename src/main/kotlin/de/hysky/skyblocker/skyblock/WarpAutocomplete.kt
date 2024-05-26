package de.hysky.skyblocker.skyblock

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.Http.sendGetRequest
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * the mixin [de.hysky.skyblocker.mixins.CommandTreeS2CPacketMixin]
 */
object WarpAutocomplete {
	private val LOGGER: Logger = LoggerFactory.getLogger(WarpAutocomplete::class.java)
	@JvmField
    var commandNode: LiteralCommandNode<FabricClientCommandSource>? = null

	fun init() {
		CompletableFuture.supplyAsync {
			try {
				val jsonElements = SkyblockerMod.GSON.fromJson(sendGetRequest("https://hysky.de/api/locations"), JsonArray::class.java)
				return@supplyAsync jsonElements.asList().stream().map<String> { obj: JsonElement -> obj.asString }.toList()
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker] Failed to download warps list", e)
			}
			listOf()
		}.thenAccept { warps: List<String>? ->
			commandNode = ClientCommandManager.literal("warp")
				.requires { fabricClientCommandSource: FabricClientCommandSource? -> isOnSkyblock }
				.then(ClientCommandManager.argument("destination", StringArgumentType.string())
					.suggests { context: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(warps, builder) }
				).build()
		}
	}
}
