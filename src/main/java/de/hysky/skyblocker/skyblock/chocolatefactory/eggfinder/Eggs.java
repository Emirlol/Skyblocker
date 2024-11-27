package de.hysky.skyblocker.skyblock.chocolatefactory.eggfinder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * <p>A list-like class that holds the eggs for a location.</p>
 * <p>
 * Normally, it will try to guess the state of the egg based on the other egg of the same type.
 * If the existing egg of the same type is unidentified, the new egg will also be unidentified.
 * </p>
 */
public class Eggs {
	@Nullable
	public Egg breakfast = null; // oddDay, orange
	@Nullable
	public Egg lunch = null; // oddDay, blue
	@Nullable
	public Egg dinner = null; // oddDay, green
	@Nullable
	public Egg brunch = null; // evenDay, orange
	@Nullable
	public Egg dejeuner = null; // evenDay, blue
	@Nullable
	public Egg supper = null; // evenDay, green

	/**
	 * <p>Adds an egg to the list of eggs.</p>
	 * <p>If the egg is unidentified, it will be added to the first empty slot for that egg type.</p>
	 * <p>If other slot for that egg is identified, the other egg's state will be set accordingly.</p>
	 */
	@SuppressWarnings("DuplicatedCode")
	public void add(Egg egg) {
		switch (egg.type) {
			case ORANGE -> {
				switch (egg.getState()) {
					case ODD_DAY -> {
						breakfast = egg;
						if (brunch != null && brunch.isUnidentified()) brunch.setState(EggState.EVEN_DAY);
					}
					case EVEN_DAY -> {
						brunch = egg;
						if (breakfast != null && breakfast.isUnidentified()) breakfast.setState(EggState.ODD_DAY);
					}
					case UNIDENTIFIED -> {
						if (breakfast == null && brunch != null) {
							breakfast = egg;
							if (brunch.isOddDayEgg()) breakfast.setState(EggState.EVEN_DAY);
							else if (brunch.isEvenDayEgg()) breakfast.setState(EggState.ODD_DAY);
						} else if (brunch == null && breakfast != null) {
							brunch = egg;
							if (breakfast.isOddDayEgg()) brunch.setState(EggState.EVEN_DAY);
							else if (breakfast.isEvenDayEgg()) brunch.setState(EggState.ODD_DAY);
						} else if (breakfast == null) {
							breakfast = egg; // Default to breakfast
						} else { // Brunch == null -> true by this point
							brunch = egg; // Fill the remaining slot in case breakfast is already filled and unidentified.
						}
					}
				}
			}
			case BLUE -> {
				switch (egg.getState()) {
					case ODD_DAY -> {
						lunch = egg;
						if (dejeuner != null && dejeuner.isUnidentified()) dejeuner.setState(EggState.EVEN_DAY);
					}
					case EVEN_DAY -> {
						dejeuner = egg;
						if (lunch != null && lunch.isUnidentified()) lunch.setState(EggState.ODD_DAY);
					}
					case UNIDENTIFIED -> {
						if (lunch == null && dejeuner != null) {
							lunch = egg;
							if (dejeuner.isOddDayEgg()) lunch.setState(EggState.EVEN_DAY);
							else if (dejeuner.isEvenDayEgg()) lunch.setState(EggState.ODD_DAY);
						} else if (dejeuner == null && lunch != null) {
							dejeuner = egg;
							if (lunch.isOddDayEgg()) dejeuner.setState(EggState.EVEN_DAY);
							else if (lunch.isEvenDayEgg()) dejeuner.setState(EggState.ODD_DAY);
						} else if (lunch == null) {
							lunch = egg; // Default to lunch
						} else { // Dejeuner == null -> true by this point
							dejeuner = egg; // Fill the remaining slot in case lunch is already filled and unidentified.
						}
					}
				}
			}
			case GREEN -> {
				switch (egg.getState()) {
					case ODD_DAY -> {
						dinner = egg;
						if (supper != null && supper.isUnidentified()) supper.setState(EggState.EVEN_DAY);
					}
					case EVEN_DAY -> {
						supper = egg;
						if (dinner != null && dinner.isUnidentified()) dinner.setState(EggState.ODD_DAY);
					}
					case UNIDENTIFIED -> {
						if (dinner == null && supper != null) {
							dinner = egg;
							if (supper.isOddDayEgg()) dinner.setState(EggState.EVEN_DAY);
							else if (supper.isEvenDayEgg()) dinner.setState(EggState.ODD_DAY);
						} else if (supper == null && dinner != null) {
							supper = egg;
							if (dinner.isOddDayEgg()) supper.setState(EggState.EVEN_DAY);
							else if (dinner.isEvenDayEgg()) supper.setState(EggState.ODD_DAY);
						} else if (dinner == null) {
							dinner = egg; // Default to dinner.
						} else { // Supper == null -> true by this point
							supper = egg; // Fill the remaining slot in case dinner is already filled and unidentified.
						}
					}
				}
			}
		}
	}

	public void remove(EggType type, EggState state) {
		switch (type) {
			case ORANGE -> {
				switch (state) {
					case ODD_DAY -> {
						if (breakfast != null) {
							if (breakfast.isOddDayEgg()) {
								breakfast = null;
								if (brunch != null) brunch.setState(EggState.EVEN_DAY);
							}
						}
					}
					case EVEN_DAY -> brunch = null;
				}
			}
			case BLUE -> {
				switch (state) {
					case ODD_DAY -> lunch = null;
					case EVEN_DAY -> dejeuner = null;
				}
			}
			case GREEN -> {
				switch (state) {
					case ODD_DAY -> dinner = null;
					case EVEN_DAY -> supper = null;
				}
			}
		}
	}

	/**
	 * @return A stream containing all not-null eggs.
	 */
	@NotNull
	public Stream<@NotNull Egg> stream() {
		return Stream.of(breakfast, lunch, dinner, brunch, dejeuner, supper).filter(Objects::nonNull);
	}

	public void forEachNotNull(@NotNull Consumer<Egg> consumer) {
		if (breakfast != null) consumer.accept(breakfast);
		if (lunch != null) consumer.accept(lunch);
		if (dinner != null) consumer.accept(dinner);
		if (brunch != null) consumer.accept(brunch);
		if (dejeuner != null) consumer.accept(dejeuner);
		if (supper != null) consumer.accept(supper);
	}
}
