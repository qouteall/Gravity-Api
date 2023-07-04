package gravity_changer.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DirectionArgumentType implements ArgumentType<Direction> {
    
    public static final DirectionArgumentType instance = new DirectionArgumentType();
    
    public static final DynamicCommandExceptionType exceptionType =
        new DynamicCommandExceptionType(object ->
            Component.literal("Invalid Direction " + object)
        );
    
    public static Direction getDirection(CommandContext<?> context, String direction) {
        return context.getArgument(direction, Direction.class);
    }
    
    @Override
    public Direction parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString().toLowerCase();
        return switch (s) {
            case "up", "u" -> Direction.UP;
            case "down", "d" -> Direction.DOWN;
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            default -> throw exceptionType.createWithContext(reader, s);
        };
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
            Arrays.stream(Direction.values())
                .map(d -> d.name().toLowerCase())
                .collect(Collectors.toList()),
            builder
        );
    }
    
    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(Direction.Axis.values())
            .map(Enum::toString).collect(Collectors.toList());
    }
    
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(
            new ResourceLocation("gravity_changer:direction"),
            DirectionArgumentType.class,
            SingletonArgumentInfo.contextFree(() -> DirectionArgumentType.instance)
        );
    }
}
