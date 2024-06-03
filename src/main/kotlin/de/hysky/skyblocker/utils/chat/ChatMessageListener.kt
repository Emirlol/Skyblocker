package de.hysky.skyblocker.utils.chat

import de.hysky.skyblocker.skyblock.barn.HungryHiker
import de.hysky.skyblocker.skyblock.barn.TreasureHunter
import de.hysky.skyblocker.skyblock.dungeon.Reparty
import de.hysky.skyblocker.skyblock.dungeon.puzzle.Trivia
import de.hysky.skyblocker.skyblock.dwarven.Fetchur
import de.hysky.skyblocker.skyblock.dwarven.Puzzler
import de.hysky.skyblocker.skyblock.filters.*
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun interface ChatMessageListener {
	fun onMessage(message: Text, asString: String): ChatFilterResult

	companion object {
		/**
		 * Registers [ChatMessageListener]s to [ChatMessageListener.EVENT] and registers [ChatMessageListener.EVENT] to [ClientReceiveMessageEvents.ALLOW_GAME]
		 */
		fun init() {
			val listeners = arrayOf<ChatMessageListener>( // Features
				Fetchur,
				Puzzler(),
				Reparty(),
				Trivia(),
				TreasureHunter(),
				HungryHiker(),  // Filters
				AbilityFilter(),
				AdFilter(),
				AoteFilter(),
				ComboFilter(),
				HealFilter(),
				ImplosionFilter(),
				MoltenWaveFilter(),
				TeleportPadFilter(),
				AutopetFilter(),
				ShowOffFilter(),
				ToggleSkyMallFilter(),
				MimicFilter(),
				DeathFilter(),
				DicerFilter()
			)
			// Register all listeners to EVENT
			for (listener in listeners) {
				EVENT.register(listener)
			}
			// Register EVENT to ClientReceiveMessageEvents.ALLOW_GAME from fabric api
			ClientReceiveMessageEvents.ALLOW_GAME.register(AllowGame { message: Text, overlay: Boolean ->
				if (!isOnSkyblock) {
					return@register true
				}
				val result = EVENT.invoker().onMessage(message, Formatting.strip(message.string))
				when (result) {
					ChatFilterResult.ACTION_BAR -> {
						if (overlay) {
							return@register true
						}
						val player = MinecraftClient.getInstance().player
						if (player != null) {
							player.sendMessage(message, true)
							return@register false
						}
					}

					ChatFilterResult.FILTER -> {
						return@register false
					}
				}
				true
			})
		}

		/**
		 * An event called when a game message is received. Register your listeners in [ChatMessageListener.init].
		 */
		val EVENT: Event<ChatMessageListener> = EventFactory.createArrayBacked(ChatMessageListener::class.java) {
			ChatMessageListener { message: Text, asString: String ->
				for (listener in it) {
					val result = listener.onMessage(message, asString)
					if (result != ChatFilterResult.PASS) return@ChatMessageListener result
				}
				ChatFilterResult.PASS
			}
		}
	}
}
