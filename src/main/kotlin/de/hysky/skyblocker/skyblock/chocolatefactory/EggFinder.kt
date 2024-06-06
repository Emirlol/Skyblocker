package de.hysky.skyblocker.skyblock.chocolatefactory

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.utils.ColorUtils.getFloatComponents
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getHeadTextureNullable
import de.hysky.skyblocker.utils.Location
import de.hysky.skyblocker.utils.SkyblockTime
import de.hysky.skyblocker.utils.Utils.location
import de.hysky.skyblocker.utils.waypoint.Waypoint
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object EggFinder {
	private val eggFoundPattern = Regex("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner)")
	private val newEggPattern = Regex("^HOPPITY'S HUNT A Chocolate (Breakfast|Lunch|Dinner) Egg has appeared!$")
	private val logger: Logger = LoggerFactory.getLogger("Skyblocker Egg Finder")
	private val armorStandQueue = LinkedList<ArmorStandEntity>()
	private val possibleLocations = arrayOf(Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.HUB, Location.THE_END, Location.THE_PARK, Location.GOLD_MINE)
	private var isLocationCorrect = false

	fun init() {
		ClientPlayConnectionEvents.JOIN.register { _, _, _ -> invalidateState() }
		SkyblockEvents.LOCATION_CHANGE.register(::handleLocationChange)
		ClientReceiveMessageEvents.GAME.register(::onChatMessage)
		WorldRenderEvents.AFTER_TRANSLUCENT.register(::renderWaypoints)
	}

	private fun handleLocationChange(location: Location) {
		for (possibleLocation in possibleLocations) {
			if (location == possibleLocation) {
				isLocationCorrect = true
				break
			}
		}
		if (!isLocationCorrect) {
			armorStandQueue.clear()
			return
		}

		while (armorStandQueue.isNotEmpty()) {
			handleArmorStand(armorStandQueue.poll())
		}
	}

	fun checkIfEgg(entity: Entity) {
		if (entity is ArmorStandEntity) checkIfEgg(entity)
	}

	fun checkIfEgg(armorStand: ArmorStandEntity) {
		if (!SkyblockerConfigManager.config.helpers.chocolateFactory.enableEggFinder) return
		if (SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return
		if (armorStand.hasCustomName() || !armorStand.isInvisible || !armorStand.shouldHideBasePlate()) return
		if (location == null) { //The location is unknown upon world change and will be changed via /locraw soon, so we can queue it for now
			armorStandQueue += armorStand
			return
		}
		if (isLocationCorrect) handleArmorStand(armorStand)
	}

	private fun handleArmorStand(armorStand: ArmorStandEntity) {
		getHeadTextureNullable(armorStand.armorItems.last())?.let { texture: String ->
			for (type in EggType.entries) { //Compare blockPos rather than entity to avoid incorrect matches when the entity just moves rather than a new one being spawned elsewhere
				if (texture == type.texture && type.egg?.entity?.blockPos != armorStand.blockPos) {
					handleFoundEgg(armorStand, type)
					return
				}
			}
		}
	}

	private fun invalidateState() {
		if (!SkyblockerConfigManager.config.helpers.chocolateFactory.enableEggFinder) return
		isLocationCorrect = false
		for (type in EggType.entries) {
			type.egg = null
		}
	}

	private fun handleFoundEgg(entity: ArmorStandEntity, eggType: EggType) {
		eggType.egg = Egg(entity, Waypoint(entity.blockPos.up(2), SkyblockerConfigManager.config.helpers.chocolateFactory.waypointType, getFloatComponents(eggType.color)))

		if (!SkyblockerConfigManager.config.helpers.chocolateFactory.sendEggFoundMessages) return
		MinecraftClient.getInstance().player!!.sendMessage(
			Constants.PREFIX
				.append("Found a ")
				.append(Text.literal("Chocolate $eggType Egg").withColor(eggType.color))
				.append(" at " + entity.blockPos.up(2).toShortString() + "!")
		)
	}

	private fun renderWaypoints(context: WorldRenderContext) {
		if (!SkyblockerConfigManager.config.helpers.chocolateFactory.enableEggFinder) return
		for (type in EggType.entries) {
			if (type.egg?.waypoint?.shouldRender() == true) type.egg!!.waypoint.render(context)
		}
	}

	private fun onChatMessage(text: Text, overlay: Boolean) {
		if (overlay || !SkyblockerConfigManager.config.helpers.chocolateFactory.enableEggFinder) return
		eggFoundPattern.find(text.string)?.let {
			try {
				EggType.valueOf(it.groupValues[1].uppercase())?.egg?.waypoint?.setFound()
			} catch (e: IllegalArgumentException) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg found message. Tried to match against: ${it.groupValues[0]}", e)
			}
		}
	}

	private data class Egg(val entity: ArmorStandEntity, val waypoint: Waypoint)

	private abstract class EggType(var egg: Egg?, val color: Int, val texture: String) {
		init {
			entries.add(this)
		}

		companion object {
			val entries = mutableListOf<EggType>()
			fun valueOf(string: String) = entries.find { it.toString() == string }
		}
	}

	private data object LunchEgg : EggType(null, Formatting.BLUE.colorValue!!, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9")
	private data object DinnerEgg : EggType(null, Formatting.GREEN.colorValue!!, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0")
	private data object BreakfastEgg : EggType(null, Formatting.GOLD.colorValue!!, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0")
}
