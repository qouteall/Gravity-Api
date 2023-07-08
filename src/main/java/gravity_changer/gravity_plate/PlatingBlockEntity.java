package gravity_changer.gravity_plate;

import java.util.List;

import gravity_changer.EntityTags;
import gravity_changer.GravityComponent;
import gravity_changer.GravityEffect;
import gravity_changer.api.GravityChangerAPI;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Based on code from AmethystGravity (by CyborgCabbage)
 */
public class PlatingBlockEntity extends BlockEntity {
    public static final ResourceLocation ID = new ResourceLocation("gravity_changer:plating_block_entity");
    public static BlockEntityType<PlatingBlockEntity> TYPE;
    
    public static void init() {
        TYPE = FabricBlockEntityTypeBuilder.create(
            PlatingBlockEntity::new, PlatingBlock.PLATING_BLOCK, PlatingBlock.DENSE_PLATING_BLOCK
        ).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID, TYPE);
    }
    
    public PlatingBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public static void tick(Level world, BlockPos blockPos, BlockState blockState, PlatingBlockEntity blockEntity) {
        if (!(blockState.getBlock() instanceof PlatingBlock platingBlock)) {
            return;
        }
        
        PlatingBlock.foreachDirection(blockState, dir -> {
            AABB box = platingBlock.getGravityEffectBox(blockPos, dir);
            
            List<Entity> entities = world.getEntitiesOfClass(
                Entity.class,
                box,
                e -> EntityTags.canChangeGravity(e) && box.contains(e.position())
            );
            
            if (!entities.isEmpty()) {
                Vec3i dirVec = dir.getNormal();
                Vec3 effectCenter = Vec3.atCenterOf(blockPos).add(
                    dirVec.getX() * -0.5, dirVec.getY() * -0.5, dirVec.getZ() * -0.5
                );
                for (Entity entity : entities) {
                    GravityComponent comp = GravityChangerAPI.getGravityComponent(entity);
                    // the priority is higher when closer
                    double distance = entity.position().distanceToSqr(effectCenter);
                    double priority = -distance;
                    GravityEffect gravityEffect = new GravityEffect(
                        dir, 1, priority, effectCenter, world.dimension()
                    );
                    comp.applyGravityEffect(gravityEffect);
                    
                    // apply levitation when the entity is close to corner
                    Direction currGravityDirection = comp.getCurrGravityDirection();
                    boolean orthogonal = currGravityDirection.getAxis() != dir.getAxis();
                    if (orthogonal) {
                        double distanceToPlate = Math.abs(entity.position().subtract(effectCenter)
                            .dot(Vec3.atLowerCornerOf(dir.getNormal())));
                        if (distanceToPlate < 1.0) {
                            double levitationForce = 0.04 / (distanceToPlate + 0.3);
                            entity.addDeltaMovement(new Vec3(
                                0, levitationForce, 0
                            ));
                        }
                    }
                }
            }
        });
    }
}

