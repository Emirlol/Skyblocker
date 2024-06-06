package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.config.SkyblockerConfigManager

interface TerminalSolver {
	fun shouldBlockIncorrectClicks(): Boolean {
		return SkyblockerConfigManager.config.dungeons.terminals.blockIncorrectClicks
	}
}
