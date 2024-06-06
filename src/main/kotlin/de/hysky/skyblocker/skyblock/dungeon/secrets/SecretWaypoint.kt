package de.hysky.skyblocker.skyblock.dungeon.secrets

import com.google.gson.JsonObject
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.DungeonsConfig.SecretWaypoints
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint.Category.CategoryArgumentType
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import de.hysky.skyblocker.utils.waypoint.Waypoint
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.Formatting
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.*

class SecretWaypoint internal constructor(val secretIndex: Int, val category: Category?, val name: Text, pos: BlockPos?) : Waypoint(pos, TYPE_SUPPLIER, category!!.colorComponents) {
	private val centerPos: Vec3d = pos!!.toCenterPos()

	internal constructor(secretIndex: Int, waypoint: JsonObject, name: String?, pos: BlockPos?) : this(secretIndex, Category.get(waypoint), name, pos)

	internal constructor(secretIndex: Int, category: Category?, name: String?, pos: BlockPos?) : this(secretIndex, category, Text.of(name), pos)

	override fun shouldRender(): Boolean {
		return super.shouldRender() && category!!.isEnabled
	}

	fun needsInteraction(): Boolean {
		return category!!.needsInteraction()
	}

	val isLever: Boolean
		get() = category!!.isLever

	fun needsItemPickup(): Boolean {
		return category!!.needsItemPickup()
	}

	val isBat: Boolean
		get() = category!!.isBat

	override fun equals(obj: Any?): Boolean {
		return super.equals(obj) || obj is SecretWaypoint && secretIndex == obj.secretIndex && category == obj.category && name == obj.name && pos == obj.pos
	}

	/**
	 * Renders the secret waypoint, including a waypoint through [Waypoint.render], the name, and the distance from the player.
	 */
	override fun render(context: WorldRenderContext) {
		//TODO In the future, shrink the box for wither essence and items so its more realistic
		super.render(context)

		if (CONFIG.get().showSecretText) {
			val posUp = centerPos.add(0.0, 1.0, 0.0)
			renderText(context, name, posUp, true)
			val distance = context.camera().pos.distanceTo(centerPos)
			renderText(context, Text.literal(Math.round(distance).toString() + "m").formatted(Formatting.YELLOW), posUp, 1f, (MinecraftClient.getInstance().textRenderer.fontHeight + 1).toFloat(), true)
		}
	}

	fun relativeToActual(room: Room): SecretWaypoint {
		return SecretWaypoint(secretIndex, category, name, room.relativeToActual(pos))
	}

	enum class Category(override val name: String, private val enabledPredicate: Predicate<SecretWaypoints>, vararg intColorComponents: Int) : StringIdentifiable {
		ENTRANCE("entrance", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableEntranceWaypoints }, 0, 255, 0),
		SUPERBOOM("superboom", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableSuperboomWaypoints }, 255, 0, 0),
		CHEST("chest", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableChestWaypoints }, 2, 213, 250),
		ITEM("item", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableItemWaypoints }, 2, 64, 250),
		BAT("bat", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableBatWaypoints }, 142, 66, 0),
		WITHER("wither", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableWitherWaypoints }, 30, 30, 30),
		LEVER("lever", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableLeverWaypoints }, 250, 217, 2),
		FAIRYSOUL("fairysoul", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableFairySoulWaypoints }, 255, 85, 255),
		STONK("stonk", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableStonkWaypoints }, 146, 52, 235),
		AOTV("aotv", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableAotvWaypoints }, 252, 98, 3),
		PEARL("pearl", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enablePearlWaypoints }, 57, 117, 125),
		DEFAULT("default", Predicate { secretWaypoints: SecretWaypoints -> secretWaypoints.enableDefaultWaypoints }, 190, 255, 252);

		val colorComponents: FloatArray = FloatArray(intColorComponents.size)

		init {
			for (i in intColorComponents.indices) {
				colorComponents[i] = intColorComponents[i] / 255f
			}
		}

		fun needsInteraction(): Boolean {
			return this == CHEST || this == WITHER
		}

		val isLever: Boolean
			get() = this == LEVER

		fun needsItemPickup(): Boolean {
			return this == ITEM
		}

		val isBat: Boolean
			get() = this == BAT

		val isEnabled: Boolean
			get() = enabledPredicate.test(SkyblockerConfigManager.config.dungeons.secretWaypoints)

		override fun toString(): String {
			return name
		}

		override fun asString(): String {
			return name
		}

		internal object CategoryArgumentType : EnumArgumentType<Category?>() {
			fun category(): CategoryArgumentType {
				return CategoryArgumentType()
			}

			fun <S> getCategory(context: CommandContext<S>, name: String?): Category {
				return context.getArgument(name, Category::class.java)
			}
		}

		companion object {
			val CODEC: Codec<Category?> = StringIdentifiable.createCodec(Supplier { entries.toTypedArray() })
			fun get(waypointJson: JsonObject): Category? {
				return CODEC.parse(JsonOps.INSTANCE, waypointJson["category"]).resultOrPartial { msg: String? -> LOGGER.error(msg) }.orElse(DEFAULT)
			}
		}
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(SecretWaypoint::class.java)
		val CODEC: Codec<SecretWaypoint> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<SecretWaypoint> ->
			instance.group(
				Codec.INT.fieldOf("secretIndex").forGetter { secretWaypoint: SecretWaypoint -> secretWaypoint.secretIndex },
				Category.CODEC.fieldOf("category").forGetter { secretWaypoint: SecretWaypoint -> secretWaypoint.category },
				TextCodecs.CODEC.fieldOf("name").forGetter { secretWaypoint: SecretWaypoint -> secretWaypoint.name },
				BlockPos.CODEC.fieldOf("pos").forGetter { secretWaypoint: SecretWaypoint -> secretWaypoint.pos }
			).apply(instance) { secretIndex: Int, category: Category?, name: Text, pos: BlockPos? -> SecretWaypoint(secretIndex, category, name, pos) }
		}
		val LIST_CODEC: Codec<List<SecretWaypoint>> = CODEC.listOf()
		val SECRET_ITEMS: List<String> = listOf("Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman")
		private val CONFIG = Supplier { SkyblockerConfigManager.config.dungeons.secretWaypoints }
		val TYPE_SUPPLIER: Supplier<Type> = Supplier { CONFIG.get().waypointType }
		fun getSquaredDistanceToFunction(entity: Entity): ToDoubleFunction<SecretWaypoint> {
			return ToDoubleFunction { secretWaypoint: SecretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos) }
		}

		fun getRangePredicate(entity: Entity): Predicate<SecretWaypoint> {
			return Predicate { secretWaypoint: SecretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos) <= 36.0 }
		}
	}
}
