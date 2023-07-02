package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RamTarget.class, priority = 1001)
public abstract class RamImpactTaskMixin {
    @Shadow
    private Vec3 ramDirection;
    
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V",
            ordinal = 0
        )
    )
    private void wrapOperation_keepRunning_takeKnockback_0(LivingEntity target, double strength, double x, double z, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            original.call(target, strength, x, z);
            return;
        }
        
        Vec3 direction = RotationUtil.vecWorldToPlayer(this.ramDirection, gravityDirection);
        original.call(target, strength, direction.x, direction.z);
    }
}
