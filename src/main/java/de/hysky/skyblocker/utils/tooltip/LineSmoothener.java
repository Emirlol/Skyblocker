package de.hysky.skyblocker.utils.tooltip;

import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class LineSmoothener extends TooltipAdder {
	//This is static to not create a new text object for each line in every item
	private static final Text BUMPY_LINE = Text.literal("-----------------").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);

	public static Text createSmoothLine() {
		return Text.literal("                    ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH, Formatting.BOLD);
	}

	protected LineSmoothener() {
		super(Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(List<Text> lore, Slot focusedSlot) {
		for (int i = 0; i < lore.size(); i++) {
			List<Text> lineSiblings = lore.get(i).getSiblings();
			//Compare the first sibling rather than the whole object as the style of the root object can change while visually staying the same
			if (lineSiblings.size() == 1 && lineSiblings.getFirst().equals(BUMPY_LINE)) {
				lore.set(i, createSmoothLine());
			}
		}
	}
}