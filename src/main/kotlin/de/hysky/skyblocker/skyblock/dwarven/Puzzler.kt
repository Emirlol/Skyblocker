package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.utils.chat.ChatFilterResult
import de.hysky.skyblocker.utils.chat.ChatPatternListener
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.regex.Matcher

object Puzzler : ChatPatternListener("^\\[NPC] Puzzler: ([▲▶◀▼]{10})$") {
	public override fun state(): ChatFilterResult = ChatFilterResult.PASS

	override fun onMatch(message: Text, matcher: Matcher): Boolean {
		var x = 181
		var z = 135
		for (c in matcher.group(1)) {
			when (c) {
				'▲' -> z++
				'▼' -> z--
				'◀' -> x++
				'▶' -> x--
			}
		}
		MinecraftClient.getInstance().world?.setBlockState(BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.defaultState)
		return false
	}
}