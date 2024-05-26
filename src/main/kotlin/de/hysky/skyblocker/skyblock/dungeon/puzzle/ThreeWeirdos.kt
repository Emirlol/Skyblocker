package de.hysky.skyblocker.skyblock.dungeon.puzzle

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room
import de.hysky.skyblocker.utils.render.RenderHelper.renderFilled
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import java.util.function.Consumer
import java.util.regex.Pattern

class ThreeWeirdos private constructor() : DungeonPuzzle("three-weirdos", "three-chests") {
	init {
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message: Text, overlay: Boolean ->
			val world: World? = MinecraftClient.getInstance().world
			if (overlay || !shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveThreeWeirdos || world == null || !DungeonManager.isCurrentRoomMatched()) return@register

			val matcher = PATTERN.matcher(Formatting.strip(message.string))
			if (!matcher.matches()) return@register
			val name = matcher.group(1)
			val room = DungeonManager.getCurrentRoom()

			checkForNPC(world, room, BlockPos(13, 69, 24), name)
			checkForNPC(world, room, BlockPos(15, 69, 25), name)
			checkForNPC(world, room, BlockPos(17, 69, 24), name)
		})
		UseBlockCallback.EVENT.register(UseBlockCallback { player: PlayerEntity?, world: World?, hand: Hand?, blockHitResult: BlockHitResult ->
			if (blockHitResult.type == HitResult.Type.BLOCK && blockHitResult.blockPos == pos) {
				pos = null
			}
			ActionResult.PASS
		})
	}

	private fun checkForNPC(world: World, room: Room, relative: BlockPos, name: String) {
		val npcPos = room.relativeToActual(relative)
		val npcs = world.getEntitiesByClass(
			ArmorStandEntity::class.java,
			Box.enclosing(npcPos, npcPos)
		) { entity: ArmorStandEntity -> entity.name.string == name }
		if (!npcs.isEmpty()) {
			pos = room.relativeToActual(relative.add(1, 0, 0))
			npcs.forEach(Consumer { entity: ArmorStandEntity -> entity.customName = Text.literal(name).formatted(Formatting.GREEN) })
		}
	}

	override fun tick(client: MinecraftClient?) {}

	override fun render(context: WorldRenderContext?) {
		if (shouldSolve() && pos != null) {
			renderFilled(context!!, pos, GREEN_COLOR_COMPONENTS, 0.5f, true)
		}
	}

	override fun reset() {
		super.reset()
		pos = null
	}

	companion object {
		val PATTERN: Pattern = Pattern.compile("^\\[NPC] ([A-Z][a-z]+): (?:The reward is(?: not in my chest!|n't in any of our chests\\.)|My chest (?:doesn't have the reward\\. We are all telling the truth\\.|has the reward and I'm telling the truth!)|At least one of them is lying, and the reward is not in [A-Z][a-z]+'s chest!|Both of them are telling the truth\\. Also, [A-Z][a-z]+ has the reward in their chest!)$")
		private val GREEN_COLOR_COMPONENTS = floatArrayOf(0f, 1f, 0f)
		private var pos: BlockPos? = null

		fun init() {
			ThreeWeirdos()
		}
	}
}
