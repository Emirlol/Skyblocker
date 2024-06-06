package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.MiningConfig.CommissionWaypointMode
import de.hysky.skyblocker.skyblock.dwarven.MiningLocationLabel.*
import de.hysky.skyblocker.utils.Utils.isInDwarvenMines
import de.hysky.skyblocker.utils.Utils.islandArea
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object CommissionLabels {
	private val DWARVEN_LOCATIONS = DwarvenCategory.entries.associateBy { it.toString() }
	private val DWARVEN_EMISSARIES = DwarvenEmissaries.entries
	private val GLACITE_LOCATIONS = GlaciteCategory.entries.associateBy { it.toString() }

	private var activeWaypoints: MutableList<MiningLocationLabel> = arrayListOf()

	init {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(::render)
	}

	/**
	 * update the activeWaypoints when there is a change in commissions
	 *
	 * @param newCommissions the new commissions to get the waypoints from
	 * @param completed      if there is a commission completed
	 */
	fun update(newCommissions: List<String>, completed: Boolean) {
		val currentMode = SkyblockerConfigManager.config.mining.commissionWaypoints.mode
		if (currentMode == CommissionWaypointMode.OFF) return

		activeWaypoints.clear()
		val location = islandArea.substring(2)
		//find commission locations in glacite
		if (location == "Dwarven Base Camp" || location == "Glacite Tunnels" || location == "Glacite Mineshafts" || location == "Glacite Lake") {
			if (currentMode != CommissionWaypointMode.BOTH && currentMode != CommissionWaypointMode.GLACITE) return

			for (commission in newCommissions) {
				for ((key, category) in GLACITE_LOCATIONS) {
					if (commission.contains(key)) {
						for (gemstoneLocation in category.locations) {
							activeWaypoints.add(MiningLocationLabel(category, gemstoneLocation))
						}
					}
				}
			}
			//add base waypoint if enabled
			if (SkyblockerConfigManager.config.mining.commissionWaypoints.showBaseCamp) {
				activeWaypoints.add(MiningLocationLabel(GlaciteCategory.CAMPFIRE, GlaciteCategory.CAMPFIRE.locations[0]))
			}
			return
		}
		//find commission locations in dwarven mines
		if (currentMode != CommissionWaypointMode.BOTH && currentMode != CommissionWaypointMode.DWARVEN) {
			return
		}

		for (commission in newCommissions) {
			for ((key, category) in DWARVEN_LOCATIONS) {
				if (key in commission) {
					category.isTitanium = "Titanium" in commission
					activeWaypoints += MiningLocationLabel(category, category.location)
				}
			}
		}
		//if there is a commission completed and enabled show emissary
		if (SkyblockerConfigManager.config.mining.commissionWaypoints.showEmissary && completed) {
			for (emissaries in DWARVEN_EMISSARIES) {
				activeWaypoints += MiningLocationLabel(emissaries, emissaries.location)
			}
		}
	}

	/**
	 * render all the active waypoints
	 *
	 * @param context render context
	 */
	private fun render(context: WorldRenderContext) {
		if (!isInDwarvenMines || SkyblockerConfigManager.config.mining.commissionWaypoints.mode == CommissionWaypointMode.OFF) return

		for (miningLocationLabel in activeWaypoints) {
			miningLocationLabel.render(context)
		}
	}
}
