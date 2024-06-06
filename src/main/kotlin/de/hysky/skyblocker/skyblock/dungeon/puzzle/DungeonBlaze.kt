package de.hysky.skyblocker.skyblock.dungeon.puzzle

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import it.unimi.dsi.fastutil.objects.ObjectIntPair
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.predicate.entity.EntityPredicates
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class provides functionality to render outlines around Blaze entities
 */
class DungeonBlaze private constructor() : DungeonPuzzle("blaze", "blaze-room-1-high", "blaze-room-1-low") {
	/**
	 * Updates the state of Blaze entities and triggers the rendering process if necessary.
	 */
	override fun tick(client: MinecraftClient?) {
		if (!shouldSolve()) {
			return
		}
		if (client!!.world == null || client.player == null || !isInDungeons) return
		val blazes = getBlazesInWorld(client.world, client.player)
		sortBlazes(blazes)
		updateBlazeEntities(blazes)
	}

	/**
	 * Renders outlines for Blaze entities based on health and position.
	 *
	 * @param wrc The WorldRenderContext used for rendering.
	 */
	override fun render(wrc: WorldRenderContext?) {
		try {
			if (highestBlaze != null && lowestBlaze != null && highestBlaze!!.isAlive && lowestBlaze!!.isAlive && SkyblockerConfigManager.config.dungeons.puzzleSolvers.blazeSolver) {
				if (highestBlaze!!.y < 69) {
					renderBlazeOutline(highestBlaze, nextHighestBlaze, wrc)
				}
				if (lowestBlaze!!.y > 69) {
					renderBlazeOutline(lowestBlaze, nextLowestBlaze, wrc)
				}
			}
		} catch (e: Exception) {
			handleException(e)
		}
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(DungeonBlaze::class.java.name)
		private val GREEN_COLOR_COMPONENTS = floatArrayOf(0.0f, 1.0f, 0.0f)
		private val WHITE_COLOR_COMPONENTS = floatArrayOf(1.0f, 1.0f, 1.0f)

		@Suppress("unused")
		private val INSTANCE = DungeonBlaze()

		private var highestBlaze: ArmorStandEntity? = null
		private var lowestBlaze: ArmorStandEntity? = null
		private var nextHighestBlaze: ArmorStandEntity? = null
		private var nextLowestBlaze: ArmorStandEntity? = null

		fun init() {
		}

		/**
		 * Retrieves Blaze entities in the world and parses their health information.
		 *
		 * @param world The client world to search for Blaze entities.
		 * @return A list of Blaze entities and their associated health.
		 */
		private fun getBlazesInWorld(world: ClientWorld?, player: ClientPlayerEntity?): List<ObjectIntPair<ArmorStandEntity>> {
			val blazes: MutableList<ObjectIntPair<ArmorStandEntity>> = ArrayList()
			for (blaze in world!!.getEntitiesByClass<ArmorStandEntity>(ArmorStandEntity::class.java, player!!.boundingBox.expand(500.0), EntityPredicates.NOT_MOUNTED)) {
				val blazeName = blaze.name.string
				if (blazeName.contains("Blaze") && blazeName.contains("/")) {
					try {
						val health = blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length - 1).replace(",".toRegex(), "").toInt()
						blazes.add(ObjectIntPair.of(blaze, health))
					} catch (e: NumberFormatException) {
						handleException(e)
					}
				}
			}
			return blazes
		}

		/**
		 * Sorts the Blaze entities based on their health values.
		 *
		 * @param blazes The list of Blaze entities to be sorted.
		 */
		private fun sortBlazes(blazes: List<ObjectIntPair<ArmorStandEntity>>) {
			blazes.sort(Comparator.comparingInt { obj: ObjectIntPair<ArmorStandEntity?> -> obj.rightInt() })
		}

		/**
		 * Updates information about Blaze entities based on sorted list.
		 *
		 * @param blazes The sorted list of Blaze entities with associated health values.
		 */
		private fun updateBlazeEntities(blazes: List<ObjectIntPair<ArmorStandEntity>>) {
			if (!blazes.isEmpty()) {
				lowestBlaze = blazes.first.left()
				val highestIndex = blazes.size - 1
				highestBlaze = blazes[highestIndex].left()
				if (blazes.size > 1) {
					nextLowestBlaze = blazes[1].left()
					nextHighestBlaze = blazes[highestIndex - 1].left()
				}
			}
		}

		/**
		 * Renders outlines for Blaze entities and connections between them.
		 *
		 * @param blaze     The Blaze entity for which to render an outline.
		 * @param nextBlaze The next Blaze entity for connection rendering.
		 * @param wrc       The WorldRenderContext used for rendering.
		 */
		private fun renderBlazeOutline(blaze: ArmorStandEntity?, nextBlaze: ArmorStandEntity?, wrc: WorldRenderContext?) {
			val blazeBox = blaze!!.boundingBox.expand(0.3, 0.9, 0.3).offset(0.0, -1.1, 0.0)
			renderOutline(wrc!!, blazeBox, GREEN_COLOR_COMPONENTS, 5f, false)

			if (nextBlaze != null && nextBlaze.isAlive && nextBlaze !== blaze) {
				val nextBlazeBox = nextBlaze.boundingBox.expand(0.3, 0.9, 0.3).offset(0.0, -1.1, 0.0)
				renderOutline(wrc, nextBlazeBox, WHITE_COLOR_COMPONENTS, 5f, false)

				val blazeCenter = blazeBox.center
				val nextBlazeCenter = nextBlazeBox.center

				renderLinesFromPoints(wrc, arrayOf(blazeCenter, nextBlazeCenter), WHITE_COLOR_COMPONENTS, 1f, 5f, false)
			}
		}

		/**
		 * Handles exceptions by logging and printing stack traces.
		 *
		 * @param e The exception to handle.
		 */
		private fun handleException(e: Exception) {
			LOGGER.error("[Skyblocker BlazeRenderer] Encountered an unknown exception", e)
		}
	}
}
