package de.hysky.skyblocker.utils.datafixer

import com.google.gson.JsonElement
import com.google.gson.JsonObject

object JsonHelper {
	fun getInt(root: JsonObject?, path: String) = getTyped(root, path) { asInt }

	fun getBoolean(root: JsonObject?, path: String) = getTyped(root, path) { asBoolean }

	fun getString(root: JsonObject?, path: String) = getTyped(root, path) { asString }

	fun <T> getTyped(root: JsonObject?, path: String, asType: JsonElement.() -> T): T? {
		//If root is null
		root ?: return null

		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return if (root.has(path) && !root[path].isJsonNull) root[path].asType() else null
		}

		val split = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		val propertyName = split[split.size - 1]
		val objects2Traverse = arrayOfNulls<String>(split.size - 1)

		//Get the traversal path
		System.arraycopy(split, 0, objects2Traverse, 0, split.size - 1)

		var currentLevel: JsonObject = root

		for (objectName in objects2Traverse) {
			if (currentLevel.has(objectName) && !currentLevel[objectName].isJsonNull) {
				currentLevel = currentLevel.getAsJsonObject(objectName)
			} else {
				return null
			}
		}

		return if (currentLevel.has(propertyName) && !currentLevel[propertyName].isJsonNull) currentLevel[propertyName].asType() else null
	}
}
