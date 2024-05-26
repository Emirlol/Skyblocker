package de.hysky.skyblocker.config.configs

import de.hysky.skyblocker.utils.waypoint.Waypoint
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.util.Formatting
import java.awt.Color

class UIAndVisualsConfig {
	@kotlin.jvm.JvmField
    @SerialEntry
	var compactorDeletorPreview: Boolean = true

	@kotlin.jvm.JvmField
    @SerialEntry
	var dontStripSkinAlphaValues: Boolean = true

	@kotlin.jvm.JvmField
    @SerialEntry
	var backpackPreviewWithoutShift: Boolean = false

	@kotlin.jvm.JvmField
    @SerialEntry
	var hideEmptyTooltips: Boolean = true

	@kotlin.jvm.JvmField
    @SerialEntry
	var fancyCraftingTable: Boolean = true

	@kotlin.jvm.JvmField
    @SerialEntry
	var hideStatusEffectOverlay: Boolean = false

	@kotlin.jvm.JvmField
    @SerialEntry
	var chestValue: ChestValue = ChestValue()

	@kotlin.jvm.JvmField
    @SerialEntry
	var itemCooldown: ItemCooldown = ItemCooldown()

	@kotlin.jvm.JvmField
    @SerialEntry
	var titleContainer: TitleContainer = TitleContainer()

	@kotlin.jvm.JvmField
    @SerialEntry
	var tabHud: TabHudConf = TabHudConf()

	@kotlin.jvm.JvmField
    @SerialEntry
	var fancyAuctionHouse: FancyAuctionHouse = FancyAuctionHouse()

	@kotlin.jvm.JvmField
    @SerialEntry
	var bars: Bars = Bars()

	@kotlin.jvm.JvmField
    @SerialEntry
	var waypoints: Waypoints = Waypoints()

	@kotlin.jvm.JvmField
    @SerialEntry
	var teleportOverlay: TeleportOverlay = TeleportOverlay()

	@kotlin.jvm.JvmField
    @SerialEntry
	var searchOverlay: SearchOverlay = SearchOverlay()

	@kotlin.jvm.JvmField
    @SerialEntry
	var inputCalculator: InputCalculator = InputCalculator()

	@kotlin.jvm.JvmField
    @SerialEntry
	var flameOverlay: FlameOverlay = FlameOverlay()

	@kotlin.jvm.JvmField
    @SerialEntry
	var compactDamage: CompactDamage = CompactDamage()

	class ChestValue {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enableChestValue: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var color: Formatting = Formatting.DARK_GREEN

		@kotlin.jvm.JvmField
        @SerialEntry
		var incompleteColor: Formatting = Formatting.BLUE
	}

	class ItemCooldown {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enableItemCooldowns: Boolean = true
	}

	class TitleContainer {
		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
        @SerialEntry
		var tabHudEnabled: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var tabHudScale: Int = 100

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableHudBackground: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var plainPlayerNames: Boolean = false

		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
        @SerialEntry
		var enabled: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var highlightCheapBIN: Boolean = true
	}

	class Bars {
		@kotlin.jvm.JvmField
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
		@kotlin.jvm.JvmField
        @SerialEntry
		var enableWaypoints: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var waypointType: Waypoint.Type = Waypoint.Type.WAYPOINT
	}

	class TeleportOverlay {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enableTeleportOverlays: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableWeirdTransmission: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableInstantTransmission: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableEtherTransmission: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableSinrecallTransmission: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableWitherImpact: Boolean = true
	}

	class SearchOverlay {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enableBazaar: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableAuctionHouse: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var keepPreviousSearches: Boolean = false

		@kotlin.jvm.JvmField
        @SerialEntry
		var maxSuggestions: Int = 3

		@kotlin.jvm.JvmField
        @SerialEntry
		var historyLength: Int = 3

		@kotlin.jvm.JvmField
        @SerialEntry
		var enableCommands: Boolean = false

		@SerialEntry
		var bazaarHistory: List<String> = ArrayList()

		@SerialEntry
		var auctionHistory: List<String> = ArrayList()
	}

	class InputCalculator {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enabled: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var requiresEquals: Boolean = false
	}

	class FlameOverlay {
		@kotlin.jvm.JvmField
        @SerialEntry
		var flameHeight: Int = 100

		@kotlin.jvm.JvmField
        @SerialEntry
		var flameOpacity: Int = 100
	}

	class CompactDamage {
		@kotlin.jvm.JvmField
        @SerialEntry
		var enabled: Boolean = true

		@kotlin.jvm.JvmField
        @SerialEntry
		var precision: Int = 1

		@kotlin.jvm.JvmField
        @SerialEntry
		var normalDamageColor: Color = Color(0xFFFFFF)

		@kotlin.jvm.JvmField
        @SerialEntry
		var critDamageGradientStart: Color = Color(0xFFFF55)

		@kotlin.jvm.JvmField
        @SerialEntry
		var critDamageGradientEnd: Color = Color(0xFF5555)
	}
}
