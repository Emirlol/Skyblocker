package de.hysky.skyblocker.skyblock.item

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeEntry
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.book.RecipeBookCategory
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.time.Duration

class SkyblockCraftingTableScreen(handler: SkyblockCraftingTableScreenHandler?, inventory: PlayerInventory?, title: Text?) : HandledScreen<SkyblockCraftingTableScreenHandler?>(handler, inventory, title) {
	private val recipeBook = ItemListWidget()
	private var narrow = false
	private var moreCraftsButton: TexturedButtonWidget? = null

	init {
		this.backgroundWidth += 22
	}

	override fun init() {
		super.init()
		this.narrow = this.width < 379
		recipeBook.initialize(this.width, this.height, client!!, this.narrow, DummyRecipeScreenHandler())
		this.x = recipeBook.findLeftEdge(this.width, this.backgroundWidth) + 11
		this.addDrawableChild(TexturedButtonWidget(this.x + 5, this.height / 2 - 49, 20, 18, RecipeBookWidget.BUTTON_TEXTURES) { button: ButtonWidget ->
			recipeBook.toggleOpen()
			this.x = recipeBook.findLeftEdge(this.width, this.backgroundWidth) + 11
			button.setPosition(this.x + 5, this.height / 2 - 49)
			if (moreCraftsButton != null) moreCraftsButton!!.setPosition(this.x + 174, this.y + 62)
		})
		if (!handler!!.mirrorverse) {
			moreCraftsButton = TexturedButtonWidget(
				this.x + 174, y + 62, 16, 16, MORE_CRAFTS_TEXTURES
			) { button: ButtonWidget? -> this.onMouseClick(handler!!.slots[26], handler!!.slots[26].id, 0, SlotActionType.PICKUP) }
			moreCraftsButton!!.setTooltipDelay(Duration.ofMillis(250L))
			moreCraftsButton!!.tooltip = Tooltip.of(Text.literal("More Crafts"))
			this.addDrawableChild(moreCraftsButton)
		}
		checkNotNull((if (client != null) client!!.player else null))
		client!!.player!!.currentScreenHandler = handler // recipe book replaces it with the Dummy one fucking DUMBASS
		this.addSelectableChild(this.recipeBook)
		this.setInitialFocus(this.recipeBook)
		this.titleX = 29
	}

	public override fun handledScreenTick() {
		super.handledScreenTick()
		recipeBook.update()
		if (moreCraftsButton == null) return
		val stack = handler!!.slots[26].stack
		moreCraftsButton!!.active = stack.isEmpty || stack.isOf(Items.PLAYER_HEAD)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (recipeBook.isOpen && this.narrow) {
			this.renderBackground(context, mouseX, mouseY, delta)
			recipeBook.render(context, mouseX, mouseY, delta)
		} else {
			super.render(context, mouseX, mouseY, delta)
			recipeBook.render(context, mouseX, mouseY, delta)
			recipeBook.drawGhostSlots(context, this.x, this.y, true, delta)
		}
		this.drawMouseoverTooltip(context, mouseX, mouseY)
		recipeBook.drawTooltip(context, this.x, this.y, mouseX, mouseY)
	}


	override fun drawSlot(context: DrawContext, slot: Slot) {
		if (slot.id == 23 && slot.stack.isOf(Items.BARRIER)) return
		super.drawSlot(context, slot)
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		val i = this.x
		val j = (this.height - this.backgroundHeight) / 2
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight)
		if (!handler!!.mirrorverse) context.drawGuiTexture(QUICK_CRAFT, i + 173, j, 0, 25, 84)
	}

	override fun isPointWithinBounds(x: Int, y: Int, width: Int, height: Int, pointX: Double, pointY: Double): Boolean {
		return (!this.narrow || !recipeBook.isOpen) && super.isPointWithinBounds(x, y, width, height, pointX, pointY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
			this.focused = this.recipeBook
			return true
		}
		if (this.narrow && recipeBook.isOpen) {
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, left: Int, top: Int, button: Int): Boolean {
		val bl = (mouseX < left.toDouble() || mouseY < top.toDouble() || mouseX >= (left + this.backgroundWidth).toDouble()) || mouseY >= (top + this.backgroundHeight).toDouble()
		return recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl
	}

	override fun onMouseClick(slot: Slot, slotId: Int, button: Int, actionType: SlotActionType) {
		super.onMouseClick(slot, slotId, button, actionType)
		recipeBook.slotClicked(slot)
	}


	internal class DummyRecipeScreenHandler : AbstractRecipeScreenHandler<SimpleInventory?>(ScreenHandlerType.GENERIC_9X6, -69) {
		override fun populateRecipeFinder(finder: RecipeMatcher) {}

		override fun clearCraftingSlots() {}

		override fun matches(recipe: RecipeEntry<out Recipe<SimpleInventory?>?>): Boolean {
			return false
		}

		override fun getCraftingResultSlotIndex(): Int {
			return 0
		}

		override fun getCraftingWidth(): Int {
			return 0
		}

		override fun getCraftingHeight(): Int {
			return 0
		}

		override fun getCraftingSlotCount(): Int {
			return 0
		}

		override fun getCategory(): RecipeBookCategory {
			return RecipeBookCategory.CRAFTING
		}

		override fun canInsertIntoSlot(index: Int): Boolean {
			return false
		}

		override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
			return ItemStack.EMPTY
		}

		override fun canUse(player: PlayerEntity): Boolean {
			return false
		}
	}

	companion object {
		private val TEXTURE = Identifier("textures/gui/container/crafting_table.png")
		protected val MORE_CRAFTS_TEXTURES: ButtonTextures = ButtonTextures(
			Identifier(SkyblockerMod.NAMESPACE, "quick_craft/more_button"),
			Identifier(SkyblockerMod.NAMESPACE, "quick_craft/more_button_disabled"),
			Identifier(SkyblockerMod.NAMESPACE, "quick_craft/more_button_highlighted")
		)

		protected val QUICK_CRAFT: Identifier = Identifier(SkyblockerMod.NAMESPACE, "quick_craft/quick_craft_overlay")
	}
}
