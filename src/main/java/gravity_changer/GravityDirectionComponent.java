package gravity_changer;

import com.mojang.logging.LogUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class GravityDirectionComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private Direction currGravityDirection = Direction.DOWN;
    private Direction prevGravityDirection = Direction.DOWN;
    
    Direction baseGravityDirection = Direction.DOWN;
    double baseGravityStrength = 1.0;
    
    public final RotationAnimation animation = new RotationAnimation();
    boolean needsInitialSync = false;
    
    private boolean needsSync = false;
    
    private final Entity entity;
    
    public GravityDirectionComponent(Entity entity) {
        this.entity = entity;
    }
    
    public void onGravityChanged(
        Direction oldGravity, Direction newGravity,
        RotationParameters rotationParameters, boolean isInitialization
    ) {
        entity.setPos(entity.position()); // Causes bounding box recalculation
        
        if (isInitialization) {
            return;
        }
        
        entity.fallDistance = 0;
        
        long timeMs = entity.level().getGameTime() * 50;
        if (entity.level().isClientSide()) {
            animation.applyRotationAnimation(
                newGravity, oldGravity,
                rotationParameters.rotationTimeMS(),
                entity, timeMs, rotationParameters.rotateView()
            );
        }
        
        if (!(entity instanceof ServerPlayer)) {
            //A relativeRotationCentre of zero will result in zero translation
            Vec3 relativeRotationCentre = getCentreOfRotation(oldGravity, newGravity, rotationParameters);
            Vec3 translation = RotationUtil.vecPlayerToWorld(relativeRotationCentre, oldGravity).subtract(RotationUtil.vecPlayerToWorld(relativeRotationCentre, newGravity));
            Direction relativeDirection = RotationUtil.dirWorldToPlayer(newGravity, oldGravity);
            Vec3 smidge = new Vec3(
                relativeDirection == Direction.EAST ? -1.0E-6D : 0.0D,
                relativeDirection == Direction.UP ? -1.0E-6D : 0.0D,
                relativeDirection == Direction.SOUTH ? -1.0E-6D : 0.0D
            );
            smidge = RotationUtil.vecPlayerToWorld(smidge, oldGravity);
            entity.setPos(entity.position().add(translation).add(smidge));
            if (!rotationParameters.alternateCenter()) {
                //Adjust entity position to avoid suffocation and collision
                adjustEntityPosition(oldGravity, newGravity);
            }
        }
        
        Vec3 realWorldVelocity = getRealWorldVelocity(entity, prevGravityDirection);
        if (rotationParameters.rotateVelocity()) {
            // Rotate velocity with gravity, this will cause things to appear to take a sharp turn
            Vector3f worldSpaceVec = realWorldVelocity.toVector3f();
            worldSpaceVec.rotate(RotationUtil.getRotationBetween(prevGravityDirection, currGravityDirection));
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(new Vec3(worldSpaceVec), currGravityDirection));
        }
        else {
            // Velocity will be conserved relative to the world, will result in more natural motion
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(realWorldVelocity, currGravityDirection));
        }
    }
    
    // getVelocity() does not return the actual velocity. It returns the velocity plus acceleration.
    // Even if the entity is standing still, getVelocity() will still give a downwards vector.
    // The real velocity is this tick position subtract last tick position
    private static Vec3 getRealWorldVelocity(Entity entity, Direction prevGravityDirection) {
        if (entity.isControlledByLocalInstance()) {
            return new Vec3(
                entity.getX() - entity.xo,
                entity.getY() - entity.yo,
                entity.getZ() - entity.zo
            );
        }
        
        return RotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), prevGravityDirection);
    }
    
    @NotNull
    private Vec3 getCentreOfRotation(Direction oldGravity, Direction newGravity, RotationParameters rotationParameters) {
        Vec3 relativeRotationCentre = Vec3.ZERO;
        if (entity instanceof EndCrystal) {
            //In the middle of the block below
            relativeRotationCentre = new Vec3(0, -0.5, 0);
        }
        else if (rotationParameters.alternateCenter()) {
            EntityDimensions dimensions = entity.getDimensions(entity.getPose());
            if (newGravity.getOpposite() == oldGravity) {
                //In the center of the hit-box
                relativeRotationCentre = new Vec3(0, dimensions.height / 2, 0);
            }
            else {
                //Around the ankles
                relativeRotationCentre = new Vec3(0, dimensions.width / 2, 0);
            }
        }
        return relativeRotationCentre;
    }
    
    // Adjust position to avoid suffocation in blocks when changing gravity
    private void adjustEntityPosition(Direction oldGravity, Direction newGravity) {
        if (entity instanceof AreaEffectCloud || entity instanceof AbstractArrow || entity instanceof EndCrystal) {
            return;
        }
        
        AABB entityBoundingBox = entity.getBoundingBox();
        
        // for example, if gravity changed from down to north, move up
        // if gravity changed from down to up, also move up
        Direction movingDirection = oldGravity.getOpposite();
        
        Iterable<VoxelShape> collisions = entity.level().getCollisions(entity, entityBoundingBox);
        AABB totalCollisionBox = null;
        for (VoxelShape collision : collisions) {
            if (!collision.isEmpty()) {
                AABB boundingBox = collision.bounds();
                if (totalCollisionBox == null) {
                    totalCollisionBox = boundingBox;
                }
                else {
                    totalCollisionBox = totalCollisionBox.minmax(boundingBox);
                }
            }
        }
        
        if (totalCollisionBox != null) {
            entity.setPos(entity.position().add(getPositionAdjustmentOffset(
                entityBoundingBox, totalCollisionBox, movingDirection
            )));
        }
    }
    
    private static Vec3 getPositionAdjustmentOffset(
        AABB entityBoundingBox, AABB nearbyCollisionUnion, Direction movingDirection
    ) {
        Direction.Axis axis = movingDirection.getAxis();
        double offset = 0;
        if (movingDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            double pushing = nearbyCollisionUnion.max(axis);
            double pushed = entityBoundingBox.min(axis);
            if (pushing > pushed) {
                offset = pushing - pushed;
            }
        }
        else {
            double pushing = nearbyCollisionUnion.min(axis);
            double pushed = entityBoundingBox.max(axis);
            if (pushing < pushed) {
                offset = pushed - pushing;
            }
        }
        
        return new Vec3(movingDirection.step()).scale(offset);
    }
    
    public double getActualGravityStrength() {
        double dimStrength = GravityChangerAPI.getDimensionGravityStrength(entity.level());
        return baseGravityStrength * dimStrength;
    }
    
    public double getBaseGravityStrength() {
        return baseGravityStrength;
    }
    
    public void setBaseGravityStrength(double strength) {
        if (canChangeGravity()) {
            baseGravityStrength = strength;
        }
    }
    
    public Direction getCurrGravityDirection() {
        return currGravityDirection;
    }
    
    private boolean canChangeGravity() {
        return EntityTags.canChangeGravity(entity);
    }
    
    public Direction getPrevGravityDirection() {
        return prevGravityDirection;
    }
    
    public Direction getBaseGravityDirection() {
        return baseGravityDirection;
    }
    
    public void updateGravity(RotationParameters rotationParameters, boolean initialGravity) {
        if (!canChangeGravity()) {return;}
        
        Direction newGravity = getActualGravityDirection();
        Direction oldGravity = currGravityDirection;
        if (oldGravity != newGravity) {
            prevGravityDirection = oldGravity;
            currGravityDirection = newGravity;
            onGravityChanged(oldGravity, newGravity, rotationParameters, initialGravity);
        }
    }
    
    public Direction getActualGravityDirection() {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            return GravityChangerAPI.getGravityDirection(vehicle);
        }
        
        Direction g = getBaseGravityDirection();
        
        // TODO
        
        return g;
    }
    
    public void setBaseGravityDirection(Direction gravityDirection, RotationParameters rotationParameters, boolean initialGravity) {
        if (!canChangeGravity()) {return;}
        
        baseGravityDirection = gravityDirection;
        updateGravity(rotationParameters, initialGravity);
        markNeedsSync();
    }
    
    public void reset() {
        baseGravityDirection = Direction.DOWN;
        updateGravity(RotationParameters.getDefault(), true);
        markNeedsSync();
    }
    
    public RotationAnimation getRotationAnimation() {
        return animation;
    }
    
    /**
     * Mark it needs-sync then it will send sync packet during ticking.
     * Note: This has no effect on client side.
     */
    public void markNeedsSync() {
        needsSync = true;
    }
    
    @Override
    public void readFromNbt(CompoundTag nbt) {
        // TODO
    }
    
    @Override
    public void writeToNbt(@NotNull CompoundTag nbt) {
        // TODO
    }
    
    @Override
    public void tick() {
        // TODO
        
    }
}
