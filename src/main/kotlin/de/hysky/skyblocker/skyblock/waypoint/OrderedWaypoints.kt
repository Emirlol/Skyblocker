package de.hysky.skyblocker.skyblock.waypoint

import com.google.common.primitives.Floats
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.item.CustomArmorDyeColors
import de.hysky.skyblocker.skyblock.waypoint.OrderedWaypoints.ColeWeightWaypoint
import de.hysky.skyblocker.skyblock.waypoint.OrderedWaypoints.OrderedWaypointGroup
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Utils.isInCrystalHollows
import de.hysky.skyblocker.utils.Utils.isInDwarvenMines
import de.hysky.skyblocker.utils.render.RenderHelper.renderLineFromCursor
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import de.hysky.skyblocker.utils.waypoint.Waypoint
import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.objects.Object2IntFunction
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.PosArgument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import java.util.function.Function
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object OrderedWaypoints {
	private val LOGGER: Logger = LogUtils.getLogger()
	private val SERIALIZATION_CODEC: Codec<Map<String, OrderedWaypointGroup>> = Codec.unboundedMap<String, OrderedWaypointGroup>(Codec.STRING, OrderedWaypointGroup.CODEC).xmap<Map<String, OrderedWaypointGroup>>(Function<Map<String?, OrderedWaypointGroup?>, Map<String, OrderedWaypointGroup>> { m: Map<String?, OrderedWaypointGroup?>? -> Object2ObjectOpenHashMap(m) }, Function<Map<String?, OrderedWaypointGroup?>, Map<String, OrderedWaypointGroup>> { m: Map<String?, OrderedWaypointGroup?>? -> Object2ObjectOpenHashMap(m) })
	private const val PREFIX = "[Skyblocker::OrderedWaypoints::v1]"
	private val PATH: Path = SkyblockerMod.CONFIG_DIR.resolve("ordered_waypoints.json")
	private val WAYPOINTS: MutableMap<String, OrderedWaypointGroup> = Object2ObjectOpenHashMap()
	private val SEMAPHORE = Semaphore(1)
	private val INDEX_STORE = Object2IntOpenHashMap<String>()
	private const val RADIUS = 2
	private val LIGHT_GRAY = floatArrayOf(192 / 255f, 192 / 255f, 192 / 255f)

	private var loaded: CompletableFuture<Void>? = null
	private var showAll = false

	fun init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { _client: MinecraftClient? -> load() })
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { _client: MinecraftClient? -> save() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommands(dispatcher) })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> render() })
	}

	private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
			.then(ClientCommandManager.literal("waypoints")
				.then(ClientCommandManager.literal("ordered")
					.then(ClientCommandManager.literal("add")
						.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
							.suggests { source: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(WAYPOINTS.keys, builder) }
							.then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
								.executes { context: CommandContext<FabricClientCommandSource> -> addWaypoint(context.source, StringArgumentType.getString(context, "groupName"), context.getArgument("pos", PosArgument::class.java), Int.MIN_VALUE, null) }
								.then(ClientCommandManager.argument("hex", StringArgumentType.word())
									.executes { context: CommandContext<FabricClientCommandSource> -> addWaypoint(context.source, StringArgumentType.getString(context, "groupName"), context.getArgument("pos", PosArgument::class.java), Int.MIN_VALUE, StringArgumentType.getString(context, "hex")) })
							)
						)
					)
					.then(ClientCommandManager.literal("addAt")
						.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
							.suggests { source: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(WAYPOINTS.keys, builder) }
							.then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
								.then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
									.executes { context: CommandContext<FabricClientCommandSource> -> addWaypoint(context.source, StringArgumentType.getString(context, "groupName"), context.getArgument("pos", PosArgument::class.java), IntegerArgumentType.getInteger(context, "index"), null) }
									.then(ClientCommandManager.argument("hex", StringArgumentType.word())
										.executes { context: CommandContext<FabricClientCommandSource> -> addWaypoint(context.source, StringArgumentType.getString(context, "groupName"), context.getArgument("pos", PosArgument::class.java), IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "hex")) })
								)
							)
						)
					)
					.then(ClientCommandManager.literal("remove")
						.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
							.suggests { source: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(WAYPOINTS.keys, builder) }
							.executes { context: CommandContext<FabricClientCommandSource> -> removeWaypointGroup(context.source, StringArgumentType.getString(context, "groupName")) }
							.then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
								.executes { context: CommandContext<FabricClientCommandSource> -> removeWaypoint(context.source, StringArgumentType.getString(context, "groupName"), context.getArgument("pos", PosArgument::class.java), Int.MIN_VALUE) })
						)
					)
					.then(ClientCommandManager.literal("removeAt")
						.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
							.suggests { source: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(WAYPOINTS.keys, builder) }
							.then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
								.executes { context: CommandContext<FabricClientCommandSource> -> removeWaypoint(context.source, StringArgumentType.getString(context, "groupName"), null, IntegerArgumentType.getInteger(context, "index")) })
						)
					)
					.then(ClientCommandManager.literal("toggle")
						.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
							.suggests { source: CommandContext<FabricClientCommandSource?>?, builder: SuggestionsBuilder? -> CommandSource.suggestMatching(WAYPOINTS.keys, builder) }
							.executes { context: CommandContext<FabricClientCommandSource> -> toggleGroup(context.source, StringArgumentType.getString(context, "groupName")) })
					)
					.then(ClientCommandManager.literal("showAll")
						.executes { context: CommandContext<FabricClientCommandSource> -> showAll(context.source) })
					.then(
						ClientCommandManager.literal("import")
							.then(
								ClientCommandManager.literal("coleWeight")
									.then(ClientCommandManager.argument("groupName", StringArgumentType.word())
										.executes { context: CommandContext<FabricClientCommandSource> -> fromColeWeightFormat(context.source, StringArgumentType.getString(context, "groupName")) })
							)
							.then(ClientCommandManager.literal("skyblocker")
								.executes { context: CommandContext<FabricClientCommandSource> -> fromSkyblockerFormat(context.source) })
					)
					.then(ClientCommandManager.literal("export")
						.executes { context: CommandContext<FabricClientCommandSource> -> export(context.source) })
				)
			)
		)
	}

	private fun addWaypoint(source: FabricClientCommandSource, groupName: String, posArgument: PosArgument, index: Int, hex: String?): Int {
		val pos = posArgument.toAbsoluteBlockPos(ServerCommandSource(null, source.position, source.rotation, null, 0, null, null, null, null))

		SEMAPHORE.acquireUninterruptibly()

		if (hex != null && !CustomArmorDyeColors.isHexadecimalColor(hex)) {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.add.invalidHexColor")))
			SEMAPHORE.release()

			return Command.SINGLE_SUCCESS
		}

		val rgb = if (hex != null) Integer.decode("0x" + hex.replace("#", "")) else Int.MIN_VALUE
		val colorComponents = if (rgb != Int.MIN_VALUE) floatArrayOf(((rgb shr 16) and 0xFF) / 255f, ((rgb shr 8) and 0xFF) / 255f, (rgb and 0xFF) / 255f) else FloatArray(0)

		val group = WAYPOINTS.computeIfAbsent(groupName) { name: String -> OrderedWaypointGroup(name, true, ObjectArrayList()) }
		val waypoint = OrderedWaypoint(pos, colorComponents)

		if (index != Int.MIN_VALUE) {
			val indexToAddAt = Math.clamp(index.toLong(), 0, group.waypoints.size)

			group.waypoints.add(indexToAddAt, waypoint)
			INDEX_STORE.removeInt(group.name)
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.addAt.success", group.name, indexToAddAt)))
		} else {
			group.waypoints.add(waypoint)
			INDEX_STORE.removeInt(group.name)
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.add.success", group.name, pos.toShortString())))
		}

		SEMAPHORE.release()

		return Command.SINGLE_SUCCESS
	}

	private fun removeWaypointGroup(source: FabricClientCommandSource, groupName: String): Int {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly()
			WAYPOINTS.remove(groupName)
			INDEX_STORE.removeInt(groupName)
			SEMAPHORE.release()
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.removeGroup.success", groupName)))
		} else {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)))
		}

		return Command.SINGLE_SUCCESS
	}

	private fun removeWaypoint(source: FabricClientCommandSource, groupName: String, posArgument: PosArgument?, index: Int): Int {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly()
			val group = WAYPOINTS[groupName]

			if (posArgument != null) {
				val pos = posArgument.toAbsoluteBlockPos(ServerCommandSource(null, source.position, source.rotation, null, 0, null, null, null, null))

				group!!.waypoints.removeIf { waypoint: OrderedWaypoint -> waypoint.getPos() == pos }
				INDEX_STORE.removeInt(group.name)
				source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.remove.success", pos.toShortString(), group.name)))
			}

			if (index != Int.MIN_VALUE) {
				val indexToRemove = Math.clamp(index.toLong(), 0, group!!.waypoints.size - 1)

				group.waypoints.removeAt(indexToRemove)
				INDEX_STORE.removeInt(group.name)
				source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.removeAt.success", indexToRemove, group.name)))
			}

			SEMAPHORE.release()
		} else {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)))
		}

		return Command.SINGLE_SUCCESS
	}

	private fun toggleGroup(source: FabricClientCommandSource, groupName: String): Int {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly()
			WAYPOINTS[groupName] = WAYPOINTS[groupName]!!.toggle()
			SEMAPHORE.release()
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.toggle.success", groupName)))
		} else {
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)))
		}

		return Command.SINGLE_SUCCESS
	}

	private fun showAll(source: FabricClientCommandSource): Int {
		source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.showAll")))
		showAll = !showAll

		return Command.SINGLE_SUCCESS
	}

	private fun render(wrc: WorldRenderContext) {
		if ((isInCrystalHollows || isInDwarvenMines) && loaded!!.isDone && SEMAPHORE.tryAcquire()) {
			for ((name1, enabled, waypoints) in WAYPOINTS.values) {
				if (enabled) {
					if (waypoints.isEmpty) continue

					if (!showAll) {
						val player = MinecraftClient.getInstance().player
						var centreIndex = INDEX_STORE.computeIfAbsent(name1, Object2IntFunction { name: Any? -> 0 })

						for (i in waypoints.indices) {
							val waypoint = waypoints[i]

							if (waypoint.getPos().isWithinDistance(player!!.pos, RADIUS.toDouble())) {
								centreIndex = i
								INDEX_STORE.put(name1, i)

								break
							}
						}

						val previousIndex = (centreIndex - 1 + waypoints.size) % waypoints.size
						val currentIndex = (centreIndex + waypoints.size) % waypoints.size
						val nextIndex = (centreIndex + 1) % waypoints.size

						val previous = waypoints[previousIndex]
						val current = waypoints[currentIndex]
						val next = waypoints[nextIndex]

						previous.render(wrc, RelativeIndex.PREVIOUS, previousIndex)
						current.render(wrc, RelativeIndex.CURRENT, currentIndex)
						next.render(wrc, RelativeIndex.NEXT, nextIndex)

						renderLineFromCursor(wrc, Vec3d.ofCenter(next.getPos().up()), LIGHT_GRAY, 1f, 5f)
					} else {
						for (i in waypoints.indices) {
							//Render them as white by default
							waypoints[i].render(wrc, RelativeIndex.CURRENT, i)
						}
					}
				}
			}

			SEMAPHORE.release()
		}
	}

	private fun load() {
		loaded = CompletableFuture.runAsync {
			try {
				Files.newBufferedReader(PATH).use { reader ->
					WAYPOINTS.putAll(SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow())
				}
			} catch (ignored: NoSuchFileException) {
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker Ordered Waypoints] Failed to load the waypoints! :(", e)
			}
		}
	}

	private fun save() {
		try {
			Files.newBufferedWriter(PATH).use { writer ->
				SkyblockerMod.GSON.toJson(SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, WAYPOINTS).getOrThrow(), writer)
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to save the waypoints! :(", e)
		}
	}

	private fun export(source: FabricClientCommandSource): Int {
		try {
			val json = Gson().toJson(SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, WAYPOINTS).getOrThrow())
			val out = ByteArrayOutputStream()
			val gzip = GZIPOutputStream(out)

			gzip.write(json.toByteArray())
			gzip.close()

			val encoded = String(Base64.getEncoder().encode(out.toByteArray()))
			val exportCode = PREFIX + encoded

			MinecraftClient.getInstance().keyboard.clipboard = exportCode
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.export.success")))
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to export waypoints!", e)
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.export.fail")))
		}

		return Command.SINGLE_SUCCESS
	}

	//TODO in future handle for when the group names clash?
	private fun fromSkyblockerFormat(source: FabricClientCommandSource): Int {
		try {
			val importCode = MinecraftClient.getInstance().keyboard.clipboard

			if (importCode.startsWith(PREFIX)) {
				val encoded = importCode.replace(PREFIX, "")
				val decoded = Base64.getDecoder().decode(encoded)

				val json = String(GZIPInputStream(ByteArrayInputStream(decoded)).readAllBytes())
				val importedWaypoints = SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow()

				SEMAPHORE.acquireUninterruptibly()
				WAYPOINTS.putAll(importedWaypoints)
				source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.success")))
				SEMAPHORE.release()
			} else {
				source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.unknownFormatHeader")))
			}
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to import waypoints!", e)
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.fail")))
		}

		return Command.SINGLE_SUCCESS
	}

	private fun fromColeWeightFormat(source: FabricClientCommandSource, groupName: String): Int {
		try {
			if (WAYPOINTS.containsKey(groupName)) {
				source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.groupAlreadyExists", groupName)))

				return Command.SINGLE_SUCCESS
			}

			val json = MinecraftClient.getInstance().keyboard.clipboard
			val coleWeightWaypoints = ColeWeightWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow()
			val convertedWaypoints = ObjectArrayList<OrderedWaypoint>()

			for ((x, y, z) in coleWeightWaypoints) {
				if (x.isPresent && y.isPresent && z.isPresent) {
					//I think Cole Weight ignores the colors and overrides them so we will comment this out
					//float[] colorComponents = (waypoint.r().isPresent() && waypoint.g().isPresent() && waypoint.b().isPresent()) ? new float[] { waypoint.r().get() / 255f, waypoint.g().get() / 255f, waypoint.b().get() / 255f } : new float[0];

					convertedWaypoints.add(OrderedWaypoint(BlockPos(x.get(), y.get(), z.get()), FloatArray(0)))
				}
			}

			SEMAPHORE.acquireUninterruptibly()
			WAYPOINTS[groupName] = OrderedWaypointGroup(groupName, true, convertedWaypoints)
			source.sendFeedback(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.success")))
			SEMAPHORE.release()
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to import waypoints from the Cole Weight format!", e)
			source.sendError(Constants.PREFIX.append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.fail")))
		}

		return Command.SINGLE_SUCCESS
	}

	@JvmRecord
	private data class OrderedWaypointGroup(val name: String, val enabled: Boolean, val waypoints: ObjectArrayList<OrderedWaypoint>) {
		fun toggle(): OrderedWaypointGroup {
			return OrderedWaypointGroup(name, !enabled, waypoints)
		}

		companion object {
			val CODEC: Codec<OrderedWaypointGroup> = RecordCodecBuilder.create<OrderedWaypointGroup> { instance: RecordCodecBuilder.Instance<OrderedWaypointGroup> ->
				instance.group<String, Boolean, ObjectArrayList<OrderedWaypoint>>(
					Codec.STRING.fieldOf("name").forGetter<OrderedWaypointGroup>(OrderedWaypointGroup::name),
					Codec.BOOL.fieldOf("enabled").forGetter<OrderedWaypointGroup>(OrderedWaypointGroup::enabled),
					OrderedWaypoint.LIST_CODEC.fieldOf("waypoints").xmap<ObjectArrayList<OrderedWaypoint?>>(Function<List<OrderedWaypoint>, ObjectArrayList<OrderedWaypoint?>> { c: List<OrderedWaypoint>? -> ObjectArrayList(c) }, Function<ObjectArrayList<OrderedWaypoint?>, List<OrderedWaypoint>> { l: ObjectArrayList<OrderedWaypoint?>? -> ObjectArrayList(l) }).forGetter<OrderedWaypointGroup>(OrderedWaypointGroup::waypoints)
				)
					.apply<OrderedWaypointGroup>(instance) { name: String, enabled: Boolean, waypoints: ObjectArrayList<OrderedWaypoint> -> OrderedWaypointGroup(name, enabled, waypoints) }
			}
		}
	}

	private class OrderedWaypoint(pos: BlockPos?, colorComponents: FloatArray?) : Waypoint(pos, Type.WAYPOINT, colorComponents) {
		private var relativeIndex: RelativeIndex? = null
		private var waypointIndex = 0

		override val pos: BlockPos?
			get() = this.pos

		override val colorComponents: FloatArray?
			get() {
				if (colorComponents!!.size != 3) {
					return when (this.relativeIndex) {
						RelativeIndex.PREVIOUS -> RED
						RelativeIndex.CURRENT -> WHITE
						RelativeIndex.NEXT -> GREEN
					}
				}

				return this.colorComponents
			}

		fun render(context: WorldRenderContext, relativeIndex: RelativeIndex, waypointIndex: Int) {
			this.relativeIndex = relativeIndex
			this.waypointIndex = waypointIndex

			render(context)
		}

		override fun render(context: WorldRenderContext) {
			super.render(context)
			renderText(context, Text.of(waypointIndex.toString()), Vec3d.ofCenter(pos!!.up(2)), true)
		}

		companion object {
			val CODEC: Codec<OrderedWaypoint> = RecordCodecBuilder.create<OrderedWaypoint> { instance: RecordCodecBuilder.Instance<OrderedWaypoint> ->
				instance.group<BlockPos, FloatArray>(
					BlockPos.CODEC.fieldOf("pos").forGetter<OrderedWaypoint> { obj: OrderedWaypoint -> obj.pos },
					Codec.floatRange(0f, 1f).listOf().xmap<FloatArray?>(Function<List<Float?>, FloatArray?> { collection: List<Float?>? -> Floats.toArray(collection) }, Function<FloatArray?, List<Float>> { a: FloatArray? -> FloatArrayList(a) }).optionalFieldOf("colorComponents", FloatArray(0)).forGetter<OrderedWaypoint> { inst: OrderedWaypoint -> if (inst.colorComponents!!.size == 3) inst.colorComponents else FloatArray(0) })
					.apply<OrderedWaypoint>(instance) { pos: BlockPos?, colorComponents: FloatArray? -> OrderedWaypoint(pos, colorComponents) }
			}
			val LIST_CODEC: Codec<List<OrderedWaypoint>> = CODEC.listOf()
			val RED: FloatArray = floatArrayOf(1f, 0f, 0f)
			val WHITE: FloatArray = floatArrayOf(1f, 1f, 1f)
			val GREEN: FloatArray = floatArrayOf(0f, 1f, 0f)
		}
	}

	@JvmRecord
	private data class ColeWeightWaypoint(val x: Optional<Int>, val y: Optional<Int>, val z: Optional<Int>, val r: Optional<Int>, val g: Optional<Int>, val b: Optional<Int>, val options: Optional<Options>) {
		//Even though we don't import the name this is still here incase that eventually changes
		@JvmRecord
		data class Options(val name: Optional<String>) {
			companion object {
				val CODEC: Codec<Options?> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<Options?> ->
					instance.group(
						Codec.STRING.optionalFieldOf("name").forGetter(Options::name)
					)
						.apply(instance) { name: Optional<String> -> Options(name) }
				}
			}
		}

		companion object {
			val CODEC: Codec<ColeWeightWaypoint> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ColeWeightWaypoint> ->
				instance.group<Optional<Int>, Optional<Int>, Optional<Int>, Optional<Int>, Optional<Int>, Optional<Int>, Optional<Options>>(
					Codec.INT.optionalFieldOf("x").forGetter(ColeWeightWaypoint::x),
					Codec.INT.optionalFieldOf("y").forGetter(ColeWeightWaypoint::y),
					Codec.INT.optionalFieldOf("z").forGetter(ColeWeightWaypoint::z),
					Codec.INT.optionalFieldOf("r").forGetter(ColeWeightWaypoint::r),
					Codec.INT.optionalFieldOf("g").forGetter(ColeWeightWaypoint::g),
					Codec.INT.optionalFieldOf("b").forGetter(ColeWeightWaypoint::b),
					Options.CODEC.optionalFieldOf("options").forGetter(ColeWeightWaypoint::options)
				)
					.apply(instance) { x: Optional<Int>, y: Optional<Int>, z: Optional<Int>, r: Optional<Int>, g: Optional<Int>, b: Optional<Int>, options: Optional<Options> -> ColeWeightWaypoint(x, y, z, r, g, b, options) }
			}
			val LIST_CODEC: Codec<List<ColeWeightWaypoint>> = CODEC.listOf()
		}
	}

	private enum class RelativeIndex {
		PREVIOUS,
		CURRENT,
		NEXT
	}
}
