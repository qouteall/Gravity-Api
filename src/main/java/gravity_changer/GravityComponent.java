package gravity_changer;

import com.mojang.logging.LogUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
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
 * 2. gravity modifier (determined from modifier events)
 * 3. gravity effects
 * The result of applying 1 and 2 is called modified gravity and is synced.
 * The result of 3 is current gravity and is not synced.
 * The gravity effect should be applied both on client and server.
 */
public class GravityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    
    public static interface GravityDirModifierCallback {
        Direction transform(GravityComponent component, Direction direction);
    }
    
    public static interface GravityStrengthModifierCallback {
        double transform(GravityComponent component, double strength);
    }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Modify the gravity direction in this event.
     * This event is mostly called on server side.
     * It can be called on client when {@link GravityComponent#updateGravityModification()} is called on client
     */
    public static final Event<GravityDirModifierCallback> GRAVITY_DIR_MODIFIER_EVENT =
        EventFactory.createArrayBacked(
            GravityDirModifierCallback.class,
            (listeners) -> (component, direction) -> {
                Direction curr = direction;
                for (GravityDirModifierCallback callback : listeners) {
                    curr = callback.transform(component, curr);
                }
                return curr;
            }
        );
    
    public static final Event<GravityStrengthModifierCallback> GRAVITY_STRENGTH_MODIFIER_EVENT =
        EventFactory.createArrayBacked(
            GravityStrengthModifierCallback.class,
            (listeners) -> (component, strength) -> {
                double curr = strength;
                for (GravityStrengthModifierCallback callback : listeners) {
                    curr = callback.transform(component, curr);
                }
                return curr;
            }
        );
    
    boolean initialized = false;
    
    // updated every tick on server side. synchronized
    private Direction modifiedGravityDirection = Direction.DOWN;
    private double modifiedGravityStrength = 1.0;
    
    private Direction currGravityDirection = Direction.DOWN;
    private double currGravityStrength = 1.0;
    
    // not synchronized
    private Direction prevGravityDirection = Direction.DOWN;
    private double prevGravityStrength = 1.0;
    
    // the base gravity direction
    Direction baseGravityDirection = Direction.DOWN;
    
    // the base gravity strength
    double baseGravityStrength = 1.0;
    
    // Synchronized. Its duration is used on client side animation.
    @Nullable RotationParameters currentTickRotationParameters = null;
    
    // Only used on client, not synchronized.
    @Nullable
    public final RotationAnimation animation;
    
    public final Entity entity;
    
    // there is no guarantee of entity ticking order
    // if the gravity source is an entity, it could tick before or after target entity
    private int gravityEffectUpdateTickCount = 0;
    private @Nullable GravityEffect currentGravityEffect = null;
    
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
        if (tag.contains("modifiedGravityDirection")) {
            modifiedGravityDirection = Direction.byName(tag.getString("modifiedGravityDirection"));
        }
        else {
            modifiedGravityDirection = Direction.DOWN;
        }
        
        if (tag.contains("modifiedGravityStrength")) {
            modifiedGravityStrength = tag.getDouble("modifiedGravityStrength");
        }
        else {
            modifiedGravityStrength = 1.0;
        }
        
        if (tag.contains("baseGravityDirection")) {
            baseGravityDirection = Direction.byName(tag.getString("baseGravityDirection"));
        }
        else {
            baseGravityDirection = Direction.DOWN;
        }
        
        if (tag.contains("baseGravityStrength")) {
            baseGravityStrength = tag.getDouble("baseGravityStrength");
        }
        else {
            baseGravityStrength = 1.0;
        }
        
        if (tag.contains("currentRotationParameters")) {
            currentTickRotationParameters =
                RotationParameters.fromTag(tag.getCompound("currentRotationParameters"));
        }
        else {
            currentTickRotationParameters = null;
        }
        
        if (!initialized) {
            updateCurrentGravityBasedOnModifiedGravity();
            prevGravityDirection = currGravityDirection;
            prevGravityStrength = currGravityStrength;
            initialized = true;
            applyGravityDirectionChange(
                prevGravityDirection, currGravityDirection, currentTickRotationParameters, true
            );
        }
    }
    
    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        tag.putString("modifiedGravityDirection", modifiedGravityDirection.getName());
        tag.putDouble("modifiedGravityStrength", modifiedGravityStrength);
        
        tag.putString("baseGravityDirection", baseGravityDirection.getName());
        
        tag.putDouble("baseGravityStrength", baseGravityStrength);
        
        if (currentTickRotationParameters != null) {
            tag.put("currentRotationParameters", currentTickRotationParameters.toTag());
        }
    }
    
    @Override
    public void tick() {
        if (!canChangeGravity()) {
            return;
        }
        
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            currGravityDirection = GravityChangerAPI.getGravityDirection(vehicle);
            currGravityStrength = GravityChangerAPI.getGravityStrength(vehicle);
        }
        else {
            int tickCount = entity.tickCount;
            if (Math.abs(tickCount - gravityEffectUpdateTickCount) >= 2) {
                gravityEffectUpdateTickCount = tickCount;
                currentGravityEffect = null;
            }
            
            if (!entity.level().isClientSide()) {
                updateGravityModification();
                
                updateCurrentGravityBasedOnModifiedGravity();
                
                applyGravityChange();
            }
            else {
                updateCurrentGravityBasedOnModifiedGravity();
                
                applyGravityChange();
            }
        }
        
        // it only persists for one tick
        currentTickRotationParameters = null;
    }
    
    public void updateCurrentGravityBasedOnModifiedGravity() {
        Validate.isTrue(modifiedGravityDirection != null);
        GravityEffect effect = getCurrentGravityEffect();
        if (effect != null) {
            if (effect.direction() != null) {
                currGravityDirection = effect.direction();
            }
            else {
                currGravityDirection = modifiedGravityDirection;
            }
            currGravityStrength = modifiedGravityStrength * effect.strengthMultiplier();
        }
        else {
            currGravityDirection = modifiedGravityDirection;
            currGravityStrength = modifiedGravityStrength;
        }
    }
    
    public void updateGravityModification() {
        Direction oldModifiedGravityDirection = modifiedGravityDirection;
        modifiedGravityDirection = GRAVITY_DIR_MODIFIER_EVENT.invoker().transform(
            this, baseGravityDirection
        );
        
        double dimensionGravityStrength = GravityChangerAPI.getDimensionGravityStrength(entity.level());
        double configStrength = GravityChangerMod.config.gravityStrengthMultiplier;
        double strength = baseGravityStrength;
        strength *= dimensionGravityStrength;
        strength *= configStrength;
        double oldModifiedGravityStrength = modifiedGravityStrength;
        modifiedGravityStrength = GRAVITY_STRENGTH_MODIFIER_EVENT.invoker().transform(
            this, strength
        );
        
        boolean needsSync = (modifiedGravityDirection != oldModifiedGravityDirection)
            || Math.abs(modifiedGravityStrength - oldModifiedGravityStrength) > 1e-6;
        
        if (needsSync) {
            GravityChangerComponents.GRAVITY_COMP_KEY.sync(entity);
        }
    }
    
    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
        
        if (entity.level().isClientSide()) {
            // the packet should be handled on client thread
            // start the gravity animation (doing that during ticking is too late)
            updateCurrentGravityBasedOnModifiedGravity();
            
            applyGravityChange();
        }
    }
    
    public void applyGravityDirectionChange(
        Direction oldGravity, Direction newGravity,
        RotationParameters rotationParameters, boolean isInitialization
    ) {
        entity.setPos(entity.position()); // Causes bounding box recalculation
        
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
    
    public void setBaseGravityDirection(Direction gravityDirection, RotationParameters rotationParameters, boolean initialGravity) {
        if (!canChangeGravity()) {
            return;
        }
        
        baseGravityDirection = gravityDirection;
    }
    
    public void reset() {
        baseGravityDirection = Direction.DOWN;
        baseGravityStrength = 1.0;
    }
    
    @Environment(EnvType.CLIENT)
    public RotationAnimation getRotationAnimation() {
        return animation;
    }
    
    public void applyGravityChange() {
        if (prevGravityDirection != currGravityDirection) {
            RotationParameters rotationParameters =
                currentTickRotationParameters == null ? RotationParameters.getDefault() : currentTickRotationParameters;
            applyGravityDirectionChange(
                prevGravityDirection, currGravityDirection,
                rotationParameters, false
            );
            prevGravityDirection = currGravityDirection;
        }
        
        if (Math.abs(currGravityStrength - prevGravityStrength) > 0.0001) {
            prevGravityStrength = currGravityStrength;
        }
    }
    
    /**
     * Applies gravity effect.
     * The effect should be applied both on client and server.
     * The effect itself is not synchronized between client and server,
     * so the effect should be determined by synchronized things such as player position.
     * To keep the effect, it should be applied every tick.
     */
    public void applyGravityEffect(GravityEffect effect) {
        int tickCount = entity.tickCount;
        if (tickCount != gravityEffectUpdateTickCount) {
            gravityEffectUpdateTickCount = tickCount;
            currentGravityEffect = null;
        }
        
        if (currentGravityEffect == null) {
            currentGravityEffect = effect;
        }
        else {
            if (effect.priority() > currentGravityEffect.priority()) {
                currentGravityEffect = effect;
            }
        }
    }
    
    @Nullable
    public GravityEffect getCurrentGravityEffect() {
//        return null;
        return currentGravityEffect;
    }
    
    public void setCurrentTickRotationParameters(@Nullable RotationParameters r) {
        currentTickRotationParameters = r;
    }
}
