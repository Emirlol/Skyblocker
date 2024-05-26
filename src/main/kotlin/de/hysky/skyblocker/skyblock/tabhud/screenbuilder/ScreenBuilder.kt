package de.hysky.skyblocker.skyblock.tabhud.screenbuilder

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.*
import de.hysky.skyblocker.skyblock.tabhud.widget.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import java.lang.reflect.InvocationTargetException

class ScreenBuilder(ident: Identifier) {
	// layout pipeline
	private val layoutPipeline = ArrayList<PipelineStage>()

	// all widget instances this builder knows
	private val instances = ArrayList<Widget>()

	// maps alias -> widget instance
	private val objectMap = HashMap<String, Widget>()

	private var builderName: String? = null

	/**
	 * Create a ScreenBuilder from a json.
	 */
	init {
		try {
			MinecraftClient.getInstance().resourceManager.openAsReader(ident).use { reader ->
				this.builderName = ident.path
				val json = JsonParser.parseReader(reader).asJsonObject

				val widgets = json.getAsJsonArray("widgets")
				val layout = json.getAsJsonArray("layout")

				for (w in widgets) {
					val widget = w.asJsonObject
					val name = widget["name"].asString
					val alias = widget["alias"].asString

					val wid = instanceFrom(name, widget)
					objectMap[alias] = wid
					instances.add(wid)
				}
				for (l in layout) {
					val ps = createStage(l.asJsonObject)
					layoutPipeline.add(ps)
				}
			}
		} catch (ex: Exception) {
			// rethrow as unchecked exception so that I don't have to catch anything in the ScreenMaster
			throw IllegalStateException("Failed to load file " + ident + ". Reason: " + ex.message)
		}
	}

	/**
	 * Try to find a class in the widget package that has the supplied name and
	 * call it's constructor. Manual work is required if the class has arguments.
	 */
	fun instanceFrom(name: String, widget: JsonObject): Widget {
		// do widgets that require args the normal way

		val arg: JsonElement?
		when (name) {
			"EventWidget" -> {
				return EventWidget(widget["inGarden"].asBoolean)
			}

			"DungeonPlayerWidget" -> {
				return DungeonPlayerWidget(widget["player"].asInt)
			}

			"ErrorWidget" -> {
				arg = widget["text"]
				return if (arg == null) {
					ErrorWidget()
				} else {
					ErrorWidget(arg.asString)
				}
			}

			"Widget" ->  // clown case sanity check. don't instantiate the superclass >:|
				throw NoSuchElementException("$builderName[ERROR]: No such Widget type \"Widget\"!")
		}
		// reflect something together for the "normal" ones.

		// list all packages that might contain widget classes
		// using Package isn't reliable, as some classes might not be loaded yet,
		// causing the packages not to show.
		val packbase = "de.hysky.skyblocker.skyblock.tabhud.widget"
		val packnames = arrayOf(
			packbase,
			"$packbase.rift"
		)

		// construct the full class name and try to load.
		var clazz: Class<*>? = null
		for (pn in packnames) {
			try {
				clazz = Class.forName("$pn.$name")
			} catch (ex: LinkageError) {
				continue
			} catch (ex: ClassNotFoundException) {
				continue
			}
		}

		// load failed.
		if (clazz == null) {
			throw NoSuchElementException("$builderName/[ERROR]: No such Widget type \"$name\"!")
		}

		// return instance of that class.
		try {
			val ctor = clazz.getConstructor()
			return ctor.newInstance() as Widget
		} catch (ex: NoSuchMethodException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		} catch (ex: InstantiationException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		} catch (ex: IllegalAccessException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		} catch (ex: IllegalArgumentException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		} catch (ex: InvocationTargetException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		} catch (ex: SecurityException) {
			throw IllegalStateException("$builderName/$name: Internal error...")
		}
	}

	/**
	 * Create a PipelineStage from a json object.
	 */
	@Throws(NoSuchElementException::class)
	fun createStage(descr: JsonObject): PipelineStage {
		val op = descr["op"].asString

		return when (op) {
			"place" -> PlaceStage(this, descr)
			"stack" -> StackStage(this, descr)
			"align" -> AlignStage(this, descr)
			"collideAgainst" -> CollideStage(this, descr)
			else -> throw NoSuchElementException("No such op " + op + " as requested by " + this.builderName)
		}
	}

	/**
	 * Lookup Widget instance from alias name
	 */
	fun getInstance(name: String): Widget? {
		if (!objectMap.containsKey(name)) {
			throw NoSuchElementException("No widget with alias $name in screen $builderName")
		}
		return objectMap[name]
	}

	/**
	 * Run the pipeline to build a Screen
	 */
	fun run(context: DrawContext, screenW: Int, screenH: Int) {
		for (w in instances) {
			w.update()
		}
		for (ps in layoutPipeline) {
			ps.run(screenW, screenH)
		}
		for (w in instances) {
			w.render(context)
		}
	}
}
