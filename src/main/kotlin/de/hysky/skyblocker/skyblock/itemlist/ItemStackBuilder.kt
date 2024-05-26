package de.hysky.skyblocker.skyblock.itemlist

import de.hysky.skyblocker.utils.ItemUtils
import de.hysky.skyblocker.utils.ItemUtils.propertyMapWithTexture
import de.hysky.skyblocker.utils.NEURepoManager
import io.github.moulberry.repo.constants.PetNumbers
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Pair
import java.util.*
import java.util.regex.Pattern

object ItemStackBuilder {
	private val SKULL_UUID_PATTERN: Pattern = Pattern.compile("(?<=SkullOwner:\\{)Id:\"(.{36})\"")
	private val SKULL_TEXTURE_PATTERN: Pattern = Pattern.compile("(?<=Properties:\\{textures:\\[0:\\{Value:)\"(.+?)\"")
	private val COLOR_PATTERN: Pattern = Pattern.compile("color:(\\d+)")
	private val EXPLOSION_COLOR_PATTERN: Pattern = Pattern.compile("\\{Explosion:\\{(?:Type:[0-9a-z]+,)?Colors:\\[(?<color>[0-9]+)]\\}")
	private var petNums: Map<String, Map<Rarity, PetNumbers>>? = null

	fun loadPetNums() {
		try {
			petNums = NEURepoManager.NEU_REPO.constants.petNumbers
		} catch (e: Exception) {
			ItemRepository.LOGGER.error("Failed to load petnums.json")
		}
	}

	fun fromNEUItem(item: NEUItem): ItemStack {
		val internalName = item.skyblockItemId

		val injectors: List<Pair<String, String>> = ArrayList(petData(internalName))

		val legacyId = item.minecraftItemId
		val itemId = Identifier(ItemFixerUpper.convertItemId(legacyId, item.damage))

		val stack = ItemStack(Registries.ITEM[itemId])

		// Custom Data
		val customData = NbtCompound()

		// Add Skyblock Item Id
		customData.put(ItemUtils.ID, NbtString.of(internalName))

		// Item Name
		val name = injectData(item.displayName, injectors)
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(name))

		// Lore
		stack.set(DataComponentTypes.LORE, LoreComponent(item.lore.stream().map { line: String -> Text.of(injectData(line, injectors)) }.toList()))

		val nbttag = item.nbttag
		// add skull texture
		val skullUuid = SKULL_UUID_PATTERN.matcher(nbttag)
		val skullTexture = SKULL_TEXTURE_PATTERN.matcher(nbttag)
		if (skullUuid.find() && skullTexture.find()) {
			val uuid = UUID.fromString(skullUuid.group(1))
			val textureValue = skullTexture.group(1)

			stack.set(DataComponentTypes.PROFILE, ProfileComponent(Optional.of(internalName), Optional.of(uuid), propertyMapWithTexture(textureValue)))
		}

		// add leather armor dye color
		val colorMatcher = COLOR_PATTERN.matcher(nbttag)
		if (colorMatcher.find()) {
			val color = colorMatcher.group(1).toInt()
			stack.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color, false))
		}
		// add enchantment glint
		if (nbttag.contains("ench:")) {
			stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
		}

		//Hide weapon damage and other useless info
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent(listOf(), false))

		// Add firework star color
		val explosionColorMatcher = EXPLOSION_COLOR_PATTERN.matcher(nbttag)
		if (explosionColorMatcher.find()) {
			//Forget about the actual ball type because it probably doesn't matter
			stack.set(DataComponentTypes.FIREWORK_EXPLOSION, FireworkExplosionComponent(FireworkExplosionComponent.Type.SMALL_BALL, IntArrayList(explosionColorMatcher.group("color").toInt()), IntArrayList(), false, false))
		}

		// Attach custom nbt data
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData))

		return stack
	}

	private fun petData(internalName: String): List<Pair<String, String>> {
		val list: MutableList<Pair<String, String>> = ArrayList()

		val petName = internalName.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
		if (!internalName.contains(";") || !petNums!!.containsKey(petName)) return list

		val rarities = arrayOf(
			Rarity.COMMON,
			Rarity.UNCOMMON,
			Rarity.RARE,
			Rarity.EPIC,
			Rarity.LEGENDARY,
			Rarity.MYTHIC,
		)
		val rarity = rarities[internalName.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()]
		val data = petNums!![petName]!![rarity]

		val minLevel = data!!.lowLevel
		val maxLevel = data.highLevel
		list.add(Pair("\\{LVL\\}", "$minLevel ➡ $maxLevel"))

		val statNumsMin = data.statsAtLowLevel.statNumbers
		val statNumsMax = data.statsAtHighLevel.statNumbers
		val entrySet: Set<Map.Entry<String, Double>> = statNumsMin.entries
		for ((key) in entrySet) {
			val left = "\\{$key\\}"
			val right = statNumsMin[key].toString() + " ➡ " + statNumsMax[key]
			list.add(Pair(left, right))
		}

		val otherNumsMin = data.statsAtLowLevel.otherNumbers
		val otherNumsMax = data.statsAtHighLevel.otherNumbers
		for (i in otherNumsMin.indices) {
			val left = "\\{$i\\}"
			val right = otherNumsMin[i].toString() + " ➡ " + otherNumsMax[i]
			list.add(Pair(left, right))
		}

		return list
	}

	private fun injectData(string: String, injectors: List<Pair<String, String>>): String {
		var string = string
		for (injector in injectors) {
			string = string.replace(injector.left.toRegex(), injector.right)
		}
		return string
	}
}
