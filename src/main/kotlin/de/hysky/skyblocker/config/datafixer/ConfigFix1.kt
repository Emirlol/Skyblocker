package de.hysky.skyblocker.config.datafixer

import com.mojang.datafixers.DSL
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.Typed
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.util.Pair
import com.mojang.logging.LogUtils
import com.mojang.serialization.Dynamic
import com.mojang.serialization.OptionalDynamic
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer.componentsAsString
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer.fixUpItem
import net.minecraft.nbt.StringNbtReader

class ConfigFix1(outputSchema: Schema?, changesType: Boolean) : ConfigDataFix(outputSchema, changesType) {
	override fun makeRule(): TypeRewriteRule {
		return fixTypeEverywhereTyped(
			"ConfigFix1",
			inputSchema.getType(ConfigDataFixer.CONFIG_TYPE)
		) { typed: Typed<*> -> typed.update(DSL.remainderFinder()) { dynamic: Dynamic<*> -> this.fix(dynamic) } }
	}

	private fun <T> fix(dynamic: Dynamic<T?>): Dynamic<T?> {
		return fixMisc(fixQuickNav(fixChat(fixSlayers(fixOtherLocations(fixFarming(fixMining(fixCrimsonIsle(fixDungeons(fixHelpers(fixUIAndVisuals(fixGeneral(fixVersion(dynamic)))))))))))))
	}

	companion object {
		private fun <T> fixGeneral(dynamic: Dynamic<T>?): Dynamic<T> {
			return dynamic!!.update("general") { general: Dynamic<*> -> general.update("itemTooltip") { itemTooltip: Dynamic<*> -> itemTooltip.setFieldIfPresent("dungeonQuality", general["dungeonQuality"].result()) }.remove("dungeonQuality") }
		}

		private fun <T> fixUIAndVisuals(dynamic: Dynamic<T>): Dynamic<T> {
			val general = dynamic["general"]
			return dynamic.set(
				"uiAndVisuals", dynamic.emptyMap()
					.setFieldIfPresent("compactorDeletorPreview", general["compactorDeletorPreview"].result())
					.setFieldIfPresent("dontStripSkinAlphaValues", general["dontStripSkinAlphaValues"].result())
					.setFieldIfPresent("backpackPreviewWithoutShift", general["backpackPreviewWithoutShift"].result())
					.setFieldIfPresent("hideEmptyTooltips", general["hideEmptyTooltips"].result())
					.setFieldIfPresent("fancyCraftingTable", general["fancyCraftingTable"].result())
					.setFieldIfPresent("hideStatusEffectOverlay", general["hideStatusEffectOverlay"].result())
					.setFieldIfPresent("chestValue", general["chestValue"].result())
					.setFieldIfPresent("itemCooldown", general["itemCooldown"].result())
					.setFieldIfPresent("titleContainer", general["titleContainer"].result())
					.setFieldIfPresent("tabHud", general["tabHud"].result())
					.setFieldIfPresent("fancyAuctionHouse", general["fancyAuctionHouse"].result())
					.setFieldIfPresent("bars", general["bars"].result())
					.setFieldIfPresent("waypoints", general["waypoints"].result())
					.setFieldIfPresent("teleportOverlay", general["teleportOverlay"].result())
					.setFieldIfPresent("searchOverlay", general["searchOverlay"].result())
					.setFieldIfPresent("flameOverlay", general["flameOverlay"].result())
			).update(
				"general"
			) { newGeneral: Dynamic<*> ->
				newGeneral
					.remove("compactorDeletorPreview")
					.remove("dontStripSkinAlphaValues")
					.remove("backpackPreviewWithoutShift")
					.remove("hideEmptyTooltips")
					.remove("fancyCraftingTable")
					.remove("hideStatusEffectOverlay")
					.remove("chestValue")
					.remove("itemCooldown")
					.remove("titleContainer")
					.remove("tabHud")
					.remove("fancyAuctionHouse")
					.remove("bars")
					.remove("waypoints")
					.remove("teleportOverlay")
					.remove("searchOverlay")
					.remove("flameOverlay")
			}
		}

		private fun <T> fixHelpers(dynamic: Dynamic<T>): Dynamic<T> {
			val general = dynamic["general"]
			return dynamic.set(
				"helpers", dynamic.emptyMap()
					.setFieldIfPresent("enableNewYearCakesHelper", general["enableNewYearCakesHelper"].result())
					.setFieldIfPresent("mythologicalRitual", general["mythologicalRitual"].result())
					.setFieldIfPresent("experiments", general["experiments"].result())
					.setFieldIfPresent("fishing", general["fishing"].result())
					.setFieldIfPresent("fairySouls", general["fairySouls"].result())
			).update(
				"general"
			) { newGeneral: Dynamic<*> ->
				newGeneral
					.remove("enableNewYearCakesHelper")
					.remove("mythologicalRitual")
					.remove("experiments")
					.remove("fishing")
					.remove("fairySouls")
			}
		}

		private fun <T> fixDungeons(dynamic: Dynamic<T>): Dynamic<T> {
			val general = dynamic["general"]
			val dungeons = dynamic["locations"]["dungeons"]
			return dynamic.set(
				"dungeons", dynamic.emptyMap()
					.setFieldIfPresent("fancyPartyFinder", general["betterPartyFinder"].result())
					.setFieldIfPresent("croesusHelper", dungeons["croesusHelper"].result())
					.setFieldIfPresent("playerSecretsTracker", dungeons["playerSecretsTracker"].result())
					.setFieldIfPresent("starredMobGlow", dungeons["starredMobGlow"].result())
					.setFieldIfPresent("starredMobBoundingBoxes", dungeons["starredMobBoundingBoxes"].result())
					.setFieldIfPresent("allowDroppingProtectedItems", dungeons["allowDroppingProtectedItems"].result())
					.set(
						"dungeonMap", dynamic.emptyMap()
							.setFieldIfPresent("enableMap", dungeons["enableMap"].result())
							.setFieldIfPresent("mapScaling", dungeons["mapScaling"].result())
							.setFieldIfPresent("mapX", dungeons["mapX"].result())
							.setFieldIfPresent("mapY", dungeons["mapY"].result())
					)
					.set(
						"puzzleSolvers", dynamic.emptyMap()
							.setFieldIfPresent("solveThreeWeirdos", dungeons["solveThreeWeirdos"].result())
							.setFieldIfPresent("blazeSolver", dungeons["blazeSolver"].result())
							.setFieldIfPresent("creeperSolver", dungeons["creeperSolver"].result())
							.setFieldIfPresent("solveTrivia", dungeons["solveTrivia"].result())
							.setFieldIfPresent("solveTicTacToe", dungeons["solveTicTacToe"].result())
							.setFieldIfPresent("solveWaterboard", dungeons["solveWaterboard"].result())
							.setFieldIfPresent("solveBoulder", dungeons["solveBoulder"].result())
							.setFieldIfPresent("solveIceFill", dungeons["solveIceFill"].result())
							.setFieldIfPresent("solveSilverfish", dungeons["solveSilverfish"].result())
					)
					.set(
						"theProfessor", dynamic.emptyMap()
							.setFieldIfPresent("fireFreezeStaffTimer", dungeons["fireFreezeStaffTimer"].result())
							.setFieldIfPresent("floor3GuardianHealthDisplay", dungeons["floor3GuardianHealthDisplay"].result())
					)
					.setFieldIfPresent("livid", dungeons["lividColor"].result())
					.setFieldIfPresent("terminals", dungeons["terminals"].result())
					.setFieldIfPresent("secretWaypoints", dungeons["secretWaypoints"].result())
					.setFieldIfPresent("mimicMessage", dungeons["mimicMessage"].result())
					.setFieldIfPresent("doorHighlight", dungeons["doorHighlight"].result())
					.setFieldIfPresent("dungeonScore", dungeons["dungeonScore"].result())
					.setFieldIfPresent("dungeonChestProfit", dungeons["dungeonChestProfit"].result())
			).update("locations") { locations: Dynamic<*> -> locations.remove("dungeons") }.update("general") { newGeneral: Dynamic<*> -> newGeneral.remove("betterPartyFinder") }
		}

		private fun <T> fixCrimsonIsle(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.setFieldIfPresent("crimsonIsle", dynamic["locations"]["crimsonIsle"].result()).update("locations") { locations: Dynamic<*> -> locations.remove("crimsonIsle") }
		}

		private fun <T> fixMining(dynamic: Dynamic<T>): Dynamic<T> {
			val dwarvenMines = dynamic["locations"]["dwarvenMines"]
			return dynamic.set(
				"mining", dynamic.emptyMap()
					.setFieldIfPresent("enableDrillFuel", dwarvenMines["enableDrillFuel"].result())
					.set(
						"dwarvenMines", dynamic.emptyMap()
							.setFieldIfPresent("solveFetchur", dwarvenMines["solveFetchur"].result())
							.setFieldIfPresent("solvePuzzler", dwarvenMines["solvePuzzler"].result())
					)
					.set(
						"dwarvenHud", dwarvenMines["dwarvenHud"].result().orElseThrow()
							.renameField("x", "commissionsX")
							.renameField("y", "commissionsY")
					)
					.setFieldIfPresent("crystalsHud", dwarvenMines["crystalsHud"].result())
					.setFieldIfPresent("crystalsWaypoints", dwarvenMines["crystalsWaypoints"].result())
					.set(
						"crystalHollows", dynamic.emptyMap()
							.setFieldIfPresent("metalDetectorHelper", dwarvenMines["metalDetectorHelper"].result())
					)
			).update("locations") { locations: Dynamic<*> -> locations.remove("dwarvenMines") }
		}

		private fun <T> fixFarming(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.set(
				"farming", dynamic.emptyMap()
					.setFieldIfPresent("garden", dynamic["locations"]["garden"].result())
			).update("locations") { locations: Dynamic<*> -> locations.remove("garden") }
		}

		private fun <T> fixOtherLocations(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.renameField("locations", "otherLocations")
		}

		private fun <T> fixSlayers(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.renameField("slayer", "slayers")
		}

		private fun <T> fixChat(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.renameField("messages", "chat")
		}

		private fun <T> fixQuickNav(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.update("quickNav") { quickNav: Dynamic<*> -> quickNav.updateMapValues { button: Pair<Dynamic<*>, Dynamic<*>> -> if (button.first.asString().getOrThrow().startsWith("button")) button.mapSecond { button: Dynamic<*> -> fixQuickNavButton(button) } else button } }
		}

		private fun <T> fixQuickNavButton(button: Dynamic<T>): Dynamic<T> {
			return button.update(
				"item"
			) { item: Dynamic<*> ->
				item
					.renameField("itemName", "id")
					.renameAndFixField("nbt", "components") { nbt: Dynamic<*> -> fixNbt(item["itemName"], nbt) }
			}
		}

		private fun fixNbt(id: OptionalDynamic<*>, nbt: Dynamic<*>): Dynamic<*> {
			try {
				var itemNbt = "{id:\"minecraft:" + id.asString().getOrThrow().lowercase() + "\",Count:1"
				val extraNbt = nbt.asString().getOrThrow()
				if (extraNbt.length > 2) itemNbt += ",$extraNbt"
				itemNbt += "}"

				val fixed = fixUpItem(StringNbtReader.parse(itemNbt))

				return nbt.createString(componentsAsString(fixed))
			} catch (e: Exception) {
				ConfigDataFixer.LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to convert nbt to components!", e)
			}

			return nbt.createString("[]")
		}

		private fun <T> fixMisc(dynamic: Dynamic<T>): Dynamic<T> {
			return dynamic.set(
				"misc", dynamic.emptyMap()
					.setFieldIfPresent("richPresence", dynamic["richPresence"].result())
			).remove("richPresence")
		}
	}
}
