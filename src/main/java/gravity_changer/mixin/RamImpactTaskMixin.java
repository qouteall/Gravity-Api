package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.RamImpactTask;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Mixin(value = RamImpactTask.class, priority = 1001)
public abstract class RamImpactTaskMixin {
    @Shadow private Vec3d direction;

    @WrapOperation(
            method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/GoatEntity;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V",
                    ordinal = 0
            )
    )
    private void wrapOperation_keepRunning_takeKnockback_0(LivingEntity target, double strength, double x, double z, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if(gravityDirection == Direction.DOWN) {
            original.call(target, strength, x, z);
            return;
        }

        Vec3d direction = RotationUtil.vecWorldToPlayer(this.direction, gravityDirection);
        original.call(target, strength, direction.x, direction.z);
    }
}
