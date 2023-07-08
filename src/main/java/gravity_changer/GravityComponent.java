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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;

public class GravityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    
    public static interface GravityDirModifierCallback {
        Direction transform(GravityComponent component, Direction direction);
    }
    
    public static interface GravityStrengthModifierCallback {
        double transform(GravityComponent component, double strength);
    }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
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
    
    // Whether to send sync packet. Only used on server side.
    private boolean needsSync = false;
    boolean initialized = false;
    
    // updated every tick on server side. synchronized
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
    RotationParameters currentRotationParameters = RotationParameters.getDefault();
    
    // Only used on client, not synchronized.
    @Nullable
    public final RotationAnimation animation;
    
    public final Entity entity;
    
    // there is no guarantee of entity ticking order
    // if the gravity source is an entity, it could tick before or after target entity
    private int gravityEffectUpdateTickCount = 0;
    private final ArrayList<GravityEffect> gravityEffects = new ArrayList<>();
    
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
        currGravityDirection = Direction.byName(tag.getString("currGravityDirection"));
        currGravityStrength = tag.getDouble("currGravityStrength");
        
        baseGravityDirection = Direction.byName(tag.getString("baseGravityDirection"));
        
        baseGravityStrength = tag.getDouble("baseGravityStrength");
        
        currentRotationParameters = RotationParameters.fromTag(tag.getCompound("currentRotationParameters"));
        
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
        tag.putString("currGravityDirection", currGravityDirection.getName());
        tag.putDouble("currGravityStrength", currGravityStrength);
        
        tag.putString("baseGravityDirection", baseGravityDirection.getName());
        
        tag.putDouble("baseGravityStrength", baseGravityStrength);
        
        tag.put("currentRotationParameters", currentRotationParameters.toTag());
    }
    
    @Override
    public void tick() {
        if (!canChangeGravity()) {
            return;
        }
        
        if (!entity.level().isClientSide()) {
            updateEffectiveGravityState();
            
            refreshGravityState();
            
            if (needsSync) {
                needsSync = false;
                GravityChangerComponents.GRAVITY_COMP_KEY.sync(entity);
            }
        }
        
        int tickCount = entity.tickCount;
        if (Math.abs(tickCount - gravityEffectUpdateTickCount) >= 2) {
            gravityEffectUpdateTickCount = tickCount;
            gravityEffects.clear();
        }
    }
    
    private void updateEffectiveGravityState() {
        GravityEffect effectiveGravityEffect = getEffectiveGravityEffect();
        
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            currGravityDirection = GravityChangerAPI.getGravityDirection(vehicle);
        }
        else {
            if (effectiveGravityEffect != null && effectiveGravityEffect.direction() != null) {
                currGravityDirection = effectiveGravityEffect.direction();
            }
            else {
                currGravityDirection = GRAVITY_DIR_MODIFIER_EVENT.invoker().transform(
                    this, baseGravityDirection
                );
            }
        }
        
        // TODO handle strength effects
        
        double effectGravityStrength = effectiveGravityEffect == null ? 1.0 : effectiveGravityEffect.strengthMultiplier();
        currGravityStrength = baseGravityStrength *
            GravityChangerAPI.getDimensionGravityStrength(entity.level()) *
            effectGravityStrength;
    }
    
    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
        
        if (entity.level().isClientSide()) {
            // the packet should be handled on client thread
            // start the gravity animation (doing that during ticking is too late)
            refreshGravityState();
            needsSync = false;
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
        if (entity.level().isClientSide()) {
            Validate.notNull(animation, "gravity animation is null");
            animation.startRotationAnimation(
                newGravity, oldGravity,
                rotationParameters.rotationTimeMS(),
                entity, timeMs, rotationParameters.rotateView()
            );
        }
        
        if (!(entity instanceof ServerPlayer)) {
            // A relativeRotationCenter of zero will result in zero translation
            Vec3 relativeRotationCenter = getCenterOfRotationRelativeToFeet(entity, oldGravity, newGravity, rotationParameters);
            Vec3 translation = RotationUtil.vecPlayerToWorld(relativeRotationCenter, oldGravity).subtract(RotationUtil.vecPlayerToWorld(relativeRotationCenter, newGravity));
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
    private static Vec3 getCenterOfRotationRelativeToFeet(
        Entity entity,
        Direction oldGravity, Direction newGravity, RotationParameters rotationParameters
    ) {
        if (entity instanceof EndCrystal) {
            //In the middle of the block below
            return new Vec3(0, -0.5, 0);
        }
        
        if (rotationParameters.alternateCenter()) {
            EntityDimensions dimensions = entity.getDimensions(entity.getPose());
            if (newGravity.getOpposite() == oldGravity) {
                //In the center of the hit-box
                return new Vec3(0, dimensions.height / 2, 0);
            }
            else {
                //Around the ankles
                return new Vec3(0, dimensions.width / 2, 0);
            }
        }
        
        return Vec3.ZERO;
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
        if (!canChangeGravity()) {
            return;
        }
        
        baseGravityStrength = strength;
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
    
    /**
     * When the gravity direction updates on client,
     * to avoid client-server synchronization delay and immediately change gravity on client,
     * call this.
     */
    public void refreshGravityState() {
        if (prevGravityDirection != currGravityDirection) {
            applyGravityDirectionChange(prevGravityDirection, currGravityDirection, currentRotationParameters, false);
            prevGravityDirection = currGravityDirection;
            needsSync = true;
        }
        
        if (Math.abs(currGravityStrength - prevGravityStrength) > 0.0001) {
            prevGravityStrength = currGravityStrength;
            needsSync = true;
        }
    }
    
    private static final int MAX_GRAVITY_EFFECT_NUM = 8;
    
    // needs to apply every tick
    public void applyGravityEffect(GravityEffect effect) {
        int tickCount = entity.tickCount;
        if (tickCount != gravityEffectUpdateTickCount) {
            gravityEffectUpdateTickCount = tickCount;
            gravityEffects.clear();
        }
        
        if (gravityEffects.size() >= MAX_GRAVITY_EFFECT_NUM) {
            return;
        }
        
        gravityEffects.add(effect);
    }
    
    @Nullable
    public GravityEffect getEffectiveGravityEffect() {
        Vec3 position = entity.position();
        
        Comparator<GravityEffect> comparator = Comparator.comparingDouble(
            GravityEffect::priority
        ).thenComparing(
            Comparator.comparingDouble(
                e -> -e.sourcePos().distanceToSqr(position)
            )
        );
        
        return gravityEffects.stream().max(comparator).orElse(null);
    }
}
