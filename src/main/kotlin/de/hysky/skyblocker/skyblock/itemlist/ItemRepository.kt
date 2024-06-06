package de.hysky.skyblocker.skyblock.itemlist

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import de.hysky.skyblocker.utils.NEURepoManager
import de.hysky.skyblocker.utils.NEURepoManager.runAsyncAfterLoad
import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ItemRepository {
	val LOGGER: Logger = LoggerFactory.getLogger(ItemRepository::class.java)

	val items: MutableList<ItemStack> = ArrayList()
	private val itemsMap: MutableMap<String, ItemStack> = HashMap()
	val recipes: MutableList<SkyblockCraftingRecipe> = ArrayList()
	private var filesImported = false

	fun init() {
		runAsyncAfterLoad { ItemStackBuilder.loadPetNums() }
		runAsyncAfterLoad { importItemFiles() }
	}

	private fun importItemFiles() {
		NEURepoManager.NEU_REPO.items.items.values.forEach { loadItem(it) }
		NEURepoManager.NEU_REPO.items.items.values.forEach { loadRecipes(it) }

		items.sortWith { lhs: ItemStack, rhs: ItemStack ->
			val lhsInternalName = getItemId(lhs)
			val lhsFamilyName = lhsInternalName.replace(".\\d+$".toRegex(), "")
			val rhsInternalName = getItemId(rhs)
			val rhsFamilyName = rhsInternalName.replace(".\\d+$".toRegex(), "")
			if (lhsFamilyName == rhsFamilyName) {
				return@sortWith if (lhsInternalName.length != rhsInternalName.length) lhsInternalName.length - rhsInternalName.length
				else lhsInternalName.compareTo(rhsInternalName)
			}
			lhsFamilyName.compareTo(rhsFamilyName)
		}
		filesImported = true
	}

	private fun loadItem(item: NEUItem) {
		val stack = ItemStackBuilder.fromNEUItem(item)
		items.add(stack)
		itemsMap[item.skyblockItemId] = stack
	}

	private fun loadRecipes(item: NEUItem) {
		for (recipe in item.recipes) {
			if (recipe is NEUCraftingRecipe) {
				recipes.add(SkyblockCraftingRecipe.fromNEURecipe(recipe))
			}
		}
	}

	fun getWikiLink(internalName: String): String? {
		val item = NEURepoManager.NEU_REPO.items.getItemBySkyblockId(internalName)
		if (item == null || item.info == null || item.info.isEmpty()) {
			return null
		}

		val info = item.info
		val wikiLink0 = info.first()
		val wikiLink1 = if (info.size > 1) info[1] else ""
		val wikiDomain = if (SkyblockerConfigManager.config.general.wikiLookup.officialWiki) "https://wiki.hypixel.net" else "https://hypixel-skyblock.fandom.com"
		return when {
			wikiLink0.startsWith(wikiDomain) -> wikiLink0
			wikiLink1.startsWith(wikiDomain) -> return wikiLink1
			else -> null
		}
	}

	fun getRecipes(internalName: String): List<SkyblockCraftingRecipe> {
		val result: MutableList<SkyblockCraftingRecipe> = ArrayList()
		for (recipe in recipes) {
			if (recipe.result?.let { getItemId(it) } == internalName) result.add(recipe)
		}
		for (recipe in recipes) {
			for (ingredient in recipe.grid) {
				if (ingredient.item != Items.AIR && getItemId(ingredient) == internalName) {
					result.add(recipe)
					break
				}
			}
		}
		return result
	}

	fun getItemStack(internalName: String) = itemsMap[internalName]
}

