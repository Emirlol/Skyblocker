package de.hysky.skyblocker.skyblock.itemlist

object ItemFixerUpper {
	private val ANVIL_VARIANTS = arrayOf(
		"minecraft:anvil",
		"minecraft:chipped_anvil",
		"minecraft:damaged_anvil"
	)

	private val COAL_VARIANTS = arrayOf(
		"minecraft:coal",
		"minecraft:charcoal"
	)

	private val COBBLESTONE_WALL_VARIANTS = arrayOf(
		"minecraft:cobblestone_wall",
		"minecraft:mossy_cobblestone_wall"
	)

	private val COOKED_FISH_VARIANTS = arrayOf(
		"minecraft:cooked_cod",
		"minecraft:cooked_salmon"
	)

	private val DIRT_VARIANTS = arrayOf(
		"minecraft:dirt",
		"minecraft:coarse_dirt",
		"minecraft:podzol"
	)

	private val DOUBLE_PLANT_VARIANTS = arrayOf(
		"minecraft:sunflower",
		"minecraft:lilac",
		"minecraft:tall_grass",
		"minecraft:large_fern",
		"minecraft:rose_bush",
		"minecraft:peony"
	)

	private val DYE_VARIANTS = arrayOf(
		"minecraft:ink_sac",
		"minecraft:red_dye",
		"minecraft:green_dye",
		"minecraft:cocoa_beans",
		"minecraft:lapis_lazuli",
		"minecraft:purple_dye",
		"minecraft:cyan_dye",
		"minecraft:light_gray_dye",
		"minecraft:gray_dye",
		"minecraft:pink_dye",
		"minecraft:lime_dye",
		"minecraft:yellow_dye",
		"minecraft:light_blue_dye",
		"minecraft:magenta_dye",
		"minecraft:orange_dye",
		"minecraft:bone_meal"
	)

	private val FISH_VARIANTS = arrayOf(
		"minecraft:cod",
		"minecraft:salmon",
		"minecraft:tropical_fish",
		"minecraft:pufferfish"
	)

	private val GOLDEN_APPLE_VARIANTS = arrayOf(
		"minecraft:golden_apple",
		"minecraft:enchanted_golden_apple"
	)

	private val LOG_VARIANTS = arrayOf(
		"minecraft:oak_log",
		"minecraft:spruce_log",
		"minecraft:birch_log",
		"minecraft:jungle_log",
		"minecraft:oak_wood",
		"minecraft:spruce_wood",
		"minecraft:birch_wood",
		"minecraft:jungle_wood",
	)

	private val LOG2_VARIANTS = arrayOf(
		"minecraft:acacia_log",
		"minecraft:dark_oak_log",
		"minecraft:acacia_wood",
		"minecraft:dark_oak_wood"
	)

	private val MONSTER_EGG_VARIANTS = arrayOf(
		"minecraft:infested_stone",
		"minecraft:infested_cobblestone",
		"minecraft:infested_stone_bricks",
		"minecraft:infested_mossy_stone_bricks",
		"minecraft:infested_cracked_stone_bricks",
		"minecraft:infested_chiseled_stone_bricks"
	)

	private val PRISMARINE_VARIANTS = arrayOf(
		"minecraft:prismarine",
		"minecraft:prismarine_bricks",
		"minecraft:dark_prismarine"
	)

	private val QUARTZ_BLOCK_VARIANTS = arrayOf(
		"minecraft:quartz_block",
		"minecraft:chiseled_quartz_block",
		"minecraft:quartz_pillar"
	)

	private val RED_FLOWER_VARIANTS = arrayOf(
		"minecraft:poppy",
		"minecraft:blue_orchid",
		"minecraft:allium",
		"minecraft:azure_bluet",
		"minecraft:red_tulip",
		"minecraft:orange_tulip",
		"minecraft:white_tulip",
		"minecraft:pink_tulip",
		"minecraft:oxeye_daisy"
	)

	private val SAND_VARIANTS = arrayOf(
		"minecraft:sand",
		"minecraft:red_sand"
	)

	private val SKULL_VARIANTS = arrayOf(
		"minecraft:skeleton_skull",
		"minecraft:wither_skeleton_skull",
		"minecraft:zombie_head",
		"minecraft:player_head",
		"minecraft:creeper_head"
	)

	private val SPONGE_VARIANTS = arrayOf(
		"minecraft:sponge",
		"minecraft:wet_sponge"
	)

	private val STONE_VARIANTS = arrayOf(
		"minecraft:stone",
		"minecraft:granite",
		"minecraft:polished_granite",
		"minecraft:diorite",
		"minecraft:polished_diorite",
		"minecraft:andesite",
		"minecraft:polished_andesite"
	)

	private val STONE_SLAB_VARIANTS = arrayOf(
		"minecraft:smooth_stone_slab",
		"minecraft:sandstone_slab",
		"minecraft:petrified_oak_slab",
		"minecraft:cobblestone_slab",
		"minecraft:brick_slab",
		"minecraft:stone_brick_slab",
		"minecraft:nether_brick_slab",
		"minecraft:quartz_slab"
	)

	private val STONEBRICK_VARIANTS = arrayOf(
		"minecraft:stone_bricks",
		"minecraft:mossy_stone_bricks",
		"minecraft:cracked_stone_bricks",
		"minecraft:chiseled_stone_bricks"
	)

	private val TALLGRASS_VARIANTS = arrayOf(
		"minecraft:dead_bush",
		"minecraft:short_grass",
		"minecraft:fern"
	)

	private val SPAWN_EGG_VARIANTS: Map<Int, String> = java.util.Map.ofEntries( //This entry 0 is technically not right but Hypixel decided to make it polar bear so well we use that
		java.util.Map.entry(0, "minecraft:polar_bear_spawn_egg"),
		java.util.Map.entry(50, "minecraft:creeper_spawn_egg"),
		java.util.Map.entry(51, "minecraft:skeleton_spawn_egg"),
		java.util.Map.entry(52, "minecraft:spider_spawn_egg"),
		java.util.Map.entry(54, "minecraft:zombie_spawn_egg"),
		java.util.Map.entry(55, "minecraft:slime_spawn_egg"),
		java.util.Map.entry(56, "minecraft:ghast_spawn_egg"),
		java.util.Map.entry(57, "minecraft:zombified_piglin_spawn_egg"),
		java.util.Map.entry(58, "minecraft:enderman_spawn_egg"),
		java.util.Map.entry(59, "minecraft:cave_spider_spawn_egg"),
		java.util.Map.entry(60, "minecraft:silverfish_spawn_egg"),
		java.util.Map.entry(61, "minecraft:blaze_spawn_egg"),
		java.util.Map.entry(62, "minecraft:magma_cube_spawn_egg"),
		java.util.Map.entry(65, "minecraft:bat_spawn_egg"),
		java.util.Map.entry(66, "minecraft:witch_spawn_egg"),
		java.util.Map.entry(67, "minecraft:endermite_spawn_egg"),
		java.util.Map.entry(68, "minecraft:guardian_spawn_egg"),
		java.util.Map.entry(90, "minecraft:pig_spawn_egg"),
		java.util.Map.entry(91, "minecraft:sheep_spawn_egg"),
		java.util.Map.entry(92, "minecraft:cow_spawn_egg"),
		java.util.Map.entry(93, "minecraft:chicken_spawn_egg"),
		java.util.Map.entry(94, "minecraft:squid_spawn_egg"),
		java.util.Map.entry(95, "minecraft:wolf_spawn_egg"),
		java.util.Map.entry(96, "minecraft:mooshroom_spawn_egg"),
		java.util.Map.entry(98, "minecraft:ocelot_spawn_egg"),
		java.util.Map.entry(100, "minecraft:horse_spawn_egg"),
		java.util.Map.entry(101, "minecraft:rabbit_spawn_egg"),
		java.util.Map.entry(120, "minecraft:villager_spawn_egg")
	)

	private val SANDSTONE_VARIANTS = arrayOf(
		":",
		":chiseled_",
		":cut_"
	)

	private val COLOR_VARIANTS = arrayOf(
		":white_",
		":orange_",
		":magenta_",
		":light_blue_",
		":yellow_",
		":lime_",
		":pink_",
		":gray_",
		":light_gray_",
		":cyan_",
		":purple_",
		":blue_",
		":brown_",
		":green_",
		":red_",
		":black_"
	)

	private val WOOD_VARIANTS = arrayOf(
		":oak_",
		":spruce_",
		":birch_",
		":jungle_",
		":acacia_",
		":dark_oak_"
	)

	//this is the map of all renames
	private val RENAMED: Map<String, String> = java.util.Map.ofEntries(
		java.util.Map.entry("minecraft:bed", "minecraft:red_bed"),
		java.util.Map.entry("minecraft:boat", "minecraft:oak_boat"),
		java.util.Map.entry("minecraft:brick_block", "minecraft:bricks"),
		java.util.Map.entry("minecraft:deadbush", "minecraft:dead_bush"),
		java.util.Map.entry("minecraft:fence_gate", "minecraft:oak_fence_gate"),
		java.util.Map.entry("minecraft:fence", "minecraft:oak_fence"),
		java.util.Map.entry("minecraft:firework_charge", "minecraft:firework_star"),
		java.util.Map.entry("minecraft:fireworks", "minecraft:firework_rocket"),
		java.util.Map.entry("minecraft:golden_rail", "minecraft:powered_rail"),
		java.util.Map.entry("minecraft:grass", "minecraft:grass_block"),
		java.util.Map.entry("minecraft:hardened_clay", "minecraft:terracotta"),
		java.util.Map.entry("minecraft:lit_pumpkin", "minecraft:jack_o_lantern"),
		java.util.Map.entry("minecraft:melon_block", "minecraft:melon"),
		java.util.Map.entry("minecraft:melon", "minecraft:melon_slice"),
		java.util.Map.entry("minecraft:mob_spawner", "minecraft:spawner"),
		java.util.Map.entry("minecraft:nether_brick", "minecraft:nether_bricks"),
		java.util.Map.entry("minecraft:netherbrick", "minecraft:nether_brick"),
		java.util.Map.entry("minecraft:noteblock", "minecraft:note_block"),
		java.util.Map.entry("minecraft:piston_extension", "minecraft:moving_piston"),
		java.util.Map.entry("minecraft:portal", "minecraft:nether_portal"),
		java.util.Map.entry("minecraft:pumpkin", "minecraft:carved_pumpkin"),
		java.util.Map.entry("minecraft:quartz_ore", "minecraft:nether_quartz_ore"),
		java.util.Map.entry("minecraft:record_11", "minecraft:music_disc_11"),
		java.util.Map.entry("minecraft:record_13", "minecraft:music_disc_13"),
		java.util.Map.entry("minecraft:record_blocks", "minecraft:music_disc_blocks"),
		java.util.Map.entry("minecraft:record_cat", "minecraft:music_disc_cat"),
		java.util.Map.entry("minecraft:record_chirp", "minecraft:music_disc_chirp"),
		java.util.Map.entry("minecraft:record_far", "minecraft:music_disc_far"),
		java.util.Map.entry("minecraft:record_mall", "minecraft:music_disc_mall"),
		java.util.Map.entry("minecraft:record_mellohi", "minecraft:music_disc_mellohi"),
		java.util.Map.entry("minecraft:record_stal", "minecraft:music_disc_stal"),
		java.util.Map.entry("minecraft:record_strad", "minecraft:music_disc_strad"),
		java.util.Map.entry("minecraft:record_wait", "minecraft:music_disc_wait"),
		java.util.Map.entry("minecraft:record_ward", "minecraft:music_disc_ward"),
		java.util.Map.entry("minecraft:red_nether_brick", "minecraft:red_nether_bricks"),
		java.util.Map.entry("minecraft:reeds", "minecraft:sugar_cane"),
		java.util.Map.entry("minecraft:sign", "minecraft:oak_sign"),
		java.util.Map.entry("minecraft:slime", "minecraft:slime_block"),
		java.util.Map.entry("minecraft:snow_layer", "minecraft:snow"),
		java.util.Map.entry("minecraft:snow", "minecraft:snow_block"),
		java.util.Map.entry("minecraft:speckled_melon", "minecraft:glistering_melon_slice"),
		java.util.Map.entry("minecraft:stone_slab2", "minecraft:red_sandstone_slab"),
		java.util.Map.entry("minecraft:stone_stairs", "minecraft:cobblestone_stairs"),
		java.util.Map.entry("minecraft:trapdoor", "minecraft:oak_trapdoor"),
		java.util.Map.entry("minecraft:waterlily", "minecraft:lily_pad"),
		java.util.Map.entry("minecraft:web", "minecraft:cobweb"),
		java.util.Map.entry("minecraft:wooden_button", "minecraft:oak_button"),
		java.util.Map.entry("minecraft:wooden_door", "minecraft:oak_door"),
		java.util.Map.entry("minecraft:wooden_pressure_plate", "minecraft:oak_pressure_plate"),
		java.util.Map.entry("minecraft:yellow_flower", "minecraft:dandelion")
	)

	//TODO : Add mushroom block variants
	//i'll do it later because it isn't used and unlike the other, it's not just a rename or a separate, it's a separate and a merge
	fun convertItemId(id: String, damage: Int): String {
		return when (id) {
			"minecraft:anvil" -> ANVIL_VARIANTS[damage]
			"minecraft:coal" -> COAL_VARIANTS[damage]
			"minecraft:cobblestone_wall" -> COBBLESTONE_WALL_VARIANTS[damage]
			"minecraft:cooked_fish" -> COOKED_FISH_VARIANTS[damage]
			"minecraft:dirt" -> DIRT_VARIANTS[damage]
			"minecraft:double_plant" -> DOUBLE_PLANT_VARIANTS[damage]
			"minecraft:dye" -> DYE_VARIANTS[damage]
			"minecraft:fish" -> FISH_VARIANTS[damage]
			"minecraft:golden_apple" -> GOLDEN_APPLE_VARIANTS[damage]
			"minecraft:log" -> LOG_VARIANTS[damage]
			"minecraft:log2" -> LOG2_VARIANTS[damage]
			"minecraft:monster_egg" -> MONSTER_EGG_VARIANTS[damage]
			"minecraft:prismarine" -> PRISMARINE_VARIANTS[damage]
			"minecraft:quartz_block" -> QUARTZ_BLOCK_VARIANTS[damage]
			"minecraft:red_flower" -> RED_FLOWER_VARIANTS[damage]
			"minecraft:sand" -> SAND_VARIANTS[damage]
			"minecraft:skull" -> SKULL_VARIANTS[damage]
			"minecraft:sponge" -> SPONGE_VARIANTS[damage]
			"minecraft:stone" -> STONE_VARIANTS[damage]
			"minecraft:stone_slab" -> STONE_SLAB_VARIANTS[damage]
			"minecraft:stonebrick" -> STONEBRICK_VARIANTS[damage]
			"minecraft:tallgrass" -> TALLGRASS_VARIANTS[damage]
			"minecraft:spawn_egg" -> SPAWN_EGG_VARIANTS[damage]!!
			"minecraft:sandstone", "minecraft:red_sandstone" -> id.replaceFirst(":".toRegex(), SANDSTONE_VARIANTS[damage])
			"minecraft:banner" -> id.replaceFirst(":".toRegex(), COLOR_VARIANTS[15 - damage])
			"minecraft:carpet", "minecraft:stained_glass", "minecraft:stained_glass_pane", "minecraft:wool" -> id.replaceFirst(":".toRegex(), COLOR_VARIANTS[damage])
			"minecraft:stained_hardened_clay" -> id.replaceFirst(":stained_hardened_clay".toRegex(), COLOR_VARIANTS[damage]) + "terracotta"
			"minecraft:leaves", "minecraft:planks", "minecraft:sapling", "minecraft:wooden_slab" -> id.replaceFirst(":(?:wooden_)?".toRegex(), WOOD_VARIANTS[damage])
			"minecraft:leaves2" -> id.replaceFirst(":".toRegex(), WOOD_VARIANTS[damage + 4]).replaceFirst("2".toRegex(), "")
			else -> RENAMED.getOrDefault(id, id)
		}
	}
}
