package de.hysky.skyblocker.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
object SlayerUtils {
	//TODO: Cache this, probably included in Packet system
	fun getEntityArmorStands(entity: Entity): List<Entity> {
		return entity.entityWorld.getOtherEntities(entity, entity.boundingBox.expand(1.0, 2.5, 1.0)) { x: Entity -> x is ArmorStandEntity && x.hasCustomName() }
	}

	val slayerEntity: Entity?
		//Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
		get() {
			MinecraftClient.getInstance().world?.let { world ->
				for (entity in world.entities) {
					//Check if entity is Bloodfiend
					if (entity.hasCustomName() && entity.customName!!.string.contains("Bloodfiend")) {
						//Grab the players username
						val username = MinecraftClient.getInstance().session.username
						//Check all armor stands around the boss
						for (armorStand in getEntityArmorStands(entity)) {
							//Check if the display name contains the players username
							if (armorStand.displayName!!.string.contains(username)) {
								return entity
							}
						}
					}
				}
			}
			return null
		}

	val isInSlayer: Boolean
		get() {
			for (line in Utils.STRING_SCOREBOARD) {
				if (line.contains("Slay the boss!")) return true
			}
			return false
		}
}