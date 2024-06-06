package de.hysky.skyblocker

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.hysky.skyblocker.config.ImageRepoLoader
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer
import de.hysky.skyblocker.debug.Debug
import de.hysky.skyblocker.skyblock.*
import de.hysky.skyblocker.skyblock.calculators.CalculatorCommand
import de.hysky.skyblocker.skyblock.chat.ChatRuleAnnouncementScreen
import de.hysky.skyblocker.skyblock.chat.ChatRulesHandler
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder
import de.hysky.skyblocker.skyblock.chocolatefactory.TimeTowerReminder
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra
import de.hysky.skyblocker.skyblock.dungeon.*
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen
import de.hysky.skyblocker.skyblock.dungeon.puzzle.*
import de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder.Boulder
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretsTracker
import de.hysky.skyblocker.skyblock.dwarven.*
import de.hysky.skyblocker.skyblock.end.BeaconHighlighter
import de.hysky.skyblocker.skyblock.end.EnderNodes
import de.hysky.skyblocker.skyblock.end.TheEnd
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars
import de.hysky.skyblocker.skyblock.garden.FarmingHud
import de.hysky.skyblocker.skyblock.garden.LowerSensitivity
import de.hysky.skyblocker.skyblock.garden.VisitorHelper
import de.hysky.skyblocker.skyblock.item.*
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipManager
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.skyblock.rift.TheRift
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager
import de.hysky.skyblocker.skyblock.shortcut.Shortcuts
import de.hysky.skyblocker.skyblock.special.SpecialEffects
import de.hysky.skyblocker.skyblock.tabhud.TabHud
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr
import de.hysky.skyblocker.skyblock.waypoint.FairySouls
import de.hysky.skyblocker.skyblock.waypoint.MythologicalRitual
import de.hysky.skyblocker.skyblock.waypoint.OrderedWaypoints
import de.hysky.skyblocker.skyblock.waypoint.Relics
import de.hysky.skyblocker.utils.*
import de.hysky.skyblocker.utils.chat.ChatMessageListener
import de.hysky.skyblocker.utils.discord.DiscordRPCManager
import de.hysky.skyblocker.utils.render.RenderHelper
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling
import de.hysky.skyblocker.utils.render.gui.ContainerSolverManager
import de.hysky.skyblocker.utils.render.title.TitleContainer
import de.hysky.skyblocker.utils.scheduler.Scheduler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.client.MinecraftClient
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

/**
 * Main class for Skyblocker which initializes features, registers events, and
 * manages ticks.
 */
object SkyblockerMod {
	const val NAMESPACE = "skyblocker"
	val SKYBLOCKER_MOD: ModContainer = FabricLoader.getInstance().getModContainer(NAMESPACE).getOrNull() ?: error("Mod container not found")
	val VERSION: String = SKYBLOCKER_MOD.metadata.version.friendlyString
	val CONFIG_DIR: Path = FabricLoader.getInstance().configDir.resolve(NAMESPACE)
	public val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
	val GSON_COMPACT: Gson = GsonBuilder().create()
	val statusBarTracker = StatusBarTracker()
	val globalJob = MainScope() + CoroutineName("Skyblocker")

	/**
	 * Do not call this method. It will be called by fabric loader.
	 *
	 * Register [tick] to
	 * [ClientTickEvents.END_CLIENT_TICK], initialize all features, and
	 * schedule tick events.
	 */
	fun onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register { client -> this.tick(client) }
		ConfigDataFixer.apply()
		Utils.init()
		SkyblockerConfigManager.init()
		SkyblockerScreen.Companion //Object declarations are initialized lazily, so we need to access it here to initialize it. Same goes for any other object below.
		Tips.init()
		NEURepoManager.init()
		ImageRepoLoader.init()
		ItemRepository.init()
		PlayerHeadHashCache.init()
		HotbarSlotLock
		ItemTooltip.init()
		AccessoriesHelper.init()
		WikiLookup
		FairySouls.init()
		Relics.init()
		MythologicalRitual.init()
		EnderNodes.init()
		OrderedWaypoints.init()
		BackpackPreview.init()
		ItemCooldowns
		TabHud.init()
		GlaciteColdOverlay.init()
		DwarvenHud.init()
		CommissionLabels
		CrystalsHud
		FarmingHud.init()
		LowerSensitivity.init()
		CrystalsLocationsManager.init()
		MetalDetector
		ChatMessageListener.init()
		Shortcuts.init()
		ChatRulesHandler.init()
		ChatRuleAnnouncementScreen.init()
		CalculatorCommand.init()
		DiscordRPCManager.init()
		LividColor.init()
		FishingHelper.init()
		DungeonMap.init()
		DungeonScoreHUD
		DungeonManager.init()
		DungeonBlaze.init()
		Waterboard.init()
		Silverfish.init()
		IceFill.init()
		DungeonScore.init()
		PartyFinderScreen.initClass()
		ChestValue.init()
		FireFreezeStaffTimer
		GuardianHealth.init()
		TheRift.init()
		TheEnd.init()
		SearchOverManager.init()
		TitleContainer.init()
		ScreenMaster.init()
		DungeonTextures
		OcclusionCulling
		TeleportOverlay.init()
		CustomItemNames.init()
		CustomArmorDyeColors.init()
		CustomArmorAnimatedDyes.init()
		CustomArmorTrims.init()
		TicTacToe.init()
		QuiverWarning.init()
		SpecialEffects.init()
		ItemProtection.init()
		CreeperBeams.init()
		Boulder.init()
		ThreeWeirdos.init()
		VisitorHelper.init()
		ItemRarityBackgrounds.init()
		MuseumItemCache.init()
		SecretsTracker.init()
		ApiUtils.init()
		ProfileUtils.init()
		Debug.init()
		Kuudra.init()
		RenderHelper.init()
		FancyStatusBars.init()
		ContainerSolverManager
		statusBarTracker.init()
		BeaconHighlighter.init()
		WarpAutocomplete
		MobBoundingBoxes.init()
		EggFinder.init()
		TimeTowerReminder.init()
		SkyblockTime
		TooltipManager.init();
		SlotTextManager.init();

		Scheduler.scheduleCyclic(20) { Utils.update() }
		Scheduler.scheduleCyclic(200) { DiscordRPCManager.updateDataAndPresence() }
		Scheduler.scheduleCyclic(10) { LividColor.update() }
		Scheduler.scheduleCyclic(50) { BackpackPreview.tick() }
		Scheduler.scheduleCyclic(40) { DwarvenHud.update() }
		Scheduler.scheduleCyclic(40) { CrystalsHud.update() }
		Scheduler.scheduleCyclic(20) { PlayerListMgr.updateList() }

		ClientLifecycleEvents.CLIENT_STOPPING.register {
			globalJob.cancel()
		}
	}

	/**
	 * Ticks the scheduler. Called once at the end of every client tick through
	 * [ClientTickEvents.END_CLIENT_TICK].
	 *
	 * @param client the Minecraft client.
	 */
	fun tick(client: MinecraftClient) {
		Scheduler.tick()
	}
}
