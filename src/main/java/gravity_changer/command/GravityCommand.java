package gravity_changer.command;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.GCUtil;
import gravity_changer.util.RotationUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.Collection;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class GravityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("gravity");
        
        builder.then(Commands.literal("set_base_direction")
            .then(Commands.argument("entities", EntityArgument.entities())
                .then(Commands.argument("direction", DirectionArgumentType.instance)
                    .executes(context -> {
                        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entities");
                        Direction direction = DirectionArgumentType.getDirection(context, "direction");
                        for (Entity entity : entities) {
                            GravityChangerAPI.setBaseGravityDirection(entity, direction);
                        }
                        return entities.size();
                    })
                )
            )
        );
        
        builder.then(Commands.literal("reset")
            .then(Commands.argument("entities", EntityArgument.entities())
                .executes(context -> {
                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entities");
                    for (Entity entity : entities) {
                        GravityChangerAPI.resetGravity(entity);
                    }
                    return entities.size();
                })
            )
        );
        
        builder.then(Commands.literal("set_base_strength")
            .then(Commands.argument("entities", EntityArgument.entities())
                .then(Commands.argument("strength", DoubleArgumentType.doubleArg(-20, 20))
                    .executes(context -> {
                        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entities");
                        double strength = DoubleArgumentType.getDouble(context, "strength");
                        for (Entity entity : entities) {
                            GravityChangerAPI.setBaseGravityStrength(entity, strength);
                        }
                        return entities.size();
                    })
                )
            )
        );
        
        builder.then(Commands.literal("randomize_base_direction")
            .then(Commands.argument("entities", EntityArgument.entities())
                .executes(context -> {
                    RandomSource random = context.getSource().getLevel().random;
                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entities");
                    for (Entity entity : entities) {
                        Direction gravityDirection = Direction.getRandom(random);
                        GravityChangerAPI.setBaseGravityDirection(entity, gravityDirection);
                    }
                    return entities.size();
                })
            )
        );
        
        dispatcher.register(builder);
    }
    
    private static void getSendFeedback(CommandSourceStack source, Entity entity, Direction gravityDirection) {
        Component text = GCUtil.getDirectionText(gravityDirection);
        if (source.getEntity() != null && source.getEntity() == entity) {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.self", text), true);
        }
        else {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.other", entity.getDisplayName(), text), true);
        }
    }
    
    private static void getStrengthSendFeedback(CommandSourceStack source, Entity entity, double strength) {
        Component text = Component.translatable("strength " + strength);
        if (source.getEntity() != null && source.getEntity() == entity) {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.self", text), true);
        }
        else {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.other", entity.getDisplayName(), text), true);
        }
    }
    
    private static int executeGet(CommandSourceStack source, Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        getSendFeedback(source, entity, gravityDirection);
        return gravityDirection.get3DDataValue();
    }
    
    private static int executeRotate(CommandSourceStack source, LocalDirection relativeDirection, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
            Direction combinedRelativeDirection = switch (relativeDirection) {
                case DOWN -> Direction.DOWN;
                case UP -> Direction.UP;
                case FORWARD, BACKWARD, LEFT, RIGHT ->
                    Direction.from2DDataValue(relativeDirection.getHorizontalOffset() + Direction.fromYRot(entity.getYRot()).get2DDataValue());
            };
            Direction newGravityDirection = RotationUtil.dirPlayerToWorld(combinedRelativeDirection, gravityDirection);
            GravityChangerAPI.setBaseGravityDirection(entity, newGravityDirection);
            //GravityChangerAPI.updateGravity(entity);
            getSendFeedback(source, entity, newGravityDirection);
            i++;
        }
        return i;
    }
    
    public enum LocalDirection {
        DOWN(-1, "down"),
        UP(-1, "up"),
        FORWARD(0, "forward"),
        BACKWARD(2, "backward"),
        LEFT(3, "left"),
        RIGHT(1, "right");
        
        private final int horizontalOffset;
        private final String name;
        
        LocalDirection(int horizontalOffset, String name) {
            this.horizontalOffset = horizontalOffset;
            this.name = name;
        }
        
        public int getHorizontalOffset() {
            return this.horizontalOffset;
        }
        
        public String getName() {
            return this.name;
        }
    }
}
