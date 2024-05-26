package de.hysky.skyblocker.skyblock.dungeon.terminal

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.gui.ColorHighlight
import de.hysky.skyblocker.utils.render.gui.ColorHighlight.Companion.green
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.util.function.Predicate

class StartsWithTerminal : ContainerSolver("^What starts with: '([A-Z])'\\?$"), TerminalSolver {
	private val trackedItemStates = Int2ObjectOpenHashMap<ItemState>()
	private var lastKnownScreenId = Int.MIN_VALUE

	override val isEnabled: Boolean
		get() = SkyblockerConfigManager.get().dungeons.terminals.solveStartsWith

	protected override fun getColors(groups: Array<String?>?, slots: Int2ObjectMap<ItemStack?>?): List<ColorHighlight?>? {
		trimEdges(slots!!, 6)
		setupState(slots)

		val prefix = groups!![0]
		val highlights: MutableList<ColorHighlight?> = ArrayList()

		for (slot in slots.int2ObjectEntrySet()) {
			val stack = slot.value
			val state = trackedItemStates.getOrDefault(slot.intKey, ItemState.DEFAULT)

			//If the item hasn't been marked as clicked and it matches the starts with condition
			//We keep track of the clicks ourselves instead of using the enchantment glint because some items like nether stars have the glint override component by default
			//so even if Hypixel tries to change that to the same thing it was before (true) it won't work and the solver would permanently consider the item to be clicked
			//even if it hasn't been yet
			if (!state.clicked && stack!!.name.string.startsWith(prefix!!)) {
				highlights.add(green(slot.intKey))
			}
		}
		return highlights
	}

	protected override fun onClickSlot(slot: Int, stack: ItemStack?, screenId: Int, groups: Array<String?>?): Boolean {
		//Some random glass pane was clicked or something
		if (!trackedItemStates.containsKey(slot) || stack == null || stack.isEmpty) return false

		val state = trackedItemStates[slot]
		val prefix = groups!![0]

		//If the item stack's name starts with the correct letter
		//Also, since Hypixel closes & reopens the GUI after every click we check if the last known screen id is the same that way in case the server lags and
		//either a player tries to click a second item or if the player puts the clicked item back and tries to click another that we don't mark multiple items
		//as clicked when only the first one will count.

		//While Hypixel does use a different syncId each time they open the screen we opt to use our own so as to avoid them potentially changing that
		//and in turn breaking this logic
		if (stack.name.string.startsWith(prefix!!) && !state.clicked && lastKnownScreenId != screenId) {
			trackedItemStates.put(slot, state.click())
			lastKnownScreenId = screenId
		} else {
			return shouldBlockIncorrectClicks()
		}

		return false
	}

	//We only setup the state when all items aren't null or empty. This prevents the state from being reset due to unsent items or server lag spikes/bad TPS (fix ur servers Hypixel)
	private fun setupState(usefulSlots: Int2ObjectMap<ItemStack?>?) {
		val notNullOrEmpty = Predicate { e: Int2ObjectMap.Entry<ItemStack?> -> e.value != null && !e.value!!.isEmpty }

		if (allEntriesMatch(usefulSlots!!.int2ObjectEntrySet(), notNullOrEmpty)) {
			//If the state hasn't been setup then we will do that
			if (trackedItemStates.isEmpty()) {
				for (entry in usefulSlots.int2ObjectEntrySet()) {
					trackedItemStates.put(entry.intKey, ItemState.of(entry.value!!.item))
				}
			} else { //If the state is setup then we verify that it hasn't changed since last time, and if it has then we will clear it and call this method again to set it up
				//Checks whether the trackedItemStates contains the slot id and if it does it checks whether the tracked state's item is a 1:1 match
				val doesItemMatch = Predicate { e: Int2ObjectMap.Entry<ItemStack?> -> trackedItemStates.containsKey(e.intKey) && trackedItemStates[e.intKey].itemMatches(e.value!!.item) }

				if (!allEntriesMatch(usefulSlots.int2ObjectEntrySet(), doesItemMatch)) {
					clearState()
					setupState(usefulSlots)
				}
			}
		}
	}

	private fun clearState() {
		trackedItemStates.clear()
		lastKnownScreenId = Int.MIN_VALUE
	}

	@JvmRecord
	private data class ItemState(val item: Item?, val clicked: Boolean) {
		fun itemMatches(item: Item): Boolean {
			return this.item == item
		}

		fun click(): ItemState {
			return ItemState(item, true)
		}

		companion object {
			val DEFAULT: ItemState = ItemState(null, false)

			fun of(item: Item?): ItemState {
				return ItemState(item, false)
			}
		}
	}

	companion object {
		private fun allEntriesMatch(entries: ObjectSet<Int2ObjectMap.Entry<ItemStack?>>, predicate: Predicate<Int2ObjectMap.Entry<ItemStack?>>): Boolean {
			for (entry in entries) {
				if (!predicate.test(entry)) return false
			}

			return true
		}
	}
}