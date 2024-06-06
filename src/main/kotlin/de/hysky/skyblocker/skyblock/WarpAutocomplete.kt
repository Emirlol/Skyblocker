package de.hysky.skyblocker.skyblock

import com.google.gson.JsonArray
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.Http.sendGetRequest
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandSource

/**
 * the mixin [de.hysky.skyblocker.mixins.CommandTreeS2CPacketMixin]
 */
@OptIn(ExperimentalCoroutinesApi::class)
object WarpAutocomplete {
	var commandNode: LiteralCommandNode<FabricClientCommandSource>? = null

	init {
		val deferred = SkyblockerMod.globalJob.async {
			try {
				val jsonElements = SkyblockerMod.GSON.fromJson(sendGetRequest("https://hysky.de/api/locations"), JsonArray::class.java)
				jsonElements.asSequence().map { it.asString }.toList()
			} catch (e: Exception) {
				TextHandler.error("[Warp Autocomplete] Failed to download warps list", e)
			}
			emptyList<String>()
		}

		deferred.invokeOnCompletion {
			if (it != null) return@invokeOnCompletion
			commandNode = ClientCommandManager.literal("warp")
				.requires { Utils.isOnSkyblock }
				.then(ClientCommandManager.argument("destination", StringArgumentType.string())
					.suggests { _, builder -> CommandSource.suggestMatching(deferred.getCompleted(), builder) }
				).build()
		}
	}
}
