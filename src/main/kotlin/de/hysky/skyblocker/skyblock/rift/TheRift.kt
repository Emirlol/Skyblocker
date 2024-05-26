package de.hysky.skyblocker.skyblock.rift

import com.mojang.brigadier.CommandDispatcher
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.scheduler.Scheduler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text

object TheRift {
	fun init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> MirrorverseWaypoints.render() })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> EffigyWaypoints.render() })
		WorldRenderEvents.AFTER_TRANSLUCENT.register(AfterTranslucent { obj: WorldRenderContext? -> EnigmaSouls.render() })
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { obj: MinecraftClient? -> MirrorverseWaypoints.load() })
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { obj: MinecraftClient? -> EnigmaSouls.load() })
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { obj: MinecraftClient? -> EnigmaSouls.save() })
		ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { obj: Text?, text: Boolean -> EnigmaSouls.onMessage(text) })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> EnigmaSouls.registerCommands(dispatcher) })
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: EffigyWaypoints? -> EffigyWaypoints.updateEffigies() }, SkyblockerConfigManager.get().slayers.vampireSlayer.effigyUpdateFrequency)
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: TwinClawsIndicator? -> TwinClawsIndicator.updateIce() }, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency)
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: ManiaIndicator? -> ManiaIndicator.updateMania() }, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency)
		Scheduler.INSTANCE.scheduleCyclic(Runnable { obj: StakeIndicator? -> StakeIndicator.updateStake() }, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency)
	}
}
