package gravity_changer.plating;

import java.util.List;

import com.mojang.logging.LogUtils;
import gravity_changer.EntityTags;
import gravity_changer.GravityChangerMod;
import gravity_changer.GravityComponent;
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
import org.slf4j.Logger;

/**
 * Based on code from AmethystGravity (by CyborgCabbage)
 */
public class PlatingBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    
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
        
        AABB roughBox = platingBlock.getRoughEffectBox(blockPos);
        
        List<Entity> entities = world.getEntitiesOfClass(
            Entity.class,
            roughBox,
            e -> EntityTags.canChangeGravity(e)
        );
        
        for (Entity entity : entities) {
            boolean applies = false;
            
            GravityComponent comp = GravityChangerAPI.getGravityComponent(entity);
            Direction entityGravityDir = comp.getCurrGravityDirection();
            
            for (Direction plateDir : Direction.values()) {
                if (PlatingBlock.hasDir(blockState, plateDir)) {
                    // when the player has no gravity effect and is touching the plate with their eyes,
                    // test the eye pos
                    boolean isOpposite = (entityGravityDir == plateDir.getOpposite());
                    Vec3 testingPos = isOpposite ? entity.getEyePosition() : entity.position();
                    
                    AABB gravityEffectBox = platingBlock.getGravityEffectBox(world, blockPos, plateDir);
                    if (!gravityEffectBox.contains(testingPos)) {
                        continue;
                    }
                    
                    Vec3i plateDirVec = plateDir.getNormal();
                    Vec3 effectCenter = Vec3.atCenterOf(blockPos).add(
                        plateDirVec.getX() * 0.5, plateDirVec.getY() * 0.5, plateDirVec.getZ() * 0.5
                    );
                    
                    double distanceToPlane = -testingPos.subtract(effectCenter)
                        .dot(Vec3.atLowerCornerOf(plateDirVec));
                    if (distanceToPlane < 0) {
                        continue;
                    }
                    
                    double priority = 100 - distanceToPlane;
                    if (plateDir == entityGravityDir) {
                        // make the current gravity a little higher priority
                        // to reduce the chance of gravity jiggling
                        priority += platingBlock.gravityEffectHeight * 0.2;
                    }
                    if (isOpposite) {
                        // reduce the chance of opposite side plating interference
                        priority -= 3;
                    }
                    comp.applyGravityDirectionEffect(
                        plateDir, null, priority
                    );
                    applies = true;
                }
            }
            
            if (applies && GravityChangerMod.config.autoJumpOnGravityPlateInnerCorner) {
                tryToDoCornerAutoJump(blockState, blockPos, entity, comp);
            }
        }
    }
    
    // when approaching an inward corner, do auto-jump to make it smoothly go forward
    private static void tryToDoCornerAutoJump(
        BlockState blockState, BlockPos blockPos,
        Entity entity, GravityComponent comp
    ) {
        if (!entity.onGround()) {
            return;
        }
        
        // apply levitation when the entity is close to corner
        Direction entityGravityDir = comp.getCurrGravityDirection();
        
        for (Direction plateDir : Direction.values()) {
            if (PlatingBlock.hasDir(blockState, plateDir)) {
                boolean orthogonal = entityGravityDir.getAxis() != plateDir.getAxis();
                if (!orthogonal) {
                    continue;
                }
                
                Vec3 plateDirVec = Vec3.atLowerCornerOf(plateDir.getNormal());
                
                Vec3 effectCenter = Vec3.atCenterOf(blockPos).add(plateDirVec.scale(0.5));
                Vec3 offset = effectCenter.subtract(entity.position());
                if (offset.dot(Vec3.atLowerCornerOf(entityGravityDir.getNormal())) > 0) {
                    // that plate is lower than entity
                    continue;
                }
                
                Vec3 worldVelocity = GravityChangerAPI.getWorldVelocity(entity);
                if (worldVelocity.dot(plateDirVec) < 0.01) {
                    continue;
                }
                
                double distanceToPlate = Math.abs(entity.position().subtract(effectCenter).dot(plateDirVec));
                if (distanceToPlate < 0.8) {
                    double strengthSqrt = Math.sqrt(comp.getCurrGravityStrength());
                    
                    Vec3 entityGravityVec = Vec3.atLowerCornerOf(entityGravityDir.getNormal());
                    
                    Vec3 deltaWorldVelocity =
                        entityGravityVec.scale(-strengthSqrt * 0.4)
                            .add(plateDirVec.scale(0.08));
                    
                    GravityChangerAPI.setWorldVelocity(
                        entity,
                        GravityChangerAPI.getWorldVelocity(entity).add(deltaWorldVelocity)
                    );
                    
                    if (entity.level().isClientSide()) {
                        LOGGER.info("Client entity auto-jump on gravity plate corner {}", entity);
                    }
                    return;
                }
            }
        }
        
    }
}

