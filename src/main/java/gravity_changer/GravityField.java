package gravity_changer;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GravityField {
    public static boolean isOnField(Entity entity) {
        BlockState feedBlockState = entity.level().getBlockState(entity.getOnPos());
        return feedBlockState.getBlock() == Blocks.GLOWSTONE;
    }
    
    public static void init() {
//        GravityComponent.GRAVITY_DIR_MODIFIER_EVENT.register(new GravityComponent.GravityDirModifierCallback() {
//            @Override
//            public Direction transform(GravityComponent component, Direction direction) {
//                if (isOnField(component.entity)) {
//                    return Direction.NORTH;
//                }
//
//                return direction;
//            }
//        });
    }
}
