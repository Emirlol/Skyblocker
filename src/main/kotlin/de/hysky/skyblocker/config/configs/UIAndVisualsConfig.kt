package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.util.Formatting
import java.awt.Color

class UIAndVisualsConfig {
	@SerialEntry
	var compactorDeletorPreview: Boolean = true

	@SerialEntry
	var dontStripSkinAlphaValues: Boolean = true

	@SerialEntry
	var backpackPreviewWithoutShift: Boolean = false

	@SerialEntry
	var hideEmptyTooltips: Boolean = true

	@SerialEntry
	var fancyCraftingTable: Boolean = true

	@SerialEntry
	var hideStatusEffectOverlay: Boolean = false

	@SerialEntry
	var chestValue: ChestValue = ChestValue()

	@SerialEntry
	var itemCooldown: ItemCooldown = ItemCooldown()

	@SerialEntry
	var titleContainer: TitleContainer = TitleContainer()

	@SerialEntry
	var tabHud: TabHudConf = TabHudConf()

	@SerialEntry
	var fancyAuctionHouse: FancyAuctionHouse = FancyAuctionHouse()

	@SerialEntry
	var bars: Bars = Bars()

	@SerialEntry
	var waypoints: Waypoints = Waypoints()

	@SerialEntry
	var teleportOverlay: TeleportOverlay = TeleportOverlay()

	@SerialEntry
	var searchOverlay: SearchOverlay = SearchOverlay()

	@SerialEntry
	var inputCalculator: InputCalculator = InputCalculator()

	@SerialEntry
	var flameOverlay: FlameOverlay = FlameOverlay()

	@SerialEntry
	var compactDamage: CompactDamage = CompactDamage()

	class ChestValue {

		@SerialEntry
		var enableChestValue: Boolean = true

		@SerialEntry
		var color: Formatting = Formatting.DARK_GREEN

		@SerialEntry
		var incompleteColor: Formatting = Formatting.BLUE
	}

	class ItemCooldown {

		@SerialEntry
		var enableItemCooldowns: Boolean = true
	}

	class TitleContainer {

		@SerialEntry
		var titleContainerScale: Float = 100f

		@SerialEntry
		var x: Int = 540

		@SerialEntry
		var y: Int = 10

		@SerialEntry
		var direction: Direction = Direction.HORIZONTAL

		@SerialEntry
		var alignment: Alignment = Alignment.MIDDLE
	}

	enum class Direction {
		HORIZONTAL, VERTICAL;

		override fun toString(): String {
			return when (this) {
				HORIZONTAL -> "Horizontal"
				VERTICAL -> "Vertical"
			}
		}
	}

	enum class Alignment {
		LEFT, RIGHT, MIDDLE;

		override fun toString(): String {
			return when (this) {
				LEFT -> "Left"
				RIGHT -> "Right"
				MIDDLE -> "Middle"
			}
		}
	}

	class TabHudConf {

		@SerialEntry
		var tabHudEnabled: Boolean = true

		@SerialEntry
		var tabHudScale: Int = 100

		@SerialEntry
		var enableHudBackground: Boolean = true

		@SerialEntry
		var plainPlayerNames: Boolean = false

		@SerialEntry
		var nameSorting: NameSorting = NameSorting.DEFAULT
	}

	enum class NameSorting {
		DEFAULT, ALPHABETICAL;

		override fun toString(): String {
			return when (this) {
				DEFAULT -> "Default"
				ALPHABETICAL -> "Alphabetical"
			}
		}
	}

	class FancyAuctionHouse {

		@SerialEntry
		var enabled: Boolean = true

		@SerialEntry
		var highlightCheapBIN: Boolean = true
	}

	class Bars {

		@SerialEntry
		var enableBars: Boolean = true

		// Kept in for backwards compatibility, remove if needed
		@SerialEntry
		var barPositions: OldBarPositions = OldBarPositions()
	}

	/**
	 * Backwards compat
	 */
	class OldBarPositions {
		@SerialEntry
		var healthBarPosition: OldBarPosition = OldBarPosition.LAYER1

		@SerialEntry
		var manaBarPosition: OldBarPosition = OldBarPosition.LAYER1

		@SerialEntry
		var defenceBarPosition: OldBarPosition = OldBarPosition.LAYER1

		@SerialEntry
		var experienceBarPosition: OldBarPosition = OldBarPosition.LAYER1
	}

	/**
	 * Backwards compat
	 */
	enum class OldBarPosition {
		LAYER1, LAYER2, RIGHT, NONE
	}

	class Waypoints {

		@SerialEntry
		var enableWaypoints: Boolean = true

		@SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT
	}

	class TeleportOverlay {

		@SerialEntry
		var enableTeleportOverlays: Boolean = true

		@SerialEntry
		var enableWeirdTransmission: Boolean = true

		@SerialEntry
		var enableInstantTransmission: Boolean = true

		@SerialEntry
		var enableEtherTransmission: Boolean = true

		@SerialEntry
		var enableSinrecallTransmission: Boolean = true

		@SerialEntry
		var enableWitherImpact: Boolean = true
	}

	class SearchOverlay {

		@SerialEntry
		var enableBazaar: Boolean = true

		@SerialEntry
		var enableAuctionHouse: Boolean = true

		@SerialEntry
		var keepPreviousSearches: Boolean = false

		@SerialEntry
		var maxSuggestions: Int = 3

		@SerialEntry
		var historyLength: Int = 3

		@SerialEntry
		var enableCommands: Boolean = false

		@SerialEntry
		var bazaarHistory: List<String> = ArrayList()

		@SerialEntry
		var auctionHistory: List<String> = ArrayList()
	}

	class InputCalculator {

		@SerialEntry
		var enabled: Boolean = true

		@SerialEntry
		var requiresEquals: Boolean = false
	}

	class FlameOverlay {

		@SerialEntry
		var flameHeight: Int = 100

		@SerialEntry
		var flameOpacity: Int = 100
	}

	class CompactDamage {

		@SerialEntry
		var enabled: Boolean = true

		@SerialEntry
		var precision: Int = 1

		@SerialEntry
		var normalDamageColor: Color = Color(0xFFFFFF)

		@SerialEntry
		var critDamageGradientStart: Color = Color(0xFFFF55)

		@SerialEntry
		var critDamageGradientEnd: Color = Color(0xFF5555)
	}
}
