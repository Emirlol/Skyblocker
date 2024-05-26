package de.hysky.skyblocker.skyblock.item.tooltip

import com.google.gson.JsonObject
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.GeneralConfig
import de.hysky.skyblocker.utils.Http.getEtag
import de.hysky.skyblocker.utils.Http.getLastModified
import de.hysky.skyblocker.utils.Http.sendGetRequest
import de.hysky.skyblocker.utils.Http.sendHeadRequest
import de.hysky.skyblocker.utils.Utils.isInTheRift
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Predicate

enum class TooltipInfoType
/**
 * @param address        the address to download the data from
 * @param dataEnabled    the predicate to check if data should be downloaded
 * @param tooltipEnabled the predicate to check if the tooltip should be shown
 * @param cacheable      whether the data should be cached
 */ @JvmOverloads constructor(private val address: String?, private val dataEnabled: Predicate<GeneralConfig.ItemTooltip>, private val tooltipEnabled: Predicate<GeneralConfig.ItemTooltip>, private val cacheable: Boolean, private val callback: Consumer<JsonObject?>? = null) : Runnable {
	NPC("https://hysky.de/api/npcprice", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableNPCPrice }, true),
	BAZAAR("https://hysky.de/api/bazaar", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableBazaarPrice || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit || SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue }, Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableBazaarPrice }, false),
	LOWEST_BINS("https://hysky.de/api/auctions/lowestbins", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableLowestBIN || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit || SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue }, Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableLowestBIN }, false),
	ONE_DAY_AVERAGE("https://hysky.de/api/auctions/lowestbins/average/1day.json", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableAvgBIN }, false),
	THREE_DAY_AVERAGE("https://hysky.de/api/auctions/lowestbins/average/3day.json", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableAvgBIN || SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableAuctionHouse }, Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableAvgBIN }, false),
	MOTES("https://hysky.de/api/motesprice", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableMotesPrice }, Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableMotesPrice && isInTheRift }, true),
	OBTAINED(Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableObtainedDate }),
	MUSEUM("https://hysky.de/api/museum", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableMuseumInfo }, true),
	COLOR("https://hysky.de/api/color", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableExoticTooltip }, true),
	ACCESSORIES("https://hysky.de/api/accessories", Predicate { itemTooltip: GeneralConfig.ItemTooltip -> itemTooltip.enableAccessoriesHelper }, true, Consumer { data: JsonObject? -> AccessoriesHelper.refreshData(data) });

	var data: JsonObject? = null
		private set
	private var hash: Long = 0

	/**
	 * Use this for when you're adding tooltip info that has no data associated with it
	 */
	constructor(enabled: Predicate<GeneralConfig.ItemTooltip>) : this(null, Predicate<GeneralConfig.ItemTooltip> { itemTooltip: GeneralConfig.ItemTooltip? -> false }, enabled, false, null)

	/**
	 * @param address   the address to download the data from
	 * @param enabled   the predicate to check if the data should be downloaded and the tooltip should be shown
	 * @param cacheable whether the data should be cached
	 * @param callback  called when the `data` is refreshed
	 */
	constructor(address: String, enabled: Predicate<GeneralConfig.ItemTooltip>, cacheable: Boolean, callback: Consumer<JsonObject?>) : this(address, enabled, enabled, cacheable, callback)

	/**
	 * @param address   the address to download the data from
	 * @param enabled   the predicate to check if the data should be downloaded and the tooltip should be shown
	 * @param cacheable whether the data should be cached
	 */
	constructor(address: String, enabled: Predicate<GeneralConfig.ItemTooltip>, cacheable: Boolean) : this(address, enabled, enabled, cacheable, null)

	/**
	 * @param address        the address to download the data from
	 * @param dataEnabled    the predicate to check if data should be downloaded
	 * @param tooltipEnabled the predicate to check if the tooltip should be shown
	 * @param cacheable      whether the data should be cached
	 */

	/**
	 * @return whether the data should be downloaded
	 */
	private fun isDataEnabled(): Boolean {
		return dataEnabled.test(ItemTooltip.config)
	}

	/**
	 * @return whether the tooltip should be shown
	 */
	fun isTooltipEnabled(): Boolean {
		return tooltipEnabled.test(ItemTooltip.config)
	}

	/**
	 * Checks if the data has the given member name and sends a warning message if data is null.
	 *
	 * @param memberName the member name to check
	 * @return whether the data has the given member name or not
	 */
	fun hasOrNullWarning(memberName: String?): Boolean {
		if (data == null) {
			ItemTooltip.nullWarning()
			return false
		} else return data!!.has(memberName)
	}

	/**
	 * Checks if the tooltip is enabled and the data has the given member name and sends a warning message if data is null.
	 *
	 * @param memberName the member name to check
	 * @return whether the tooltip is enabled and the data has the given member name or not
	 */
	fun isTooltipEnabledAndHasOrNullWarning(memberName: String?): Boolean {
		return isTooltipEnabled() && hasOrNullWarning(memberName)
	}

	/**
	 * Downloads the data if it is enabled.
	 *
	 * @param futureList the list to add the future to
	 */
	fun downloadIfEnabled(futureList: MutableList<CompletableFuture<Void?>?>) {
		if (isDataEnabled()) {
			download(futureList)
		}
	}

	/**
	 * Downloads the data.
	 *
	 * @param futureList the list to add the future to
	 */
	fun download(futureList: MutableList<CompletableFuture<Void?>?>) {
		futureList.add(CompletableFuture.runAsync(this))
	}

	/**
	 * Downloads the data.
	 */
	override fun run() {
		try {
			if (cacheable) {
				val headers = sendHeadRequest(address)
				val hash = (getEtag(headers).hashCode() + getLastModified(headers).hashCode()).toLong()
				if (this.hash == hash) return
				else this.hash = hash
			}
			data = SkyblockerMod.GSON.fromJson(sendGetRequest(address!!), JsonObject::class.java)

			callback?.accept(data)
		} catch (e: Exception) {
			ItemTooltip.LOGGER.warn("[Skyblocker] Failed to download $this prices!", e)
		}
	}
}
