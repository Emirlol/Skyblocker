package de.hysky.skyblocker.skyblock.dungeon.puzzle

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.render.RenderHelper.renderLinesFromPoints
import de.hysky.skyblocker.utils.render.RenderHelper.renderOutline
import it.unimi.dsi.fastutil.objects.ObjectDoublePair
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Intersectiond
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

class CreeperBeams private constructor() : DungeonPuzzle("creeper", "creeper-room") {
	override fun reset() {
		super.reset()
		beams.clear()
		base = null
	}

	override fun tick(client: MinecraftClient?) {
		// don't do anything if the room is solved

		if (!shouldSolve()) {
			return
		}

		// clear state if not in dungeon
		if (client!!.world == null || client.player == null || !isInDungeons) {
			return
		}

		// try to find base if not found and solve
		if (base == null) {
			base = findCreeperBase(client.player, client.world)
			if (base == null) {
				return
			}
			val creeperPos = Vec3d(base!!.x + 0.5, BASE_Y + 1.75, base!!.z + 0.5)
			val targets = findTargets(client.world, base)
			beams = findLines(creeperPos, targets)
		}

		// update the beam states
		beams.forEach(Consumer { b: Beam -> b.updateState(client.world) })

		// check if the room is solved
		if (!isTarget(client.world, base)) {
			reset()
		}
	}

	override fun render(wrc: WorldRenderContext?) {
		// don't render if solved or disabled

		if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperSolver) {
			return
		}

		// lines.size() is always <= 4 so no issues OOB issues with the colors here.
		for (i in beams.indices) {
			beams[i].render(wrc, COLORS[i])
		}
	}

	// helper class to hold all the things needed to render a beam
	private class Beam(// raw block pos of target
		var blockOne: BlockPos, var blockTwo: BlockPos
	) {
		// middle of targets used for rendering the line
		var line: Array<Vec3d?> = arrayOfNulls(2)

		// boxes used for rendering the block outline
		var outlineOne: Box
		var outlineTwo: Box

		// state: is this beam created/inputted or not?
		private var toDo = true

		init {
			line[0] = Vec3d(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5)
			line[1] = Vec3d(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5)
			outlineOne = Box(a)
			outlineTwo = Box(b)
		}

		// used to filter the list of all beams so that no two beams share a target
		fun containsComponentOf(other: Beam): Boolean {
			return this.blockOne == other.blockOne || (this.blockOne == other.blockTwo) || (this.blockTwo == other.blockOne) || (this.blockTwo == other.blockTwo)
		}

		// update the state: is the beam created or not?
		fun updateState(world: ClientWorld?) {
			toDo = !(world!!.getBlockState(blockOne).block === Blocks.PRISMARINE
					&& world!!.getBlockState(blockTwo).block === Blocks.PRISMARINE)
		}

		// render either in a color if not created or faintly green if created
		fun render(wrc: WorldRenderContext?, color: FloatArray?) {
			if (toDo) {
				renderOutline(wrc!!, outlineOne, color!!, 3f, false)
				renderOutline(wrc, outlineTwo, color, 3f, false)
				renderLinesFromPoints(wrc, line, color, 1f, 2f, false)
			} else {
				renderOutline(wrc!!, outlineOne, GREEN_COLOR_COMPONENTS, 1f, false)
				renderOutline(wrc, outlineTwo, GREEN_COLOR_COMPONENTS, 1f, false)
				renderLinesFromPoints(wrc, line, GREEN_COLOR_COMPONENTS, 0.75f, 1f, false)
			}
		}
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(CreeperBeams::class.java.name)

		private val COLORS = arrayOf(
			DyeColor.LIGHT_BLUE.colorComponents,
			DyeColor.LIME.colorComponents,
			DyeColor.YELLOW.colorComponents,
			DyeColor.MAGENTA.colorComponents,
			DyeColor.PINK.colorComponents,
		)
		private val GREEN_COLOR_COMPONENTS: FloatArray = DyeColor.GREEN.colorComponents

		private const val FLOOR_Y = 68
		private const val BASE_Y = 74

		@Suppress("unused")
		private val INSTANCE = CreeperBeams()

		private var beams = ArrayList<Beam>()
		private var base: BlockPos? = null

		fun init() {
		}

		// find the sea lantern block beneath the creeper
		private fun findCreeperBase(player: ClientPlayerEntity?, world: ClientWorld?): BlockPos? {
			// find all creepers

			val creepers = world!!.getEntitiesByClass(
				CreeperEntity::class.java,
				player!!.boundingBox.expand(50.0),
				EntityPredicates.VALID_ENTITY
			)

			if (creepers.isEmpty()) {
				return null
			}

			// (sanity) check:
			// if the creeper isn't above a sea lantern, it's not the target.
			for (ce in creepers) {
				val creeperPos = ce.pos
				val potentialBase = BlockPos.ofFloored(creeperPos.x, BASE_Y.toDouble(), creeperPos.z)
				if (isTarget(world, potentialBase)) {
					return potentialBase
				}
			}

			return null
		}

		// find the sea lanterns (and the ONE prismarine ty hypixel) in the room
		private fun findTargets(world: ClientWorld?, basePos: BlockPos?): ArrayList<BlockPos> {
			val targets = ArrayList<BlockPos>()

			val start = BlockPos(basePos!!.x - 15, BASE_Y + 12, basePos.z - 15)
			val end = BlockPos(basePos.x + 16, FLOOR_Y, basePos.z + 16)

			for (pos in BlockPos.iterate(start, end)) {
				if (isTarget(world, pos)) {
					targets.add(BlockPos(pos))
				}
			}
			return targets
		}

		// generate lines between targets and finally find the solution
		private fun findLines(creeperPos: Vec3d, targets: ArrayList<BlockPos>): ArrayList<Beam> {
			val allLines = ArrayList<ObjectDoublePair<Beam>>()

			// optimize this a little bit by
			// only generating lines "one way", i.e. 1 -> 2 but not 2 -> 1
			for (i in targets.indices) {
				for (j in i + 1 until targets.size) {
					val beam = Beam(targets[i], targets[j])
					val dist = Intersectiond.distancePointLine(
						creeperPos.x, creeperPos.y, creeperPos.z,
						beam.line[0]!!.x, beam.line[0]!!.y, beam.line[0]!!.z,
						beam.line[1]!!.x, beam.line[1]!!.y, beam.line[1]!!.z
					)
					allLines.add(ObjectDoublePair.of(beam, dist))
				}
			}

			// this feels a bit heavy-handed, but it works for now.
			val result = ArrayList<Beam>()
			allLines.sort(Comparator.comparingDouble { obj: ObjectDoublePair<Beam?> -> obj.rightDouble() })

			while (result.size < 5 && !allLines.isEmpty()) {
				val solution = allLines.first.left()
				result.add(solution)

				// remove the line we just added and other lines that use blocks we're using for
				// that line
				allLines.removeFirst()
				allLines.removeIf { beam: ObjectDoublePair<Beam> -> solution.containsComponentOf(beam.left()) }
			}

			if (result.size < 5) {
				LOGGER.error("Not enough solutions found. This is bad...")
			}

			return result
		}

		private fun isTarget(world: ClientWorld?, pos: BlockPos?): Boolean {
			val block = world!!.getBlockState(pos).block
			return block === Blocks.SEA_LANTERN || block === Blocks.PRISMARINE
		}
	}
}
