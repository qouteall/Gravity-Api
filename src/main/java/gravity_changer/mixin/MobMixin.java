package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobMixin {
    @WrapOperation(
        method = "doHurtTarget",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;getYRot()F"
        )
    )
    private float wrapOperation_tryAttack_getYaw_0(Mob attacker, Operation<Float> original, Entity target) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return original.call(attacker);
        }
        
        return RotationUtil.rotWorldToPlayer(original.call(attacker), attacker.getXRot(), gravityDirection).x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D",
            ordinal = 0
        )
    )
    private double redirect_lookAtEntity_getEyeY_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getEyeY();
        }
        
        return livingEntity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_lookAtEntity_getX_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getX();
        }
        
        return entity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_lookAtEntity_getZ_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }
        
        return entity.getEyePosition().z;
    }
}
