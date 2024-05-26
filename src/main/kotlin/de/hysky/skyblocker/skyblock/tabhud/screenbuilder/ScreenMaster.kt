package de.hysky.skyblocker.skyblock.tabhud.screenbuilder

import com.google.gson.JsonParser
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.tabhud.TabHud
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerLocator
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ScreenMaster {
	private val LOGGER: Logger = LoggerFactory.getLogger("skyblocker")

	private const val VERSION = 1

	private val standardMap = HashMap<String?, ScreenBuilder>()
	private val screenAMap = HashMap<String?, ScreenBuilder>()
	private val screenBMap = HashMap<String?, ScreenBuilder>()

	/**
	 * Load a screen mapping from an identifier
	 */
	fun load(ident: Identifier) {
		val path = ident.path
		val parts = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		val screenType = parts[parts.size - 2]
		var location = parts[parts.size - 1]
		location = location.replace(".json", "")

		val sb = ScreenBuilder(ident)
		when (screenType) {
			"standard" -> standardMap[location] = sb
			"screen_a" -> screenAMap[location] = sb
			"screen_b" -> screenBMap[location] = sb
		}
	}

	/**
	 * Top level render method.
	 * Calls the appropriate ScreenBuilder with the screen's dimensions
	 */
	@JvmStatic
	fun render(context: DrawContext, w: Int, h: Int) {
		val location = PlayerLocator.getPlayerLocation().internal
		val lookup = if (TabHud.toggleA!!.isPressed) {
			screenAMap
		} else if (TabHud.toggleB!!.isPressed) {
			screenBMap
		} else {
			standardMap
		}

		var sb = lookup[location]
		// seems suboptimal, maybe load the default first into all possible values
		// and then override?
		if (sb == null) {
			sb = lookup["default"]
		}

		sb!!.run(context, w, h)
	}

	fun init() {
		// WHY MUST IT ALWAYS BE SUCH NESTED GARBAGE MINECRAFT KEEP THAT IN DFU FFS

		ResourceManagerHelper.registerBuiltinResourcePack(
			Identifier(SkyblockerMod.NAMESPACE, "top_aligned"),
			SkyblockerMod.SKYBLOCKER_MOD,
			ResourcePackActivationType.NORMAL
		)

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener( // ...why are we instantiating an interface again?
			object : SimpleSynchronousResourceReloadListener {
				override fun getFabricId(): Identifier {
					return Identifier("skyblocker", "tabhud")
				}

				override fun reload(manager: ResourceManager) {
					standardMap.clear()
					screenAMap.clear()
					screenBMap.clear()

					var excnt = 0

					for ((key) in manager
						.findResources("tabhud") { path: Identifier -> path.path.endsWith("version.json") }) {
						try {
							MinecraftClient.getInstance().resourceManager
								.openAsReader(key).use { reader ->
									val json = JsonParser.parseReader(reader).asJsonObject
									check(json["format_version"].asInt == VERSION) { String.format("Resource pack isn't compatible! Expected version %d, got %d", VERSION, json["format_version"].asInt) }
								}
						} catch (ex: Exception) {
							throw IllegalStateException(
								"Rejected this resource pack. Reason: " + ex.message
							)
						}
					}

					for ((key) in manager
						.findResources("tabhud") { path: Identifier -> path.path.endsWith(".json") && !path.path.endsWith("version.json") }) {
						try {
							load(key)
						} catch (e: Exception) {
							LOGGER.error(e.message)
							excnt++
						}
					}
					check(excnt <= 0) { "This screen definition isn't valid, see above" }
				}
			})
	}
}
