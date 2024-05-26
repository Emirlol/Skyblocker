package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.SkyblockerMod
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.minecraft.util.Identifier

object DungeonTextures {
	fun init() {
		ResourceManagerHelper.registerBuiltinResourcePack(
			Identifier(SkyblockerMod.NAMESPACE, "recolored_dungeon_items"),
			SkyblockerMod.SKYBLOCKER_MOD,
			ResourcePackActivationType.NORMAL
		)
	}
}
