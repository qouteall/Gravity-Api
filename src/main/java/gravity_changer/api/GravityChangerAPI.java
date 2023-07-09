package gravity_changer.api;

import gravity_changer.GravityChangerComponents;
import gravity_changer.EntityTags;
import gravity_changer.GravityComponent;
import gravity_changer.DimensionGravityDataComponent;
import gravity_changer.RotationAnimation;
import gravity_changer.util.RotationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class GravityChangerAPI {
    public static final ComponentKey<GravityComponent> GRAVITY_COMPONENT =
        GravityChangerComponents.GRAVITY_COMP_KEY;
    
    public static final ComponentKey<DimensionGravityDataComponent> DIMENSION_DATA_COMPONENT =
        GravityChangerComponents.DIMENSION_COMP_KEY;
    
    
    /**
     * Returns the applied gravity direction for the given entity
     */
    public static Direction getGravityDirection(Entity entity) {
        return getGravityComponent(entity).getCurrGravityDirection();
    }
    
    public static double getGravityStrength(Entity entity) {
        return getGravityComponent(entity).getCurrGravityStrength();
    }
    
    public static double getBaseGravityStrength(Entity entity) {
        return getGravityComponent(entity).getBaseGravityStrength();
    }
    
    public static void setBaseGravityStrength(Entity entity, double strength) {
        GravityComponent component = getGravityComponent(entity);
        
        component.setBaseGravityStrength(strength);
    }
    
    public static double getDimensionGravityStrength(Level world) {
        return DIMENSION_DATA_COMPONENT.get(world).getDimensionGravityStrength();
    }
    
    public static void setDimensionGravityStrength(Level world, double strength) {
        DIMENSION_DATA_COMPONENT.get(world).setDimensionGravityStrength(strength);
    }
    
    public static void resetGravity(Entity entity) {
        if (!EntityTags.canChangeGravity(entity)) {return;}
        
        getGravityComponent(entity).reset();
    }
    
    /**
     * Returns the main gravity direction for the given entity
     * This may not be the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static Direction getBaseGravityDirection(Entity entity) {
        return getGravityComponent(entity).getBaseGravityDirection();
    }
    
    public static void setBaseGravityDirection(
        Entity entity, Direction gravityDirection
    ) {
        GravityComponent component = getGravityComponent(entity);
        component.setBaseGravityDirection(gravityDirection);
    }
    
    @Nullable
    @Environment(EnvType.CLIENT)
    public static RotationAnimation getRotationAnimation(Entity entity) {
        return getGravityComponent(entity).getRotationAnimation();
    }
    
    public static void instantlySetClientBaseGravityDirection(Entity entity, Direction direction) {
        Validate.isTrue(entity.level().isClientSide(), "should only be used on client");
        
        GravityComponent component = getGravityComponent(entity);
        
        component.setBaseGravityDirection(direction);
    
        component.updateGravityModification(false);
        
        component.updateCurrentGravityBasedOnModifiedGravity();
    
        component.applyGravityChange(RotationParameters.getDefault().withRotationTimeMs(0));
    }
    
    public static GravityComponent getGravityComponent(Entity entity) {
        return GRAVITY_COMPONENT.get(entity);
    }
    
    /**
     * Returns the world relative velocity for the given entity
     * Using minecraft's methods to get the velocity will return entity local velocity
     */
    public static Vec3 getWorldVelocity(Entity entity) {
        return RotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), getGravityDirection(entity));
    }
    
    /**
     * Sets the world relative velocity for the given player
     * Using minecraft's methods to set the velocity of an entity will set player relative velocity
     */
    public static void setWorldVelocity(Entity entity, Vec3 worldVelocity) {
        entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity)));
    }
    
    /**
     * Returns eye position offset from feet position for the given entity
     */
    public static Vec3 getEyeOffset(Entity entity) {
        return RotationUtil.vecPlayerToWorld(0, (double) entity.getEyeHeight(), 0, getGravityDirection(entity));
    }
    
}
