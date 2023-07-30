package gravity_changer;

import com.mojang.logging.LogUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import gravity_changer.mixin.EntityAccessor;
import gravity_changer.util.RotationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

/**
 * The gravity is determined by the follows:
 * 1. base gravity
 * 2. gravity modifier, can override base gravity (determined from modifier events)
 * 3. gravity effects, can override modified gravity
 * The result of applying 1 and 2 is called modified gravity and is synced.
 * The result of 3 is current gravity and is not synced.
 * The gravity effect should be applied both on client and server.
 */
public class GravityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    
    public static interface GravityUpdateCallback {
        void update(Entity entity, GravityComponent component);
    }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Fired every tick for every entity, both on client and server.
     * <p>
     * In the event, it can call
     * {@link GravityComponent#applyGravityDirectionEffect(Direction, RotationParameters, double)}
     * and
     * {@link GravityComponent#applyGravityStrengthEffect(double)}
     * (these two applying methods can also be called outside the event)
     * <p>
     * To keep the result consistent between client and server,
     * the event listener should only use synchronized information.
     * <p>
     * In the event, it can read the current gravity direction for use cases like gravity inverting. (It requires phase ordering to keep the execution order.)
     */
    public static final Event<GravityUpdateCallback> GRAVITY_UPDATE_EVENT =
        EventFactory.createArrayBacked(
            GravityUpdateCallback.class,
            listeners -> (entity, component) -> {
                for (GravityUpdateCallback callback : listeners) {
                    callback.update(entity, component);
                }
            }
        );
    
    boolean initialized = false;
    
    // not synchronized
    private Direction prevGravityDirection = Direction.DOWN;
    private double prevGravityStrength = 1.0;
    
    // the base gravity direction
    Direction baseGravityDirection = Direction.DOWN;
    
    // the base gravity strength
    double baseGravityStrength = 1.0;
    
    @Nullable RotationParameters currentRotationParameters = RotationParameters.getDefault();
    
    // Only used on client, not synchronized.
    @Nullable
    public final RotationAnimation animation;
    
    public final Entity entity;
    
    private Direction currGravityDirection = Direction.DOWN;
    private double currGravityStrength = 1.0;
    private double currentEffectPriority = Double.MIN_VALUE;
    
    private boolean isFiringUpdateEvent = false;
    
    private @Nullable GravityComponent.GravityDirEffect delayApplyDirEffect = null;
    private double delayApplyStrengthEffect = 1.0;
    
    // if it equals entity.tickCount,
    // it means that the gravity update event has already fired in this tick
    private long lastUpdateTickCount = 0;
    
    // only used on server side
    private boolean needsSync = false;
    
    public GravityComponent(Entity entity) {
        this.entity = entity;
        if (entity.level().isClientSide()) {
            animation = new RotationAnimation();
        }
        else {
            animation = null;
        }
    }
    
    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("baseGravityDirection")) {
            baseGravityDirection = Direction.byName(tag.getString("baseGravityDirection"));
        }
        else {
            baseGravityDirection = Direction.DOWN;
        }
        
        // the current gravity is serialized to avoid unnecessary gravity rotation when entering world
        // only deserialize it when initializing entity
        if (!initialized) {
            if (tag.contains("currentGravityDirection")) {
                currGravityDirection = Direction.byName(tag.getString("currentGravityDirection"));
            }
            else {
                currGravityDirection = Direction.DOWN;
            }
        }
        
        if (tag.contains("baseGravityStrength")) {
            baseGravityStrength = tag.getDouble("baseGravityStrength");
        }
        else {
            baseGravityStrength = 1.0;
        }
        
        if (!initialized) {
            prevGravityDirection = currGravityDirection;
            prevGravityStrength = currGravityStrength;
            initialized = true;
            applyGravityDirectionChange(
                prevGravityDirection, currGravityDirection, currentRotationParameters, true
            );
        }
    }
    
    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        tag.putString("baseGravityDirection", baseGravityDirection.getName());
        tag.putString("currentGravityDirection", currGravityDirection.getName());
        tag.putDouble("baseGravityStrength", baseGravityStrength);
    }
    
    @Override
    public void tick() {
        if (!canChangeGravity()) {
            return;
        }
        
        updateGravityStatus();
        
        applyGravityChange();
        
        if (!entity.level().isClientSide()) {
            if (needsSync) {
                needsSync = false;
                GravityChangerComponents.GRAVITY_COMP_KEY.sync(entity);
            }
        }
    }
    
    public void updateGravityStatus() {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            currGravityDirection = GravityChangerAPI.getGravityDirection(vehicle);
            currGravityStrength = GravityChangerAPI.getGravityStrength(vehicle);
        }
        else {
            currGravityDirection = baseGravityDirection;
            currGravityStrength = baseGravityStrength;
            currGravityStrength *= GravityChangerAPI.getDimensionGravityStrength(entity.level());
            currGravityStrength *= GravityChangerMod.config.gravityStrengthMultiplier;
            // the rotation parameters is not being reset here
            // the rotation parameter is kept when an effect vanishes
            currentEffectPriority = Double.MIN_VALUE;
            
            isFiringUpdateEvent = true;
            try {
                GRAVITY_UPDATE_EVENT.invoker().update(entity, this);
                if (delayApplyDirEffect != null) {
                    applyGravityDirectionEffect(
                        delayApplyDirEffect.direction(),
                        delayApplyDirEffect.rotationParameters(), delayApplyDirEffect.priority()
                    );
                    delayApplyDirEffect = null;
                }
                currGravityStrength *= delayApplyStrengthEffect;
                delayApplyStrengthEffect = 1.0;
            }
            finally {
                isFiringUpdateEvent = false;
            }
            
            if (currentEffectPriority == Double.MIN_VALUE) {
                // if no effect is applied, reset the rotation parameters
                currentRotationParameters = RotationParameters.getDefault();
            }
            
            lastUpdateTickCount = entity.tickCount;
        }
    }
    
    public void applyGravityDirectionEffect(
        @NotNull Direction direction,
        @Nullable RotationParameters rotationParameters,
        double priority
    ) {
        if (isFiringUpdateEvent) {
            if (priority > currentEffectPriority) {
                currentEffectPriority = priority;
                currGravityDirection = direction;
                
                if (rotationParameters != null) {
                    currentRotationParameters = rotationParameters;
                }
            }
        }
        else {
            // When not firing event, store it on delayApplyEffect.
            // The effect could come from another entity ticking,
            // but there is no guarantee for ticking order between entities.
            // (the ticking order does not change according to EntityTickList)
            if (delayApplyDirEffect == null || priority > delayApplyDirEffect.priority()) {
                delayApplyDirEffect = new GravityDirEffect(
                    direction, rotationParameters, priority
                );
            }
        }
    }
    
    public void applyGravityStrengthEffect(
        double strengthMultiplier
    ) {
        if (isFiringUpdateEvent) {
            currGravityStrength *= strengthMultiplier;
        }
        else {
            delayApplyStrengthEffect *= strengthMultiplier;
        }
    }
    
    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
        
        if (entity.level().isClientSide()) {
            // the packet should be handled on client thread
            // start the gravity animation (doing that during ticking is too late)
            applyGravityChange();
        }
    }
    
    public void applyGravityDirectionChange(
        Direction oldGravity, Direction newGravity,
        RotationParameters rotationParameters, boolean isInitialization
    ) {
        if (!canChangeGravity()) {
            return;
        }
        
        // update bounding box
        entity.setBoundingBox(((EntityAccessor) entity).gc_makeBoundingBox());
        
        // A weird thing is that,
        // using `entity.setPos(entity.position())` to a painting on client side
        // make the painting move wrongly, because Painting overrides `trackingPosition()`.
        // No entity other than Painting overrides that method.
        // It seems to be legacy code from early versions of Minecraft.
        
        if (isInitialization) {
            return;
        }
        
        entity.fallDistance = 0;
        
        long timeMs = entity.level().getGameTime() * 50;
        
        Vec3 relativeRotationCenter = getLocalRotationCenter(
            entity, oldGravity, newGravity, rotationParameters
        );
        Vec3 oldPos = entity.position();
        Vec3 oldLastTickPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        Vec3 rotationCenter = oldPos.add(RotationUtil.vecPlayerToWorld(relativeRotationCenter, oldGravity));
        Vec3 newPos = rotationCenter.subtract(RotationUtil.vecPlayerToWorld(relativeRotationCenter, newGravity));
        Vec3 posTranslation = newPos.subtract(oldPos);
        Vec3 newLastTickPos = oldLastTickPos.add(posTranslation);
        
        entity.setPos(newPos);
        entity.xo = newLastTickPos.x;
        entity.yo = newLastTickPos.y;
        entity.zo = newLastTickPos.z;
        entity.xOld = newLastTickPos.x;
        entity.yOld = newLastTickPos.y;
        entity.zOld = newLastTickPos.z;
        
        adjustEntityPosition(oldGravity, newGravity, entity.getBoundingBox());
        
        if (entity.level().isClientSide()) {
            Validate.notNull(animation, "gravity animation is null");
            
            int rotationTimeMS = rotationParameters.rotationTimeMS();
            
            animation.startRotationAnimation(
                newGravity, oldGravity,
                rotationTimeMS,
                entity, timeMs, rotationParameters.rotateView(),
                relativeRotationCenter
            );
        }
        
        Vec3 realWorldVelocity = getRealWorldVelocity(entity, oldGravity);
        if (rotationParameters.rotateVelocity()) {
            // Rotate velocity with gravity, this will cause things to appear to take a sharp turn
            Vector3f worldSpaceVec = realWorldVelocity.toVector3f();
            worldSpaceVec.rotate(RotationUtil.getRotationBetween(oldGravity, newGravity));
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(new Vec3(worldSpaceVec), newGravity));
        }
        else {
            // Velocity will be conserved relative to the world, will result in more natural motion
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(realWorldVelocity, newGravity));
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
    private static Vec3 getLocalRotationCenter(
        Entity entity,
        Direction oldGravity, Direction newGravity, RotationParameters rotationParameters
    ) {
        if (entity instanceof EndCrystal) {
            //In the middle of the block below
            return new Vec3(0, -0.5, 0);
        }
        
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        if (newGravity.getOpposite() == oldGravity) {
            // In the center of the hit-box
            return new Vec3(0, dimensions.height / 2, 0);
        }
        else {
            return Vec3.ZERO;
        }
    }
    
    // Adjust position to avoid suffocation in blocks when changing gravity
    private void adjustEntityPosition(Direction oldGravity, Direction newGravity, AABB entityBoundingBox) {
        if (!GravityChangerMod.config.adjustPositionAfterChangingGravity) {
            return;
        }
        
        if (entity instanceof AreaEffectCloud || entity instanceof AbstractArrow || entity instanceof EndCrystal) {
            return;
        }
        
        // for example, if gravity changed from down to north, move up
        // if gravity changed from down to up, also move up
        Direction movingDirection = oldGravity.getOpposite();
        
        Iterable<VoxelShape> collisions = entity.level().getCollisions(
            entity,
            entityBoundingBox.inflate(-0.01) // shrink to avoid floating point error
        );
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
            Vec3 positionAdjustmentOffset = getPositionAdjustmentOffset(
                entityBoundingBox, totalCollisionBox, movingDirection
            );
            if (entity instanceof Player) {
                LOGGER.info("Adjusting player position {} {}", positionAdjustmentOffset, entity);
            }
            entity.setPos(entity.position().add(positionAdjustmentOffset));
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
    
    public double getBaseGravityStrength() {
        return baseGravityStrength;
    }
    
    public void setBaseGravityStrength(double strength) {
        if (!canChangeGravity()) {
            return;
        }
        
        baseGravityStrength = strength;
        needsSync = true;
    }
    
    public Direction getCurrGravityDirection() {
        return currGravityDirection;
    }
    
    public double getCurrGravityStrength() {
        return currGravityStrength;
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
    
    public void setBaseGravityDirection(Direction gravityDirection) {
        if (!canChangeGravity()) {
            return;
        }
        
        baseGravityDirection = gravityDirection;
        needsSync = true;
    }
    
    public void reset() {
        baseGravityDirection = Direction.DOWN;
        baseGravityStrength = 1.0;
        needsSync = true;
    }
    
    @Environment(EnvType.CLIENT)
    public RotationAnimation getRotationAnimation() {
        return animation;
    }
    
    public void applyGravityChange() {
        if (currentRotationParameters == null) {
            currentRotationParameters = RotationParameters.getDefault();
        }
        
        if (prevGravityDirection != currGravityDirection) {
            applyGravityDirectionChange(
                prevGravityDirection, currGravityDirection,
                currentRotationParameters, false
            );
            prevGravityDirection = currGravityDirection;
        }
        
        if (Math.abs(currGravityStrength - prevGravityStrength) > 0.0001) {
            prevGravityStrength = currGravityStrength;
        }
    }
    
    /**
     * Not needed in normal cases.
     * Only used in {@link GravityChangerAPI#instantlySetClientBaseGravityDirection(Entity, Direction)}
     * Used by ImmPtl.
     */
    public void forceApplyGravityChange() {
        prevGravityDirection = currGravityDirection;
        prevGravityStrength = currGravityStrength;
    }
    
    private static record GravityDirEffect(
        @NotNull Direction direction,
        @Nullable RotationParameters rotationParameters,
        double priority
    ) {
    
    }
}
