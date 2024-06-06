package de.hysky.skyblocker.skyblock.item

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils
import de.hysky.skyblocker.utils.TextHandler
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW

object WikiLookup {
    val wikiLookup: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.wikiLookup", GLFW.GLFW_KEY_F4, "key.categories.skyblocker"))

    fun openWiki(slot: Slot, player: PlayerEntity) {
		if (!isOnSkyblock || !SkyblockerConfigManager.config.general.wikiLookup.enableWikiLookup) return

		ItemUtils.getItemId(slot.stack)?.apply { ItemRepository.getWikiLink(this) }.let { wikiLink ->
			if (wikiLink == null) {
				player.sendMessage(Constants.PREFIX.append(Text.translatable("skyblocker.wikiLookup.noArticleFound")), false)
			} else {
				SkyblockerMod.globalJob.launch { Util.getOperatingSystem().open(wikiLink) }.invokeOnCompletion {
					if (it != null) {
						TextHandler.error("Error while retrieving wiki article...", it)
						player.sendMessage(Constants.PREFIX.append("Failed to retrieve wiki article, see logs for details."), false)
					}
				}
			}
		}
	}
}
