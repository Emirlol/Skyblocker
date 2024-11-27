package de.hysky.skyblocker.skyblock.chocolatefactory.eggfinder;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.NotNull;

public final class Egg {
	@NotNull
	public final ArmorStandEntity entity;
	@NotNull
	public final Waypoint waypoint;
	@NotNull
	public final EggType type;
	@NotNull
	private EggState state = EggState.UNIDENTIFIED;
	private boolean seen = false;
	private boolean collected = false;

	public Egg(@NotNull ArmorStandEntity entity, @NotNull Waypoint waypoint, @NotNull EggType type) {
		this.entity = entity;
		this.waypoint = waypoint;
		this.type = type;
	}

	public boolean seen() {
		return seen;
	}

	public void seen(boolean seen) {
		this.seen = seen;
	}

	public boolean collected() {
		return collected;
	}

	public void collected(boolean collected) {
		this.collected = collected;
	}

	@NotNull
	public EggState getState() {
		return state;
	}

	public void setState(@NotNull EggState state) {
		this.state = state;
	}

	public boolean isOddDayEgg() {
		return state == EggState.ODD_DAY;
	}

	public boolean isEvenDayEgg() {
		return state == EggState.EVEN_DAY;
	}

	public boolean isUnidentified() {
		return state == EggState.UNIDENTIFIED;
	}

	public void markAsOddDayEgg() {
		this.state = EggState.ODD_DAY;
	}

	public void markAsEvenDayEgg() {
		this.state = EggState.EVEN_DAY;
	}

	@Override
	public String toString() {
		return "Type=" + type +
				", State=" + state +
				", Seen=" + seen +
				", Collected=" + collected
				;
	}
}
