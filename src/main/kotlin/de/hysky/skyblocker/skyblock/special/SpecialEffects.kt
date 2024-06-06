package de.hysky.skyblocker.skyblock.special

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.render.RenderHelper.runOnRenderThread
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

object SpecialEffects {
	private val LOGGER: Logger = LoggerFactory.getLogger(SpecialEffects::class.java)
	private val DROP_PATTERN: Pattern = Pattern.compile("(?:\\[[A-Z+]+] )?(?<player>[A-Za-z0-9_]+) unlocked (?<item>.+)!")

	fun init() {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, message: Boolean -> displayRareDropEffect(message) })
	}

	private fun displayRareDropEffect(message: Text, overlay: Boolean) {
		//We don't check if we're in dungeons because that check doesn't work in m7 which defeats the point of this
		//It might also allow it to work with Croesus
		if (isOnSkyblock && SkyblockerConfigManager.config.general.specialEffects.rareDungeonDropEffects && !overlay) {
			try {
				val stringForm = message.string
				val matcher = DROP_PATTERN.matcher(stringForm)

				if (matcher.matches()) {
					val client = MinecraftClient.getInstance()
					val player = matcher.group("player")

					if (player == client.session.username) {
						val stack = getStackFromName(matcher.group("item"))

						if (stack != null && !stack.isEmpty) {
							runOnRenderThread {
								client.particleManager.addEmitter(client.player, ParticleTypes.PORTAL, 30)
								client.gameRenderer.showFloatingItem(stack)
							}
						}
					}
				}
			} catch (e: Exception) { //In case there's a regex failure or something else bad happens
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered: ", e)
			}
		}
	}

	private fun getStackFromName(itemName: String): ItemStack? {
		val itemId = when (itemName) {
			"Necron Dye" -> "NECRON_DYE"
			"Dark Claymore" -> "DARK_CLAYMORE"
			"Necron's Handle", "Shiny Necron's Handle" -> "NECRON_HANDLE"
			"Enchanted Book (Thunderlord VII)" -> "ENCHANTED_BOOK"
			"Master Skull - Tier 5" -> "MASTER_SKULL_TIER_5"
			"Shadow Warp", "Wither Shield", "Implosion" -> "IMPLOSION_SCROLL"
			"Fifth Master Star" -> "FIFTH_MASTER_STAR"
			"Giant's Sword" -> "GIANTS_SWORD"
			else -> "NONE"
		}

		return ItemRepository.getItemStack(itemId)
	}
}
