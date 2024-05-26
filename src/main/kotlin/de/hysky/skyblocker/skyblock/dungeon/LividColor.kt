package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.DungeonsConfig.Livid
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.scheduler.MessageScheduler
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.util.*

object LividColor {
	private val WOOL_TO_FORMATTING: Map<Block, Formatting> = java.util.Map.of(
		Blocks.RED_WOOL, Formatting.RED,
		Blocks.YELLOW_WOOL, Formatting.YELLOW,
		Blocks.LIME_WOOL, Formatting.GREEN,
		Blocks.GREEN_WOOL, Formatting.DARK_GREEN,
		Blocks.BLUE_WOOL, Formatting.BLUE,
		Blocks.MAGENTA_WOOL, Formatting.LIGHT_PURPLE,
		Blocks.PURPLE_WOOL, Formatting.DARK_PURPLE,
		Blocks.GRAY_WOOL, Formatting.GRAY,
		Blocks.WHITE_WOOL, Formatting.WHITE
	)
	private val LIVID_TO_FORMATTING: Map<String, Formatting> = java.util.Map.of(
		"Hockey Livid", Formatting.RED,
		"Arcade Livid", Formatting.YELLOW,
		"Smile Livid", Formatting.GREEN,
		"Frog Livid", Formatting.DARK_GREEN,
		"Scream Livid", Formatting.BLUE,
		"Crossed Livid", Formatting.LIGHT_PURPLE,
		"Purple Livid", Formatting.DARK_PURPLE,
		"Doctor Livid", Formatting.GRAY,
		"Vendetta Livid", Formatting.WHITE
	)
	@JvmField
    val LIVID_NAMES: Set<String> = java.util.Set.copyOf(LIVID_TO_FORMATTING.keys)
	val CONFIG: Livid = SkyblockerConfigManager.get().dungeons.livid
	private var tenTicks = 0
	private var color: Formatting? = null

	fun init() {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message: Text, overlay: Boolean ->
			val config = SkyblockerConfigManager.get().dungeons.livid
			if ((config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow) && (message.string == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
				tenTicks = 8
			}
		})
	}

	fun update() {
		val client = MinecraftClient.getInstance()
		if (tenTicks != 0) {
			val config = SkyblockerConfigManager.get().dungeons.livid
			if ((config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow) && isInDungeons && (client.world != null)) {
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
		val message = Constants.PREFIX.get()
			.append(CONFIG.lividColorText.replace("\\[color]".toRegex(), colorString))
			.formatted(LividColor.color)
		if (CONFIG.enableLividColorText) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown(message.string)
		}
		if (CONFIG.enableLividColorTitle) {
			client.inGameHud.setDefaultTitleFade()
			client.inGameHud.setTitle(message)
		}
		tenTicks = 0
	}

	@JvmStatic
    fun allowGlow(): Boolean {
		return !SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow || !DungeonManager.getBoss().isFloor(5)
	}

	@JvmStatic
    fun shouldGlow(name: String): Boolean {
		return SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow && color == LIVID_TO_FORMATTING[name]
	}

	@JvmStatic
    fun getGlowColor(name: String): Int {
		return if (LIVID_TO_FORMATTING.containsKey(name)) LIVID_TO_FORMATTING[name]!!.colorValue!! else Formatting.WHITE.colorValue!!
	}
}
