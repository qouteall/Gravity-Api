package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin {
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_shootSkullAt_getX_0(LivingEntity target) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }
        
        return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
            ordinal = 0
        )
    )
    private double redirect_shootSkullAt_getY_0(LivingEntity target) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }
        
        return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).y - target.getEyeHeight() * 0.5D;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_shootSkullAt_getZ_0(LivingEntity target) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }
        
        return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).z;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;aiStep()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getEyeY()D",
            ordinal = 0
        )
    )
    private double redirect_tickMovement_getEyeY_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getEyeY();
        }
        
        return entity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;aiStep()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_tickMovement_getX_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getX();
        }
        
        return entity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;aiStep()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_tickMovement_getZ_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }
        
        return entity.getEyePosition().z;
    }
}
