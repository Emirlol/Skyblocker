package de.hysky.skyblocker.skyblock.dungeon

import com.mojang.brigadier.Command
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

object Reparty : ChatPatternListener(
	"""
	^(?:You are not currently in a party\.|Party (?:Membe|Moderato)rs(?: \(([0-9]+)\)|:( .*))|([\[\D+\]]* )?(?<disband>.*) has disbanded .*|.*
	([\[A-z+\]]* )?(?<invite>.*) has invited you to join their party!
	You have 60 seconds to accept. Click here to join!
	.*)$
	""".trimIndent()
) {
	private var players: Array<String?> = arrayOf()
	private var playersSoFar = 0
	private var repartying = false
	private var partyLeader: String? = null
	private val PLAYER = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●")
	private const val BASE_DELAY = 10


	init {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
			dispatcher.register(ClientCommandManager.literal("rp").executes {
				if (!isOnSkyblock || this.repartying || MinecraftClient.getInstance().player == null) return@executes 0
				this.repartying = true
				MessageScheduler.sendMessageAfterCooldown("/p list")
				Command.SINGLE_SUCCESS
			})
		})
	}

	override fun state() = if (SkyblockerConfigManager.config.general.acceptReparty || this.repartying) ChatFilterResult.FILTER else ChatFilterResult.PASS

	override fun onMatch(message: Text, matcher: Matcher): Boolean {
		when {
			matcher.group(1) != null && repartying -> {
				this.playersSoFar = 0
				this.players = arrayOfNulls(matcher.group(1).toInt() - 1)
			}
			matcher.group(2) != null && repartying -> {
				val m = PLAYER.matcher(matcher.group(2))
				while (m.find()) {
					players[playersSoFar++] = m.group(1)
				}
			}
			matcher.group("disband") != null && matcher.group("disband") != MinecraftClient.getInstance().session.username -> {
				partyLeader = matcher.group("disband")
				Scheduler.schedule(61) { partyLeader = null }
				return false
			}
			matcher.group("invite") != null && matcher.group("invite") == partyLeader -> {
				val command = "/party accept $partyLeader"
				sendCommand(command, 0)
				return false
			}
			else -> {
				this.repartying = false
				return false
			}
		}
		if (this.playersSoFar == players.size) {
			reparty()
		}
		return false
	}

	private fun reparty() {
		val playerEntity = MinecraftClient.getInstance().player
		if (playerEntity == null) {
			this.repartying = false
			return
		}
		sendCommand("/p disband", 1)
		for (i in players.indices) {
			val command = "/p invite " + players[i]
			sendCommand(command, i + 2)
		}
		Scheduler.schedule(players.size + 2) { this.repartying = false }
	}

	private fun sendCommand(command: String, delay: Int) {
		MessageScheduler.queueMessage(command, delay * BASE_DELAY)
	}
}