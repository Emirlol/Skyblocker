package de.hysky.skyblocker.utils.datafixer

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import net.minecraft.util.Util
import org.junit.jupiter.api.Assertions
import java.util.function.Consumer

class ItemStackComponentizationFixerTest {
	private val NBT: NbtCompound = convertToNbt("{id:\"minecraft:diamond_sword\",Count:1,tag:{ExtraAttributes:{id:\"TEST\"}}}")
	private val GSON = Gson()
	private val TEST_STACK: ItemStack = Util.make<ItemStack>(ItemStack(Items.DIAMOND_SWORD, 1), Consumer<ItemStack> { item: ItemStack ->
		val builder: ItemEnchantmentsComponent.Builder = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
		builder.add(Enchantments.SHARPNESS, 1)
		item.set<ItemEnchantmentsComponent>(DataComponentTypes.ENCHANTMENTS, builder.build())
	})

	@Test
	fun testNbtConversion() {
		Assertions.assertNotEquals(NBT, NbtCompound())
	}

	@Test
	fun testDataFixer() {
		val fixedStack: ItemStack? = ItemStackComponentizationFixer.fixUpItem(NBT)
		val stackJson: JsonElement = ItemStack.CODEC.encodeStart<JsonElement>(JsonOps.INSTANCE, fixedStack).getOrThrow()

		Assertions.assertEquals("{\"id\":\"minecraft:diamond_sword\",\"count\":1,\"components\":{\"minecraft:custom_data\":{\"ExtraAttributes\":{\"id\":\"TEST\"}}}}", GSON.toJson(stackJson))
	}

	@Test
	fun testComponentsAsString() {
		val componentString = ItemStackComponentizationFixer.componentsAsString(TEST_STACK)

		Assertions.assertEquals("[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]", componentString)
	}

	@Test
	fun testFromComponentsString() {
		val componentString = "[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]"
		val stack: ItemStack? = ItemStackComponentizationFixer.fromComponentsString("minecraft:diamond_sword", 1, componentString)

		Assertions.assertTrue(ItemStack.areItemsAndComponentsEqual(stack, TEST_STACK))
	}

	@Test
	fun testFromComponentsStringWithInvalidItem() {
		val componentString = "[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]"
		val stack: ItemStack? = ItemStackComponentizationFixer.fromComponentsString("minecraft:does_not_exist", 1, componentString)

		Assertions.assertEquals(stack, ItemStack.EMPTY)
	}

	@Test
	fun testNbtToComponentsString() {
		val fixedStack: ItemStack? = ItemStackComponentizationFixer.fixUpItem(NBT)
		val componentsString = ItemStackComponentizationFixer.componentsAsString(fixedStack)

		Assertions.assertEquals("[minecraft:custom_data={ExtraAttributes:{id:\"TEST\"}}]", componentsString)
	}

	companion object {
		@BeforeAll
		fun setup() {
			SharedConstants.createGameVersion()
			Bootstrap.initialize()
		}

		private fun convertToNbt(nbt: String): NbtCompound {
			return try {
				StringNbtReader.parse(nbt)
			} catch (e: Exception) {
				NbtCompound()
			}
		}
	}
}
