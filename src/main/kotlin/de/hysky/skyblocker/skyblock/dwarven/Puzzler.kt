package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.regex.Matcher

class Puzzler : ChatPatternListener("^\\[NPC] Puzzler: ((?:▲|▶|◀|▼){10})$") {
	public override fun state(): ChatFilterResult {
		return if (SkyblockerConfigManager.get().mining.dwarvenMines.solvePuzzler) null else ChatFilterResult.PASS
	}

	public override fun onMatch(message: Text?, matcher: Matcher?): Boolean {
		var x = 181
		var z = 135
		for (c in matcher!!.group(1).toCharArray()) {
			if (c == '▲') z++
			else if (c == '▼') z--
			else if (c == '◀') x++
			else if (c == '▶') x--
		}
		val world = MinecraftClient.getInstance().world
		world?.setBlockState(BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.defaultState)
		return false
	}
}