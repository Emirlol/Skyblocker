package de.hysky.skyblocker.skyblock.chocolatefactory.eggfinder;

import java.util.function.Consumer;

public record EggConsumer(EggType type, Consumer<Egg> consumer) {}
