package de.hysky.skyblocker.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class QuickNavigationConfig {
	@kotlin.jvm.JvmField
	@SerialEntry
	var enableQuickNav: Boolean = true

	@kotlin.jvm.JvmField
	@SerialEntry
	var button1: QuickNavItem = QuickNavItem(true, ItemData("diamond_sword"), "Your Skills", "/skills")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button2: QuickNavItem = QuickNavItem(true, ItemData("painting"), "Collections", "/collection")

	/* REGEX Explanation
     * "Pets" : simple match on letters
     * "(?: \\(\\d+\\/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
     */
	@kotlin.jvm.JvmField
	@SerialEntry
	var button3: QuickNavItem = QuickNavItem(true, ItemData("bone"), "Pets(:? \\(\\d+\\/\\d+\\))?", "/pets")

	/* REGEX Explanation
     * "Wardrobe" : simple match on letters
     * " \\([12]\\/2\\)" : match on the page either " (1/2)" or " (2/2)"
     */
	@kotlin.jvm.JvmField
	@SerialEntry
	var button4: QuickNavItem = QuickNavItem(true, ItemData("leather_chestplate", 1, "[minecraft:dyed_color={rgb:8991416}]"), "Wardrobe \\([12]/2\\)", "/wardrobe")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button5: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={id:[I;-2081424676,-57521078,-2073572414,158072763],name:\"\",properties:[{name:\"textures\",value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}]"), "Sack of Sacks", "/sacks")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button6: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={name:\"5da6bec64bd942bc\",id:[I;1571208902,1272529596,-1566400349,-679283814],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0=\"}]}]"), "Accessory Bag(?: \\(\\d/\\d\\))?", "/accessories")

	/* REGEX Explanation
     * "(?:Rift )?" : optional match on the non-capturing group "Rift "
     * "Storage" : simple match on letters
     * "(?: \\([12]\\/2\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
     */
	@kotlin.jvm.JvmField
	@SerialEntry
	var button7: QuickNavItem = QuickNavItem(true, ItemData("ender_chest"), "(?:Rift )?Storage(?: \\(\\d/\\d\\))?", "/storage")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button8: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={name:\"421a8ef40eff47f4\",id:[I;1109036788,251611124,-2126904485,-130621758],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0=\"}]}]"), "", "/is")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button9: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={name:\"e30e30d02878417c\",id:[I;-485609264,678969724,-1929747597,-718202427],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ4ODBkMmMxZTdiODZlODc1MjJlMjA4ODI2NTZmNDViYWZkNDJmOTQ5MzJiMmM1ZTBkNmVjYWE0OTBjYjRjIn19fQ==\"}]}]"), "", "/warp garden")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button10: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={id:[I;-300151517,-631415889,-1193921967,-1821784279],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}]"), "none", "/hub")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button11: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={id:[I;1605800870,415127827,-1236127084,15358548],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}]"), "none", "/warp dungeon_hub")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button12: QuickNavItem = QuickNavItem(true, ItemData("gold_block"), "", "/ah")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button13: QuickNavItem = QuickNavItem(true, ItemData("player_head", 1, "[minecraft:profile={id:[I;-562285948,532499670,-1705302742,775653035],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZlMmRjZGE0MWVjM2FmZjhhZjUwZjI3MmVjMmUwNmE4ZjUwOWUwZjgwN2YyMzU1YTFmNWEzM2MxYjY2ZTliNCJ9fX0=\"}]}]"), "Bazaar .*", "/bz")

	@kotlin.jvm.JvmField
	@SerialEntry
	var button14: QuickNavItem = QuickNavItem(true, ItemData("crafting_table"), "Craft Item", "/craft")

	class QuickNavItem(@kotlin.jvm.JvmField @field:SerialEntry var render: Boolean, @kotlin.jvm.JvmField @field:SerialEntry var itemData: ItemData, @kotlin.jvm.JvmField @field:SerialEntry var uiTitle: String, @kotlin.jvm.JvmField @field:SerialEntry var clickEvent: String)

	class ItemData @JvmOverloads constructor(@kotlin.jvm.JvmField @field:SerialEntry var item: Item, @kotlin.jvm.JvmField @field:SerialEntry var count: Int = 1, @kotlin.jvm.JvmField @field:SerialEntry var components: String? = "[]") {
		@JvmOverloads
		constructor(item: String?, count: Int = 1, components: String? = "[]") : this(Registries.ITEM[Identifier(item)], count, components)
	}
}
