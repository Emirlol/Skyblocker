package de.hysky.skyblocker.skyblock.item

import com.google.gson.JsonObject
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import de.hysky.skyblocker.utils.ProfileUtils
import de.hysky.skyblocker.utils.ProfileUtils.updateProfile
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.block.BlockState
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.Consumer
import kotlin.math.max

object ItemCooldowns {
	private const val JUNGLE_AXE_ID = "JUNGLE_AXE"
	private const val TREECAPITATOR_ID = "TREECAPITATOR_AXE"
	private const val GRAPPLING_HOOK_ID = "GRAPPLING_HOOK"
	private val BAT_ARMOR_IDS = listOf("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS")
	private val ITEM_COOLDOWNS: MutableMap<String, CooldownEntry> = HashMap()
	private val EXPERIENCE_LEVELS = intArrayOf(
		0, 660, 730, 800, 880, 960, 1050, 1150, 1260, 1380, 1510, 1650, 1800, 1960, 2130,
		2310, 2500, 2700, 2920, 3160, 3420, 3700, 4000, 4350, 4750, 5200, 5700, 6300, 7000,
		7800, 8700, 9700, 10800, 12000, 13300, 14700, 16200, 17800, 19500, 21300, 23200,
		25200, 27400, 29800, 32400, 35200, 38200, 41400, 44800, 48400, 52200, 56200, 60400,
		64800, 69400, 74200, 79200, 84700, 90700, 97200, 104200, 111700, 119700, 128200,
		137200, 147700, 156700, 167700, 179700, 192700, 206700, 221700, 237700, 254700,
		272700, 291700, 311700, 333700, 357700, 383700, 411700, 441700, 476700, 516700,
		561700, 611700, 666700, 726700, 791700, 861700, 936700, 1016700, 1101700, 1191700,
		1286700, 1386700, 1496700, 1616700, 1746700, 1886700
	)
	var monkeyLevel: Int = 1
	var monkeyExp: Double = 0.0

	fun init() {
		ClientPlayerBlockBreakEvents.AFTER.register(ClientPlayerBlockBreakEvents.After { obj: ClientWorld?, world: ClientPlayerEntity?, player: BlockPos?, pos: BlockState? -> afterBlockBreak(world, player, pos) })
		UseItemCallback.EVENT.register(UseItemCallback { obj: PlayerEntity?, player: World?, world: Hand? -> onItemInteract(player, world) })
	}

	fun updateCooldown() {
		updateProfile().thenAccept(Consumer { player: JsonObject ->
			for (pet in player.getAsJsonObject("pets_data").getAsJsonArray("pets")) {
				if (pet.asJsonObject["type"].asString != "MONKEY") continue
				if (pet.asJsonObject["active"].asString != "true") continue
				if (pet.asJsonObject["tier"].asString == "LEGENDARY") {
					monkeyExp = pet.asJsonObject["exp"].asString.toDouble()
					monkeyLevel = 0
					for (xpLevel in EXPERIENCE_LEVELS) {
						if (monkeyExp < xpLevel) {
							break
						} else {
							monkeyExp -= xpLevel.toDouble()
							monkeyLevel++
						}
					}
				}
			}
		}).exceptionally { e: Throwable? ->
			ProfileUtils.LOGGER.error("[Skyblocker Item Cooldown] Failed to get Player Pet Data, is the API Down/Limited?", e)
			null
		}
	}

	private val cooldown: Int
		get() {
			val baseCooldown = 2000
			val monkeyPetCooldownReduction = baseCooldown * monkeyLevel / 200
			return baseCooldown - monkeyPetCooldownReduction
		}

	fun afterBlockBreak(world: World?, player: PlayerEntity, pos: BlockPos?, state: BlockState) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns) return
		val usedItemId = getItemId(player.mainHandStack)
		if (usedItemId.isEmpty()) return
		if (state.isIn(BlockTags.LOGS)) {
			if (usedItemId == JUNGLE_AXE_ID || usedItemId == TREECAPITATOR_ID) {
				updateCooldown()
				if (!isOnCooldown(JUNGLE_AXE_ID) || !isOnCooldown(TREECAPITATOR_ID)) {
					ITEM_COOLDOWNS[usedItemId] = CooldownEntry(cooldown)
				}
			}
		}
	}

	private fun onItemInteract(player: PlayerEntity, world: World, hand: Hand): TypedActionResult<ItemStack?> {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns) return TypedActionResult.pass(ItemStack.EMPTY)
		val usedItemId = getItemId(player.mainHandStack)
		if (usedItemId == GRAPPLING_HOOK_ID && player.fishHook != null) {
			if (!isOnCooldown(GRAPPLING_HOOK_ID) && !isWearingBatArmor(player)) {
				ITEM_COOLDOWNS[GRAPPLING_HOOK_ID] = CooldownEntry(2000)
			}
		}

		return TypedActionResult.pass(ItemStack.EMPTY)
	}

	@JvmStatic
    fun isOnCooldown(itemStack: ItemStack?): Boolean {
		return isOnCooldown(getItemId(itemStack!!))
	}

	private fun isOnCooldown(itemId: String): Boolean {
		if (ITEM_COOLDOWNS.containsKey(itemId)) {
			val cooldownEntry = ITEM_COOLDOWNS[itemId]
			if (cooldownEntry!!.isOnCooldown) {
				return true
			} else {
				ITEM_COOLDOWNS.remove(itemId)
				return false
			}
		}

		return false
	}

	@JvmStatic
    fun getItemCooldownEntry(itemStack: ItemStack?): CooldownEntry? {
		return ITEM_COOLDOWNS[getItemId(itemStack!!)]
	}

	private fun isWearingBatArmor(player: PlayerEntity): Boolean {
		for (stack in player.armorItems) {
			val itemId = getItemId(stack)
			if (!BAT_ARMOR_IDS.contains(itemId)) {
				return false
			}
		}
		return true
	}

	@JvmRecord
	data class CooldownEntry(val cooldown: Int, val startTime: Long) {
		constructor(cooldown: Int) : this(cooldown, System.currentTimeMillis())

		val isOnCooldown: Boolean
			get() = (this.startTime + this.cooldown) > System.currentTimeMillis()

		val remainingCooldown: Long
			get() {
				val time = (this.startTime + this.cooldown) - System.currentTimeMillis()
				return max(time.toDouble(), 0.0).toLong()
			}

		val remainingCooldownPercent: Float
			get() = if (this.isOnCooldown) remainingCooldown.toFloat() / cooldown else 0.0f
	}
}
