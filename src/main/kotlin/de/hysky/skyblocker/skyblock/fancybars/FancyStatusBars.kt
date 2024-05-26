package de.hysky.skyblocker.skyblock.fancybars

import com.google.gson.JsonObject
import com.mojang.brigadier.CommandDispatcher
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig.OldBarPosition
import de.hysky.skyblocker.skyblock.StatusBarTracker
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarAnchor
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.scheduler.Scheduler.Companion.queueOpenScreenCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class FancyStatusBars {
	private val client: MinecraftClient = MinecraftClient.getInstance()
	private val statusBarTracker: StatusBarTracker = SkyblockerMod.getInstance().statusBarTracker

	fun render(context: DrawContext, scaledWidth: Int, scaledHeight: Int): Boolean {
		val player = client.player
		if (!isEnabled || player == null) return false

		val barCollection: Collection<StatusBar> = statusBars.values
		for (statusBar in barCollection) {
			if (statusBar.anchor != null) statusBar.render(context, -1, -1, client.lastFrameDuration)
		}
		for (statusBar in barCollection) {
			if (statusBar.anchor != null && statusBar.showText()) statusBar.renderText(context)
		}
		val health = statusBarTracker.health
		statusBars["health"]!!.updateValues(health.value / health.max.toFloat(), health.overflow / health.max.toFloat(), health.value)

		val intelligence = statusBarTracker.mana
		statusBars["intelligence"]!!.updateValues(intelligence.value / intelligence.max.toFloat(), intelligence.overflow / intelligence.max.toFloat(), intelligence.value)
		val defense = statusBarTracker.defense
		statusBars["defense"]!!.updateValues(defense / (defense + 100f), 0f, defense)
		statusBars["experience"]!!.updateValues(player.experienceProgress, 0f, player.experienceLevel)
		return true
	}

	companion object {
		private val FILE: Path = SkyblockerMod.CONFIG_DIR.resolve("status_bars.json")
		private val LOGGER: Logger = LoggerFactory.getLogger(FancyStatusBars::class.java)

		var barPositioner: BarPositioner = BarPositioner()
		var statusBars: MutableMap<String, StatusBar> = HashMap()

		@JvmStatic
        val isHealthFancyBarVisible: Boolean
			get() {
				val health = statusBars["health"]
				return health!!.anchor != null || health.inMouse
			}

		@JvmStatic
        val isExperienceFancyBarVisible: Boolean
			get() {
				val experience = statusBars["experience"]
				return experience!!.anchor != null || experience.inMouse
			}

		fun init() {
			statusBars["health"] = StatusBar(
				Identifier(SkyblockerMod.NAMESPACE, "bars/icons/health"),
				arrayOf<Color?>(Color(255, 0, 0), Color(255, 220, 0)),
				true, Color(255, 85, 85), Text.translatable("skyblocker.bars.config.health")
			)
			statusBars["intelligence"] = StatusBar(
				Identifier(SkyblockerMod.NAMESPACE, "bars/icons/intelligence"),
				arrayOf<Color?>(Color(0, 255, 255), Color(180, 0, 255)),
				true, Color(85, 255, 255), Text.translatable("skyblocker.bars.config.intelligence")
			)
			statusBars["defense"] = StatusBar(
				Identifier(SkyblockerMod.NAMESPACE, "bars/icons/defense"),
				arrayOf<Color?>(Color(255, 255, 255)),
				false, Color(185, 185, 185), Text.translatable("skyblocker.bars.config.defense")
			)
			statusBars["experience"] = StatusBar(
				Identifier(SkyblockerMod.NAMESPACE, "bars/icons/experience"),
				arrayOf<Color?>(Color(100, 230, 70)),
				false, Color(128, 255, 32), Text.translatable("skyblocker.bars.config.experience")
			)

			// Fetch from old status bar config
			val counts = IntArray(3) // counts for RIGHT, LAYER1, LAYER2
			val health = statusBars["health"]
			val barPositions = SkyblockerConfigManager.get().uiAndVisuals.bars.barPositions
			loadOldBarPosition(health, counts, barPositions.healthBarPosition)
			val intelligence = statusBars["intelligence"]
			loadOldBarPosition(intelligence, counts, barPositions.manaBarPosition)
			val defense = statusBars["defense"]
			loadOldBarPosition(defense, counts, barPositions.defenceBarPosition)
			val experience = statusBars["experience"]
			loadOldBarPosition(experience, counts, barPositions.experienceBarPosition)

			CompletableFuture.supplyAsync { loadBarConfig() }.thenAccept { `object`: JsonObject? ->
				if (`object` != null) {
					for (s in `object`.keySet()) {
						if (statusBars.containsKey(s)) {
							try {
								statusBars[s]!!.loadFromJson(`object`[s].asJsonObject)
							} catch (e: Exception) {
								LOGGER.error("[Skyblocker] Failed to load {} status bar", s, e)
							}
						} else {
							LOGGER.warn("[Skyblocker] Unknown status bar: {}", s)
						}
					}
				}
				placeBarsInPositioner()
				configLoaded = true
			}.exceptionally { throwable: Throwable? ->
				LOGGER.error("[Skyblocker] Failed reading status bars config", throwable)
				null
			}
			ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { client: MinecraftClient? ->
				saveBarConfig()
				GLFW.glfwDestroyCursor(StatusBarsConfigScreen.Companion.RESIZE_CURSOR)
			})

			ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
				dispatcher.register(
					ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("bars").executes(queueOpenScreenCommand { StatusBarsConfigScreen() }))
				)
			})
		}

		/**
		 * Loads the bar position from the old config
		 * @param bar the bar to load the position for
		 * @param counts the counts for each bar position (LAYER1, LAYER2, RIGHT)
		 * @param position the position to load
		 */
		private fun loadOldBarPosition(bar: StatusBar?, counts: IntArray, position: OldBarPosition) {
			when (position) {
				OldBarPosition.RIGHT -> {
					bar!!.anchor = BarAnchor.HOTBAR_RIGHT
					bar.gridY = 0
					bar.gridX = counts[position.ordinal]++
				}

				OldBarPosition.LAYER1 -> {
					bar!!.anchor = BarAnchor.HOTBAR_TOP
					bar.gridY = 0
					bar.gridX = counts[position.ordinal]++
				}

				OldBarPosition.LAYER2 -> {
					bar!!.anchor = BarAnchor.HOTBAR_TOP
					bar.gridY = 1
					bar.gridX = counts[position.ordinal]++
				}
			}
		}

		private var configLoaded = false

		private fun placeBarsInPositioner() {
			val original = statusBars.values.stream().toList()

			for (barAnchor in BarAnchor.Companion.allAnchors()) {
				val barList: List<StatusBar> = ArrayList(original.stream().filter { bar: StatusBar -> bar.anchor == barAnchor }.toList())
				if (barList.isEmpty()) continue
				barList.sort(java.util.Comparator { a: StatusBar, b: StatusBar -> if (a.gridY == b.gridY) Integer.compare(a.gridX, b.gridX) else Integer.compare(a.gridY, b.gridY) })

				var y = -1
				var rowNum = -1
				for (statusBar in barList) {
					if (statusBar.gridY > y) {
						barPositioner.addRow(barAnchor)
						rowNum++
						y = statusBar.gridY
					}
					barPositioner.addBar(barAnchor, rowNum, statusBar)
				}
			}
		}

		fun loadBarConfig(): JsonObject? {
			try {
				Files.newBufferedReader(FILE).use { reader ->
					return SkyblockerMod.GSON.fromJson(reader, JsonObject::class.java)
				}
			} catch (e: NoSuchFileException) {
				LOGGER.warn("[Skyblocker] No status bar config file found, using defaults")
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker] Failed to load status bars config", e)
			}
			return null
		}

		fun saveBarConfig() {
			val output = JsonObject()
			statusBars.forEach { (s: String?, statusBar: StatusBar) -> output.add(s, statusBar.toJson()) }
			try {
				Files.newBufferedWriter(FILE).use { writer ->
					SkyblockerMod.GSON.toJson(output, writer)
					LOGGER.info("[Skyblocker] Saved status bars config")
				}
			} catch (e: IOException) {
				LOGGER.error("[Skyblocker] Failed to save status bars config", e)
			}
		}

		@JvmStatic
        fun updatePositions() {
			if (!configLoaded) return
			val width = MinecraftClient.getInstance().window.scaledWidth
			val height = MinecraftClient.getInstance().window.scaledHeight

			for (barAnchor in BarAnchor.Companion.allAnchors()) {
				val anchorPosition = barAnchor.getAnchorPosition(width, height)
				val sizeRule = barAnchor.sizeRule

				var targetSize = sizeRule!!.targetSize
				val visibleHealthMove = barAnchor == BarAnchor.HOTBAR_TOP && !isHealthFancyBarVisible
				if (visibleHealthMove) {
					targetSize /= 2
				}

				if (sizeRule!!.isTargetSize) {
					for (row in 0 until barPositioner.getRowCount(barAnchor)) {
						val barRow = barPositioner.getRow(barAnchor, row)
						if (barRow!!.isEmpty()) continue

						// FIX SIZES
						var totalSize = 0
						for (statusBar in barRow) totalSize += (Math.clamp(statusBar!!.size.toLong(), sizeRule!!.minSize, sizeRule!!.maxSize).also { statusBar.size = it })

						whileLoop@ while (totalSize != targetSize) {
							if (totalSize > targetSize) {
								for (statusBar in barRow) {
									if (statusBar!!.size > sizeRule!!.minSize) {
										statusBar.size--
										totalSize--
										if (totalSize == targetSize) break@whileLoop
									}
								}
							} else {
								for (statusBar in barRow) {
									if (statusBar!!.size < sizeRule!!.maxSize) {
										statusBar.size++
										totalSize++
										if (totalSize == targetSize) break@whileLoop
									}
								}
							}
						}
					}
				}

				for (row in 0 until barPositioner.getRowCount(barAnchor)) {
					val barRow: List<StatusBar?>? = barPositioner.getRow(barAnchor, row)
					if (barRow!!.isEmpty()) continue


					// Update the positions
					var widthPerSize: Float = if (sizeRule!!.isTargetSize) sizeRule!!.totalWidth.toFloat() / targetSize
					else sizeRule!!.widthPerSize.toFloat()

					if (visibleHealthMove) widthPerSize /= 2f

					var currSize = 0
					val rowSize = barRow.size
					for (i in 0 until rowSize) {
						// A bit of a padding
						var offsetX = 0
						var lessWidth = 0
						if (rowSize > 1) { // Technically bars in the middle of 3+ bars will be smaller than the 2 side ones but shh
							if (i == 0) lessWidth = 1
							else if (i == rowSize - 1) {
								lessWidth = 1
								offsetX = 1
							} else {
								lessWidth = 2
								offsetX = 1
							}
						}
						val statusBar = barRow[i]
						statusBar!!.size = Math.clamp(statusBar.size.toLong(), sizeRule!!.minSize, sizeRule!!.maxSize)

						val x = if (barAnchor.isRight) anchorPosition!!.x() + (if (visibleHealthMove) sizeRule!!.totalWidth / 2f else 0f) + currSize * widthPerSize else anchorPosition!!.x() - currSize * widthPerSize - statusBar.size * widthPerSize
						statusBar.x = MathHelper.ceil(x) + offsetX

						val y = if (barAnchor.isUp) anchorPosition!!.y() - (row + 1) * (statusBar.height + 1) else anchorPosition!!.y() + row * (statusBar.height + 1)
						statusBar.y = y

						statusBar.width = MathHelper.floor(statusBar.size * widthPerSize) - lessWidth
						currSize += statusBar.size
						statusBar.gridX = i
						statusBar.gridY = row
					}
				}
			}
		}

		@JvmStatic
        val isEnabled: Boolean
			get() = SkyblockerConfigManager.get().uiAndVisuals.bars.enableBars && !isInTheRift
	}
}
