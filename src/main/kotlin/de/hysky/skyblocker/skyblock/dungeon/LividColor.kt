package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.util.*

object LividColor {
	private val WOOL_TO_FORMATTING = mapOf(
		Blocks.RED_WOOL to Formatting.RED,
		Blocks.YELLOW_WOOL to Formatting.YELLOW,
		Blocks.LIME_WOOL to Formatting.GREEN,
		Blocks.GREEN_WOOL to Formatting.DARK_GREEN,
		Blocks.BLUE_WOOL to Formatting.BLUE,
		Blocks.MAGENTA_WOOL to Formatting.LIGHT_PURPLE,
		Blocks.PURPLE_WOOL to Formatting.DARK_PURPLE,
		Blocks.GRAY_WOOL to Formatting.GRAY,
		Blocks.WHITE_WOOL to Formatting.WHITE
	)
	private val LIVID_TO_FORMATTING = mapOf(
		"Hockey Livid" to Formatting.RED,
		"Arcade Livid" to Formatting.YELLOW,
		"Smile Livid" to Formatting.GREEN,
		"Frog Livid" to Formatting.DARK_GREEN,
		"Scream Livid" to Formatting.BLUE,
		"Crossed Livid" to Formatting.LIGHT_PURPLE,
		"Purple Livid" to Formatting.DARK_PURPLE,
		"Doctor Livid" to Formatting.GRAY,
		"Vendetta Livid" to Formatting.WHITE
	)

    val LIVID_NAMES = LIVID_TO_FORMATTING.keys
	val CONFIG = SkyblockerConfigManager.config.dungeons.livid
	private var tenTicks = 0
	private var color: Formatting? = null

	fun init() {
		ClientReceiveMessageEvents.GAME.register{ message, _ ->
			if ((CONFIG.enableLividColorText || CONFIG.enableLividColorTitle || CONFIG.enableLividColorGlow) && (message.string == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
				tenTicks = 8
			}
		}
	}

	fun update() {
		if (tenTicks != 0) {
			val client = MinecraftClient.getInstance()
			if ((CONFIG.enableLividColorText || CONFIG.enableLividColorTitle || CONFIG.enableLividColorGlow) && isInDungeons && (client.world != null)) {
				if (tenTicks == 1) {
					onLividColorFound(client, Blocks.RED_WOOL)
					return
				}
				val color = client.world!!.getBlockState(BlockPos(5, 110, 42)).block
				if (WOOL_TO_FORMATTING.containsKey(color) && color != Blocks.RED_WOOL) {
					onLividColorFound(client, color)
					return
				}
				tenTicks--
			} else {
				tenTicks = 0
			}
		}
	}

	private fun onLividColorFound(client: MinecraftClient, color: Block) {
		LividColor.color = WOOL_TO_FORMATTING[color]
		var colorString = Registries.BLOCK.getId(color).path
		colorString = colorString.substring(0, colorString.length - 5).uppercase(Locale.getDefault())
		val message = Constants.PREFIX.append(CONFIG.lividColorText.replace("\\[color]".toRegex(), colorString)).formatted(LividColor.color)
		if (CONFIG.enableLividColorText) MessageScheduler.sendMessageAfterCooldown(message.string)
		if (CONFIG.enableLividColorTitle) {
			client.inGameHud.setDefaultTitleFade()
			client.inGameHud.setTitle(message)
		}
		tenTicks = 0
	}

    fun allowGlow()= !SkyblockerConfigManager.config.dungeons.livid.enableLividColorGlow || !DungeonManager.boss.isFloor(5)

    fun shouldGlow(name: String) = SkyblockerConfigManager.config.dungeons.livid.enableLividColorGlow && color == LIVID_TO_FORMATTING[name]

    fun getGlowColor(name: String) = if (LIVID_TO_FORMATTING.containsKey(name)) LIVID_TO_FORMATTING[name]!!.colorValue!! else Formatting.WHITE.colorValue!!
}
