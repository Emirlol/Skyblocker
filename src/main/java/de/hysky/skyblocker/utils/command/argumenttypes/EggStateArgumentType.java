package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.hysky.skyblocker.skyblock.chocolatefactory.eggfinder.EggState;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EggStateArgumentType implements ArgumentType<EggState> {
	@Override
	public EggState parse(StringReader reader) throws CommandSyntaxException {
		String s = reader.readUnquotedString();
		for (EggState eggState : EggState.values()) {
			if (eggState.name().equalsIgnoreCase(s)) {
				return eggState;
			}
		}
		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(Stream.of(EggState.values()).map(Enum::name), builder);
	}

	public static EggState getEggState(CommandContext<?> context, String name) {
		return context.getArgument(name, EggState.class);
	}

	public static EggStateArgumentType eggState() {
		return new EggStateArgumentType();
	}
}
