package de.hysky.skyblocker.skyblock.dungeon.puzzle

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.events.DungeonEvents
import de.hysky.skyblocker.events.DungeonEvents.RoomMatched
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.Resettable
import de.hysky.skyblocker.utils.Tickable
import de.hysky.skyblocker.utils.render.Renderable
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.command.CommandRegistryAccess

abstract class DungeonPuzzle(@JvmField protected val puzzleName: String, private val roomNames: Set<String?>) : Tickable, Renderable, Resettable {
	private var shouldSolve = false

	constructor(puzzleName: String, vararg roomName: String?) : this(puzzleName, java.util.Set.of<String?>(*roomName))

	init {
		DungeonEvents.PUZZLE_MATCHED.register(RoomMatched { room: Room ->
			if (roomNames.contains(room.name)) {
				room.addSubProcess(this)
				shouldSolve = true
			}
		})
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("dungeons").then(ClientCommandManager.literal("puzzle").then(ClientCommandManager.literal(puzzleName).then(ClientCommandManager.literal("solve").executes { context: CommandContext<FabricClientCommandSource> ->
				val currentRoom = DungeonManager.getCurrentRoom()
				if (currentRoom != null) {
					reset()
					currentRoom.addSubProcess(this)
					context.source.sendFeedback(Constants.PREFIX.get().append("§aSolving $puzzleName puzzle in the current room."))
				} else {
					context.source.sendError(Constants.PREFIX.get().append("§cCurrent room is null."))
				}
				Command.SINGLE_SUCCESS
			})))))
		})
		ClientPlayConnectionEvents.JOIN.register(this)
	}

	fun shouldSolve(): Boolean {
		return shouldSolve
	}

	override fun reset() {
		shouldSolve = false
	}
}
