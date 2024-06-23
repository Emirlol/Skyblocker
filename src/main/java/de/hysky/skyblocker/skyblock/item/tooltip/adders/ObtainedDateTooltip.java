package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;

public class ObtainedDateTooltip extends TooltipAdder {
	private static final DateTimeFormatter OBTAINED_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneId.systemDefault()).localizedBy(Locale.ENGLISH);
	private static final DateTimeFormatter OLD_OBTAINED_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yy h:m a").withZone(ZoneId.of("UTC")).localizedBy(Locale.ENGLISH);

	public ObtainedDateTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (TooltipInfoType.OBTAINED.isTooltipEnabled()) {
			String timestamp = getTimestamp(stack);

			if (!timestamp.isEmpty()) {
				lines.add(Text.literal("Obtained:").formatted(Formatting.LIGHT_PURPLE)
				              .align(Text.literal(timestamp).formatted(Formatting.RED), 100));
			}
		}
	}

	/**
	 * This method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
	 * Currently, there are two types of string timestamps the legacy which is built like this
	 * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
	 * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
	 * taking into account of their formats, we do the same. The final result looks like this
	 * "MMMM dd, yyyy" (December 24, 2020).
	 * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
	 * "25/04/20 16:38" -> "January 04, 2022" instead of "April 25, 2020".
	 * This causes the museum rank to be much worse than it should be.
	 * <p>
	 * This also handles the long timestamp format introduced in January 2024 where the timestamp is in epoch milliseconds.
	 *
	 * @param stack the item under the pointer
	 * @return if the item have a "Timestamp" it will be shown formated on the tooltip
	 */
	public static String getTimestamp(ItemStack stack) {
		NbtCompound customData = ItemUtils.getCustomData(stack);

		if (customData != null && customData.contains("timestamp", NbtElement.LONG_TYPE)) {
			Instant date = Instant.ofEpochMilli(customData.getLong("timestamp"));
			return OBTAINED_DATE_FORMATTER.format(date);
		}

		if (customData != null && customData.contains("timestamp", NbtElement.STRING_TYPE)) {
			TemporalAccessor date = OLD_OBTAINED_DATE_FORMAT.parse(customData.getString("timestamp"));
			return OBTAINED_DATE_FORMATTER.format(date);
		}

		return "";
	}
}
