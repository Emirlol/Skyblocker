package de.hysky.skyblocker.skyblock.item

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemIdOptional
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.function.Function

object WikiLookup {
	private val LOGGER: Logger = LoggerFactory.getLogger(WikiLookup::class.java)
	@JvmField
    var wikiLookup: KeyBinding? = null

	fun init() {
		wikiLookup = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.wikiLookup",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				"key.categories.skyblocker"
			)
		)
	}

	@JvmStatic
    fun openWiki(slot: Slot, player: PlayerEntity) {
		if (!isOnSkyblock || !SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) return

		getItemIdOptional(slot.stack)
			.map<String>(Function<String, String> { obj: String -> obj.getWikiLink() })
			.ifPresentOrElse({ wikiLink: String? ->
				CompletableFuture.runAsync { Util.getOperatingSystem().open(wikiLink) }.exceptionally { e: Throwable? ->
					LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e)
					player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false)
					null
				}
			}, { player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.wikiLookup.noArticleFound")), false) })
	}
}
