package de.hysky.skyblocker.skyblock.dungeon

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

class Reparty : ChatPatternListener(
	"""
	^(?:You are not currently in a party\.|Party (?:Membe|Moderato)rs(?: \(([0-9]+)\)|:( .*))|([\[A-z+\]]* )?(?<disband>.*) has disbanded .*|.*
	([\[A-z+\]]* )?(?<invite>.*) has invited you to join their party!
	You have 60 seconds to accept. Click here to join!
	.*)$
	""".trimIndent()
) {
	private var players: Array<String?>
	private var playersSoFar = 0
	private var repartying = false
	private var partyLeader: String? = null

	init {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(ClientCommandManager.literal("rp").executes { context: CommandContext<FabricClientCommandSource?>? ->
				if (!isOnSkyblock || this.repartying || client.player == null) return@executes 0
				this.repartying = true
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/p list")
				0
			})
		})
	}

	public override fun state(): ChatFilterResult {
		return if ((SkyblockerConfigManager.get().general.acceptReparty || this.repartying)) ChatFilterResult.FILTER else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		if (matcher!!.group(1) != null && repartying) {
			this.playersSoFar = 0
			this.players = arrayOfNulls(matcher.group(1).toInt() - 1)
		} else if (matcher.group(2) != null && repartying) {
			val m = PLAYER.matcher(matcher.group(2))
			while (m.find()) {
				players[playersSoFar++] = m.group(1)
			}
		} else if (matcher.group("disband") != null && matcher.group("disband") != client.session.username) {
			partyLeader = matcher.group("disband")
			Scheduler.INSTANCE.schedule({ partyLeader = null }, 61)
			return false
		} else if (matcher.group("invite") != null && matcher.group("invite") == partyLeader) {
			val command = "/party accept $partyLeader"
			sendCommand(command, 0)
			return false
		} else {
			this.repartying = false
			return false
		}
		if (this.playersSoFar == players.size) {
			reparty()
		}
		return false
	}

	private fun reparty() {
		val playerEntity = client.player
		if (playerEntity == null) {
			this.repartying = false
			return
		}
		sendCommand("/p disband", 1)
		for (i in players.indices) {
			val command = "/p invite " + players[i]
			sendCommand(command, i + 2)
		}
		Scheduler.INSTANCE.schedule({ this.repartying = false }, players.size + 2)
	}

	private fun sendCommand(command: String, delay: Int) {
		MessageScheduler.INSTANCE.queueMessage(command, delay * BASE_DELAY)
	}

	companion object {
		private val client: MinecraftClient = MinecraftClient.getInstance()
		val PLAYER: Pattern = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●")
		private const val BASE_DELAY = 10
	}
}