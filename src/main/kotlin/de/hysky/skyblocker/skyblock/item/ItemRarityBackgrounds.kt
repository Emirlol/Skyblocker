package de.hysky.skyblocker.skyblock.item

import com.google.common.collect.ImmutableMap
import com.mojang.blaze3d.systems.RenderSystem
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.GeneralConfig.ItemInfoDisplay
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import de.hysky.skyblocker.utils.scheduler.Scheduler
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.BeforeInit
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import java.util.*
import java.util.Map
import java.util.function.Supplier

object ItemRarityBackgrounds {
	private val CONFIG: ItemInfoDisplay = SkyblockerConfigManager.get().general.itemInfoDisplay
	private val SPRITE = Supplier { MinecraftClient.getInstance().guiAtlasManager.getSprite(CONFIG.itemRarityBackgroundStyle.tex) }
	@JvmField
	val LORE_RARITIES: ImmutableMap<String, SkyblockItemRarity> = ImmutableMap.ofEntries(
		Map.entry("ADMIN", SkyblockItemRarity.ADMIN),
		Map.entry("ULTIMATE", SkyblockItemRarity.ULTIMATE),
		Map.entry("SPECIAL", SkyblockItemRarity.SPECIAL),  //Very special is the same color so this will cover it
		Map.entry("DIVINE", SkyblockItemRarity.DIVINE),
		Map.entry("MYTHIC", SkyblockItemRarity.MYTHIC),
		Map.entry("LEGENDARY", SkyblockItemRarity.LEGENDARY),
		Map.entry("LEGENJERRY", SkyblockItemRarity.LEGENDARY),
		Map.entry("EPIC", SkyblockItemRarity.EPIC),
		Map.entry("RARE", SkyblockItemRarity.RARE),
		Map.entry("UNCOMMON", SkyblockItemRarity.UNCOMMON),
		Map.entry("COMMON", SkyblockItemRarity.COMMON)
	)
	private val CACHE = Int2ReferenceOpenHashMap<SkyblockItemRarity?>()

	fun init() {
		//Clear the cache every 5 minutes, ints are very compact!
		Scheduler.INSTANCE.scheduleCyclic({ CACHE.clear() }, 4800)

		//Clear cache after a screen where items can be upgraded in rarity closes
		ScreenEvents.BEFORE_INIT.register(BeforeInit { client: MinecraftClient?, screen: Screen, scaledWidth: Int, scaledHeight: Int ->
			val title = screen.title.string
			if (isOnSkyblock && (title.contains("The Hex") || title == "Craft Item" || title == "Anvil" || title == "Reforge Anvil")) {
				ScreenEvents.remove(screen).register(ScreenEvents.Remove { screen1: Screen? -> CACHE.clear() })
			}
		})
	}

	@JvmStatic
	fun tryDraw(stack: ItemStack?, context: DrawContext, x: Int, y: Int) {
		val client = MinecraftClient.getInstance()

		if (client.player != null) {
			val itemRarity = getItemRarity(stack, client.player)

			if (itemRarity != null) draw(context, x, y, itemRarity)
		}
	}

	private fun getItemRarity(stack: ItemStack?, player: ClientPlayerEntity?): SkyblockItemRarity? {
		if (stack == null || stack.isEmpty) return null

		val itemUuid = getItemUuid(stack)

		//If the item has an uuid, then use the hash code of the uuid otherwise use the identity hash code of the stack
		val hashCode = if (itemUuid.isEmpty()) System.identityHashCode(stack) else itemUuid.hashCode()

		if (CACHE.containsKey(hashCode)) return CACHE[hashCode]

		val lore = getLore(stack)
		val stringifiedTooltip = lore.stream().map<String> { obj: Text -> obj.string }.toArray<String> { _Dummy_.__Array__() }

		for (rarityString in LORE_RARITIES.keys) {
			if (Arrays.stream(stringifiedTooltip).anyMatch { line: String -> line.contains(rarityString) }) {
				val rarity = LORE_RARITIES[rarityString]

				CACHE.put(hashCode, rarity)
				return rarity
			}
		}

		CACHE.put(hashCode, null)
		return null
	}

	private fun draw(context: DrawContext, x: Int, y: Int, rarity: SkyblockItemRarity) {
		//Enable blending to handle HUD translucency
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()

		context.drawSprite(x, y, 0, 16, 16, SPRITE.get(), rarity.r, rarity.g, rarity.b, SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgroundsOpacity)

		RenderSystem.disableBlend()
	}
}
