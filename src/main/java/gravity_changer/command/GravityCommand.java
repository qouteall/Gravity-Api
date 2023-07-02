package gravity_changer.command;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.Gravity;
import gravity_changer.api.RotationParameters;
import gravity_changer.util.RotationUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class GravityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalSet = Commands.literal("add");
        for (Direction direction : Direction.values()) {
            literalSet.then(
                Commands.literal(direction.getName())
                    .then(Commands.argument("priority", IntegerArgumentType.integer())
                        .then(Commands.argument("duration", IntegerArgumentType.integer())
                            .executes(context -> executeSet(context.getSource(), direction, IntegerArgumentType.getInteger(context, "priority"), IntegerArgumentType.getInteger(context, "duration"), Collections.singleton(context.getSource().getPlayer())))
                            .then(Commands.argument("entities", EntityArgument.entities())
                                .executes(context -> executeSet(context.getSource(), direction, IntegerArgumentType.getInteger(context, "priority"), IntegerArgumentType.getInteger(context, "duration"), EntityArgument.getEntities(context, "entities"))))))
            );
        }
        
        LiteralArgumentBuilder<CommandSourceStack> literalSetDefault = Commands.literal("set");
        for (Direction direction : Direction.values())
            literalSetDefault.then(Commands.literal(direction.getName())
                .executes(context -> executeSetDefault(context.getSource(), direction, Collections.singleton(context.getSource().getPlayer())))
                .then(Commands.argument("entities", EntityArgument.entities())
                    .executes(context -> executeSetDefault(context.getSource(), direction, EntityArgument.getEntities(context, "entities")))));
        
        LiteralArgumentBuilder<CommandSourceStack> literalRotate = Commands.literal("rotate");
        for (FacingDirection facingDirection : FacingDirection.values())
            literalRotate.then(Commands.literal(facingDirection.getName())
                .executes(context -> executeRotate(context.getSource(), facingDirection, Collections.singleton(context.getSource().getPlayer())))
                .then(Commands.argument("entities", EntityArgument.entities())
                    .executes(context -> executeRotate(context.getSource(), facingDirection, EntityArgument.getEntities(context, "entities")))));
        
        dispatcher.register(Commands.literal("gravity").requires(source -> source.hasPermission(2))
            .then(Commands.literal("get")
                .executes(context -> executeGet(context.getSource(), context.getSource().getPlayer()))
                .then(Commands.argument("entities", EntityArgument.entity())
                    .executes(context -> executeGet(context.getSource(), EntityArgument.getEntity(context, "entities")))))
            .then(Commands.literal("cleargravity")
                .executes(context -> executeClearGravity(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                .then(Commands.argument("entities", EntityArgument.entity())
                    .executes(context -> executeClearGravity(context.getSource(), EntityArgument.getEntities(context, "entities")))))
            .then(Commands.literal("setdefaultstrength")
                .executes(context -> executeSetDefaultStrength(context.getSource(), DoubleArgumentType.getDouble(context, "double"), Collections.singleton(context.getSource().getPlayer())))
                .then(Commands.argument("entities", EntityArgument.entity()).then(Commands.argument("double", DoubleArgumentType.doubleArg())
                    .executes(context -> executeSetDefaultStrength(context.getSource(), DoubleArgumentType.getDouble(context, "double"), Collections.singleton(context.getSource().getPlayer())))))
                .then(literalSet).then(literalSetDefault).then(literalRotate).then(Commands.literal("randomise")
                    .executes(context -> executeRandomise(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                    .then(Commands.argument("entities", EntityArgument.entities())
                        .executes(context -> executeRandomise(context.getSource(), EntityArgument.getEntities(context, "entities")))))));
    }
    
    private static void getSendFeedback(CommandSourceStack source, Entity entity, Direction gravityDirection) {
        Component text = Component.translatable("direction." + gravityDirection.getName());
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
    
    private static int executeSet(CommandSourceStack source, Direction gravityDirection, int priority, int durration, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            GravityChangerAPI.addGravity(
                entity,
                new Gravity(gravityDirection, priority, durration, "command")
            );
            i++;
        }
        return i;
    }
    
    private static int executeSetDefault(CommandSourceStack source, Direction gravityDirection, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            if (GravityChangerAPI.getDefaultGravityDirection(entity) != gravityDirection) {
                GravityChangerAPI.setDefaultGravityDirection(entity, gravityDirection, new RotationParameters());
                //GravityChangerAPI.updateGravity(entity);
                getSendFeedback(source, entity, gravityDirection);
                i++;
            }
        }
        return i;
    }
    
    private static int executeSetDefaultStrength(CommandSourceStack source, double gravityStrength, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            if (GravityChangerAPI.getDefaultGravityStrength(entity) != gravityStrength) {
                GravityChangerAPI.setDefualtGravityStrength(entity, gravityStrength);
                getStrengthSendFeedback(source, entity, gravityStrength);
                i++;
            }
        }
        return i;
    }
    
    private static int executeRotate(CommandSourceStack source, FacingDirection relativeDirection, Collection<? extends Entity> entities) {
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
            GravityChangerAPI.setDefaultGravityDirection(entity, newGravityDirection, new RotationParameters());
            //GravityChangerAPI.updateGravity(entity);
            getSendFeedback(source, entity, newGravityDirection);
            i++;
        }
        return i;
    }
    
    private static int executeRandomise(CommandSourceStack source, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            Direction gravityDirection = Direction.getRandom(source.getLevel().random);
            if (GravityChangerAPI.getGravityDirection(entity) != gravityDirection) {
                GravityChangerAPI.setDefaultGravityDirection(entity, gravityDirection, new RotationParameters());
                //GravityChangerAPI.updateGravity(entity);
                getSendFeedback(source, entity, gravityDirection);
                i++;
            }
        }
        return i;
    }
    
    private static int executeClearGravity(CommandSourceStack source, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            GravityChangerAPI.clearGravity(entity, new RotationParameters());
            i++;
        }
        return i;
    }
    
    public enum FacingDirection {
        DOWN(-1, "down"),
        UP(-1, "up"),
        FORWARD(0, "forward"),
        BACKWARD(2, "backward"),
        LEFT(3, "left"),
        RIGHT(1, "right");
        
        private final int horizontalOffset;
        private final String name;
        
        FacingDirection(int horizontalOffset, String name) {
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
