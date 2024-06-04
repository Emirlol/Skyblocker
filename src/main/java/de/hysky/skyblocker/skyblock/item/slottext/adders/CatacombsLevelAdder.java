package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//This class is split into 3 inner classes as there are multiple screens for showing catacombs levels, each with different slot ids or different style of showing the level.
//It's still kept in 1 main class for organization purposes.
public class CatacombsLevelAdder {
	private CatacombsLevelAdder() {
	}

	public static class Dungeoneering extends SlotTextAdder {
		public Dungeoneering() {
			super("^Dungeoneering");
		}

		@Override
		public @NotNull List<PositionedText> getText(Slot slot) {
			switch (slot.id) {
				case 12, 29, 30, 31, 32, 33 -> {
					String name = slot.getStack().getName().getString();
					int lastIndex = name.lastIndexOf(' ');
					if (lastIndex == -1) return List.of(PositionedText.BOTTOM_LEFT(Text.literal("0").formatted(Formatting.RED)));
					String level = name.substring(lastIndex + 1);
					if (!NumberUtils.isDigits(level)) return List.of(); //Sanity check, just in case.
					return List.of(PositionedText.BOTTOM_RIGHT(Text.literal(level).formatted(Formatting.RED)));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static class DungeonClasses extends SlotTextAdder {

		public DungeonClasses() {
			super("^Dungeon Classes"); //Applies to both screens as they are same in both the placement and the style of the level text.
		}

		@Override
		public @NotNull List<PositionedText> getText(Slot slot) {
			switch (slot.id) {
				case 11, 12, 13, 14, 15 -> {
					String level = getBracketedLevelFromName(slot.getStack());
					if (!NumberUtils.isDigits(level)) return List.of();
					return List.of(PositionedText.BOTTOM_LEFT(Text.literal(level).formatted(Formatting.RED)));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static class ReadyUp extends SlotTextAdder {

		public ReadyUp() {
			super("^Ready Up");
		}

		@Override
		public @NotNull List<PositionedText> getText(Slot slot) {
			switch (slot.id) {
				case 29, 30, 31, 32, 33 -> {
					String level = getBracketedLevelFromName(slot.getStack());
					if (!NumberUtils.isDigits(level)) return List.of();
					return List.of(PositionedText.BOTTOM_LEFT(Text.literal(level).formatted(Formatting.RED)));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static String getBracketedLevelFromName(ItemStack itemStack) {
		String name = itemStack.getName().getString();
		if (!name.startsWith("[Lvl ")) return null;
		int index = name.indexOf(']');
		if (index == -1) return null;
		return name.substring(5, index);
	}
}