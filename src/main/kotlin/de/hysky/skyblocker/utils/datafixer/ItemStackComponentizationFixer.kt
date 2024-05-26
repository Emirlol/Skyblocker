package de.hysky.skyblocker.utils.datafixer

import com.mojang.brigadier.StringReader
import com.mojang.serialization.Dynamic
import net.minecraft.command.argument.ItemStringReader
import net.minecraft.component.DataComponentType
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.DynamicRegistryManager.ImmutableImpl
import net.minecraft.registry.Registries
import java.util.*
import java.util.List

/**
 * Contains a data fixer to convert legacy item NBT to the new components system, among other fixers related to the item components system.
 *
 * @see net.minecraft.datafixer.fix.ItemStackComponentizationFix
 */
object ItemStackComponentizationFixer {
	private const val ITEM_NBT_DATA_VERSION = 3817
	private const val ITEM_COMPONENTS_DATA_VERSION = 3825
	private val REGISTRY_MANAGER: DynamicRegistryManager = ImmutableImpl(List.of(Registries.ITEM, Registries.DATA_COMPONENT_TYPE))

	@JvmStatic
	fun fixUpItem(nbt: NbtCompound?): ItemStack {
		val dynamic = Schemas.getFixer().update(TypeReferences.ITEM_STACK, Dynamic(NbtOps.INSTANCE, nbt), ITEM_NBT_DATA_VERSION, ITEM_COMPONENTS_DATA_VERSION)

		return ItemStack.CODEC.parse(dynamic).getOrThrow()
	}

	/**
	 * Modified version of [net.minecraft.command.argument.ItemStackArgument.asString] to only care about changed components.
	 *
	 * @return The [ItemStack]'s components as a string which is in the format that the `/give` command accepts.
	 */
	@JvmStatic
	fun componentsAsString(stack: ItemStack?): String {
		val nbtRegistryOps = REGISTRY_MANAGER.getOps(NbtOps.INSTANCE)

		return stack!!.componentChanges.entrySet().stream().map { entry: Map.Entry<DataComponentType<*>, Optional<*>> ->
			val dataComponentType = entry.key as DataComponentType<Any>
			val componentId = Registries.DATA_COMPONENT_TYPE.getId(dataComponentType)
			val encodedComponent = dataComponentType.codec!!.encodeStart(nbtRegistryOps, entry.value.orElseThrow()).result()

			if (componentId == null || encodedComponent.isEmpty) {
				return@map null
			}
			componentId.toString() + "=" + encodedComponent.orElseThrow()
		}.filter { obj: String? -> Objects.nonNull(obj) }.toArray().contentToString()
	}

	/**
	 * Constructs an [ItemStack] from an `itemId`, with item components in string format as returned by [.componentsAsString], and with a specified stack count.
	 *
	 * @return an [ItemStack] or [ItemStack.EMPTY] if there was an exception thrown.
	 */
	@JvmStatic
	fun fromComponentsString(itemId: String, count: Int, componentsString: String): ItemStack {
		val reader = ItemStringReader(REGISTRY_MANAGER)

		try {
			val result = reader.consume(StringReader(itemId + componentsString))
			val stack = ItemStack(result.item(), count)

			stack.applyComponentsFrom(result.components())

			return stack
		} catch (ignored: Exception) {
		}

		return ItemStack.EMPTY
	}
}
