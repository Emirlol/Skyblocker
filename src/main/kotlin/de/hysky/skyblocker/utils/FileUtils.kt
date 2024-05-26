package de.hysky.skyblocker.utils

import java.nio.file.Path

object FileUtils {
	/**
	 * Replaces any characters that do not match the regex: [^a-z0-9_.-]
	 *
	 * @implNote Designed to convert a file path to an [net.minecraft.util.Identifier]
	 */
	fun normalizePath(path: Path) = path.toString().lowercase().replace("[^a-z0-9_.-]".toRegex(), "")
}
