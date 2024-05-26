package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import de.hysky.skyblocker.utils.ItemUtils.getLore
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import java.util.*

class FinderSettingsContainer(x: Int, y: Int, height: Int) : ContainerWidget(x, y, 336, height, Text.empty()) {
	private var isInitialized = false
	private var floorSelector: OptionDropdownWidget? = null
	private var dungeonTypeSelector: OptionDropdownWidget? = null
	private var sortGroupsSelector: OptionDropdownWidget? = null

	private var classLevelRange: RangedValueWidget? = null
	private var dungeonLevelRange: RangedValueWidget? = null

	private var currentlyOpenedOption: ContainerWidget? = null

	private val initializedWidgets: MutableList<ContainerWidget> = ArrayList()


	override fun setDimensionsAndPosition(width: Int, height: Int, x: Int, y: Int) {
		super.setDimensionsAndPosition(width, height, x, y)
		if (this.floorSelector != null) floorSelector!!.setPosition(x + width / 4 - 70, y + 20)
		if (this.dungeonTypeSelector != null) dungeonTypeSelector!!.setPosition(x + 3 * width / 4 - 70, y + 20)
		if (this.sortGroupsSelector != null) sortGroupsSelector!!.setPosition(x + width / 2 - 70, y + 120)
		if (this.classLevelRange != null) classLevelRange!!.setPosition(x + width / 4 - 50, y + 70)
		if (this.dungeonLevelRange != null) dungeonLevelRange!!.setPosition(x + 3 * width / 4 - 50, y + 70)
	}

	/**
	 * Handles everything in the Settings page
	 * @param screen the parent Party Finder screen
	 * @param inventoryName le inventory name
	 * @return returns false if it doesn't know what's happening
	 */
	fun handle(screen: PartyFinderScreen, inventoryName: String): Boolean {
		val nameLowerCase = inventoryName.lowercase(Locale.getDefault())
		val handler = screen.handler
		if (!isInitialized) {
			if (!nameLowerCase.contains("search settings")) return false
			isInitialized = true
			//System.out.println("initializing");
			for (slot in handler!!.slots) {
				if (slot.id > handler.rows * 9 - 1) break
				if (!slot.hasStack()) continue
				val stack = slot.stack
				//System.out.println(stack.toString());
				val name = stack.name.string.lowercase(Locale.getDefault())
				if (name.contains("floor")) {
					//System.out.println("Floor selector created");

					this.floorSelector = OptionDropdownWidget(screen, stack.name, null, x + getWidth() / 4 - 70, y + 20, 140, 170, slot.id)
					if (!setSelectedElementFromTooltip(slot, stack, floorSelector!!)) return false

					initializedWidgets.add(floorSelector!!)
				} else if (name.contains("dungeon type")) {
					this.dungeonTypeSelector = OptionDropdownWidget(screen, stack.name, null, x + (3 * getWidth()) / 4 - 70, y + 20, 140, 100, slot.id)
					if (!setSelectedElementFromTooltip(slot, stack, dungeonTypeSelector!!)) return false

					initializedWidgets.add(dungeonTypeSelector!!)
				} else if (name.contains("groups")) {
					this.sortGroupsSelector = OptionDropdownWidget(screen, stack.name, null, x + getWidth() / 2 - 70, y + 120, 140, 100, slot.id)
					if (!setSelectedElementFromTooltip(slot, stack, sortGroupsSelector!!)) return false

					initializedWidgets.add(sortGroupsSelector!!)
				} else if (name.contains("class level")) {
					this.classLevelRange = RangedValueWidget(screen, stack.name, x + getWidth() / 4 - 50, y + 70, 100, slot.id)
					if (!setRangeFromTooltip(stack, classLevelRange!!)) return false

					initializedWidgets.add(classLevelRange!!)
				} else if (name.contains("dungeon level")) {
					this.dungeonLevelRange = RangedValueWidget(screen, stack.name, x + 3 * (getWidth()) / 4 - 50, y + 70, 100, slot.id)
					if (!setRangeFromTooltip(stack, dungeonLevelRange!!)) return false

					initializedWidgets.add(dungeonLevelRange!!)
				}
			}
		}
		if (nameLowerCase.contains("search settings")) {
			if (floorSelector != null) floorSelector!!.close()
			if (dungeonTypeSelector != null) dungeonTypeSelector!!.close()
			if (sortGroupsSelector != null) sortGroupsSelector!!.close()
			if (classLevelRange != null) classLevelRange!!.setState(RangedValueWidget.State.CLOSED)
			if (dungeonLevelRange != null) dungeonLevelRange!!.setState(RangedValueWidget.State.CLOSED)

			screen.partyFinderButton!!.active = true
			currentlyOpenedOption = null

			for (i in (handler!!.rows - 1) * 9 until (handler.rows * 9)) {
				val slot = handler.slots[i]
				if (slot.hasStack() && slot.stack.isOf(Items.ARROW)) {
					screen.partyButtonSlotId = slot.id
				}
			}
			return true
		} else {
			screen.partyFinderButton!!.active = false

			if (nameLowerCase.contains("floor")) {
				updateDropdownOptionWidget(handler, floorSelector)
				currentlyOpenedOption = floorSelector
				return true
			} else if (nameLowerCase.contains("select type")) {
				updateDropdownOptionWidget(handler, dungeonTypeSelector)
				currentlyOpenedOption = dungeonTypeSelector
				return true
			} else if (nameLowerCase.contains("class level range")) {
				updateRangedValue(handler, classLevelRange)
				return true
			} else if (nameLowerCase.contains("dungeon level range")) {
				updateRangedValue(handler, dungeonLevelRange)
				return true
			} else if (nameLowerCase.contains("sort")) {
				updateDropdownOptionWidget(handler, sortGroupsSelector)
				currentlyOpenedOption = sortGroupsSelector
				return true
			}
		}
		return false
	}

	private fun findBackSlotId(handler: GenericContainerScreenHandler?): Int {
		var backId = -1
		for (i in (handler!!.rows - 1) * 9 until (handler.rows * 9)) {
			val slot = handler.slots[i]
			if (slot.hasStack() && slot.stack.isOf(Items.ARROW)) {
				backId = slot.id
				break
			}
		}
		return backId
	}

	/**
	 * @return true if all goes well
	 */
	private fun setRangeFromTooltip(stack: ItemStack, widget: RangedValueWidget): Boolean {
		for (text in getLore(stack)) {
			val textLowerCase = text.string.lowercase(Locale.getDefault())
			if (textLowerCase.contains("selected:")) {
				val split = text.string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (split.size < 2) return false
				val minAndMax = split[1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (minAndMax.size < 2) return false
				//System.out.println(textLowerCase);
				//System.out.println("Min and max: " + minAndMax[0] + " " + minAndMax[1]);
				var leMin = -1
				var leMax = -1
				try {
					leMin = minAndMax[0].trim { it <= ' ' }.toInt()
				} catch (ignored: NumberFormatException) {
				}
				try {
					leMax = minAndMax[1].trim { it <= ' ' }.toInt()
				} catch (ignored: NumberFormatException) {
				}

				widget.setMinAndMax(leMin, leMax)
				return true
			}
		}
		return false
	}

	/**
	 * @return true if all goes well
	 */
	private fun setSelectedElementFromTooltip(slot: Slot, stack: ItemStack, dropdownWidget: OptionDropdownWidget): Boolean {
		for (text in getLore(stack)) {
			val textLowerCase = text.string.lowercase(Locale.getDefault())
			if (textLowerCase.contains("selected:")) {
				val split = text.string.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (split.size < 2) return false
				val floorName = split[1].trim { it <= ' ' }
				dropdownWidget.setSelectedOption(dropdownWidget.Option(floorName, stack, slot.id))
				return true
			}
		}
		return false
	}

	fun handleSign(sign: SignBlockEntity, front: Boolean): Boolean {
		if (!isInitialized) return false
		if (currentlyOpenedOption === classLevelRange) {
			return updateValues(sign, front, classLevelRange)
		} else if (currentlyOpenedOption === dungeonLevelRange) {
			return updateValues(sign, front, dungeonLevelRange)
		}
		return false
	}

	private fun updateValues(sign: SignBlockEntity, front: Boolean, valueWidget: RangedValueWidget?): Boolean {
		val state: RangedValueWidget.State
		val lowerCase = sign.getText(front).getMessage(3, false).string.lowercase(Locale.getDefault())
		state = if (lowerCase.contains("max")) {
			RangedValueWidget.State.MODIFYING_MAX
		} else if (lowerCase.contains("min")) {
			RangedValueWidget.State.MODIFYING_MIN
		} else return false
		valueWidget!!.setState(state)
		this.focused = valueWidget
		return true
	}

	private fun updateDropdownOptionWidget(handler: GenericContainerScreenHandler?, dropdownWidget: OptionDropdownWidget?) {
		val entries: MutableList<OptionDropdownWidget.Option> = ArrayList()
		for (slot in handler!!.slots) {
			if (slot.id > (handler.rows - 1) * 9 - 1) break
			if (slot.hasStack() && !slot.stack.isOf(Items.BLACK_STAINED_GLASS_PANE)) {
				entries.add(dropdownWidget!!.Option(slot.stack.name.string, slot.stack, slot.id))
			}
		}
		val backId = findBackSlotId(handler)
		dropdownWidget!!.open(entries, backId)
	}

	private fun updateRangedValue(handler: GenericContainerScreenHandler?, valueWidget: RangedValueWidget?) {
		currentlyOpenedOption = valueWidget
		var min = -1
		var max = -1
		for (slot in handler!!.slots) {
			if (slot.id > (handler.rows - 1) * 9 - 1) break
			if (slot.hasStack() && slot.stack.name.string.lowercase(Locale.getDefault()).contains("min")) {
				min = slot.id
			} else if (slot.hasStack() && slot.stack.name.string.lowercase(Locale.getDefault()).contains("max")) {
				max = slot.id
			}
		}
		val backId = findBackSlotId(handler)

		valueWidget!!.setStateAndSlots(RangedValueWidget.State.OPEN, min, max, backId)
	}

	var visible: Boolean
		get() = super.visible
		set(visible) {
			this.visible = visible
			if (floorSelector != null) floorSelector!!.visible = visible
			if (dungeonTypeSelector != null) dungeonTypeSelector!!.visible = visible
			if (classLevelRange != null) classLevelRange!!.visible = visible
			if (dungeonLevelRange != null) dungeonLevelRange!!.visible = visible
			if (sortGroupsSelector != null) sortGroupsSelector!!.visible = visible
		}

	fun canInteract(widget: ContainerWidget?): Boolean {
		return currentlyOpenedOption == null || currentlyOpenedOption === widget
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		for (initializedWidget in initializedWidgets) {
			initializedWidget.render(context, mouseX, mouseY, delta)
		}
	}

	override fun children(): List<Element> {
		return initializedWidgets
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {}
}
