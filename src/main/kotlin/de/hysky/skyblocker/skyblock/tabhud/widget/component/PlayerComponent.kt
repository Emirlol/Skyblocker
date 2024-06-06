package de.hysky.skyblocker.skyblock.tabhud.widget.component

import de.hysky.skyblocker.config.SkyblockerConfigManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.PlayerSkinDrawer
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * Component that consists of a player's skin icon and their name
 */
class PlayerComponent(ple: PlayerListEntry) : Component() {
	private val name: Text
	private val tex: Identifier

	init {
		val plainNames = SkyblockerConfigManager.config.uiAndVisuals.tabHud.plainPlayerNames
		val team = ple.scoreboardTeam
		val username = ple.profile.name
		name = if ((team != null && !plainNames)) Text.empty().append(team.prefix).append(Text.literal(username).formatted(team.color)).append(team.suffix) else Text.of(username)
		tex = ple.skinTextures.texture()

		this.width = SKIN_ICO_DIM + Component.Companion.PAD_S + Component.Companion.txtRend.getWidth(name)
		this.height = Component.Companion.txtRend.fontHeight
	}

	override fun render(context: DrawContext, x: Int, y: Int) {
		PlayerSkinDrawer.draw(context, tex, x, y, SKIN_ICO_DIM)
		context.drawText(Component.Companion.txtRend, name, x + SKIN_ICO_DIM + Component.Companion.PAD_S, y, -0x1, false)
	}

	companion object {
		private const val SKIN_ICO_DIM = 8
	}
}
