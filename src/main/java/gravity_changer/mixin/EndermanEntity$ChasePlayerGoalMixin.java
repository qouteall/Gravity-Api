package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    targets = "net.minecraft.world.entity.monster.EnderMan$EndermanFreezeWhenLookedAt"
)
public abstract class EndermanEntity$ChasePlayerGoalMixin {
    @Redirect(
        method = "Lnet/minecraft/world/entity/monster/EnderMan$EndermanFreezeWhenLookedAt;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getEyeY_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getEyeY();
        }
        
        return livingEntity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/entity/mob/EndermanEntity$ChasePlayerGoal;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getX_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getX();
        }
        
        return livingEntity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/entity/mob/EndermanEntity$ChasePlayerGoal;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getZ_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getZ();
        }
        
        return livingEntity.getEyePosition().z;
    }
}
