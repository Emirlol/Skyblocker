package de.hysky.skyblocker.utils.datafixer

import com.google.gson.JsonObject
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault
import java.util.*

/**
 * Helper methods to assist in retrieving values nested in JSON objects.
 *
 * All methods are fully null safe, whether it be from passing a `null` root object or from encountering a nonexistent or null object/value.
 *
 * @author AzureAaron
 * @see [Aaron's Mod's JSON Helper](https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/utils/JsonHelper.java)
 */
@MethodsReturnNonnullByDefault
object JsonHelper {
	@JvmStatic
	fun getInt(root: JsonObject?, path: String): OptionalInt {
		//If root is null
		if (root == null) return OptionalInt.empty()


		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return if (root.has(path) && !root[path].isJsonNull) OptionalInt.of(root[path].asInt) else OptionalInt.empty()
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
				return OptionalInt.empty()
			}
		}

		return if (currentLevel.has(propertyName) && !currentLevel[propertyName].isJsonNull) OptionalInt.of(currentLevel[propertyName].asInt) else OptionalInt.empty()
	}

	fun getBoolean(root: JsonObject?, path: String): Optional<Boolean> {
		//If root is null
		if (root == null) return Optional.empty()


		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return if (root.has(path) && !root[path].isJsonNull) Optional.of(root[path].asBoolean) else Optional.empty()
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
				return Optional.empty()
			}
		}

		return if (currentLevel.has(propertyName) && !currentLevel[propertyName].isJsonNull) Optional.of(currentLevel[propertyName].asBoolean) else Optional.empty()
	}

	fun getString(root: JsonObject?, path: String): Optional<String> {
		//If root is null
		if (root == null) return Optional.empty()


		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return if (root.has(path) && !root[path].isJsonNull) Optional.of(root[path].asString) else Optional.empty()
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
				return Optional.empty()
			}
		}

		return if (currentLevel.has(propertyName) && !currentLevel[propertyName].isJsonNull) Optional.of(currentLevel[propertyName].asString) else Optional.empty()
	}
}
