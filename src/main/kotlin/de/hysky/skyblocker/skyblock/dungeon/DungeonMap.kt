package de.hysky.skyblocker.skyblock.dungeon

import com.mojang.brigadier.CommandDispatcher
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.HudRenderEvents
import de.hysky.skyblocker.events.HudRenderEvents.HudRenderStage
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.scheduler.Scheduler.Companion.queueOpenScreenCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.MapIdComponent
import net.minecraft.item.FilledMapItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object DungeonMap {
	private val DEFAULT_MAP_ID_COMPONENT = MapIdComponent(1024)
	private var cachedMapIdComponent: MapIdComponent? = null

	fun init() {
		HudRenderEvents.AFTER_MAIN_HUD.register(HudRenderStage { context: DrawContext, tickDelta: Float -> render(context) })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
			dispatcher.register(
				ClientCommandManager.literal("skyblocker")
					.then(
						ClientCommandManager.literal("hud")
							.then(
								ClientCommandManager.literal("dungeon")
									.executes(queueOpenScreenCommand { DungeonMapConfigScreen() })
							)
					)
			)
		})
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler: ClientPlayNetworkHandler?, sender: PacketSender?, client: MinecraftClient? -> reset() })
	}

	fun render(matrices: MatrixStack) {
		val client = MinecraftClient.getInstance()
		if (client.player == null || client.world == null) return

		val mapId = getMapIdComponent(client.player!!.inventory.main[8])

		val state = FilledMapItem.getMapState(mapId, client.world) ?: return

		val x = SkyblockerConfigManager.get().dungeons.dungeonMap.mapX
		val y = SkyblockerConfigManager.get().dungeons.dungeonMap.mapY
		val scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling
		val vertices = client.bufferBuilders.effectVertexConsumers
		val mapRenderer = client.gameRenderer.mapRenderer

		matrices.push()
		matrices.translate(x.toFloat(), y.toFloat(), 0f)
		matrices.scale(scaling, scaling, 0f)
		mapRenderer.draw(matrices, vertices, mapId, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE)
		vertices.draw()
		matrices.pop()
	}

	@JvmStatic
    fun getMapIdComponent(stack: ItemStack): MapIdComponent? {
		if (stack.isOf(Items.FILLED_MAP) && stack.contains(DataComponentTypes.MAP_ID)) {
			val mapIdComponent = stack.get(DataComponentTypes.MAP_ID)
			cachedMapIdComponent = mapIdComponent
			return mapIdComponent
		} else return if (cachedMapIdComponent != null) cachedMapIdComponent else DEFAULT_MAP_ID_COMPONENT
	}

	private fun render(context: DrawContext) {
		if (isInDungeons && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap) {
			render(context.matrices)
		}
	}

	private fun reset() {
		cachedMapIdComponent = null
	}
}
