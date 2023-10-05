package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FishingHook.class)
public abstract class FishinghookMixin extends Entity {
    
    
    public FishinghookMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    // TODO fishing hook
//    @WrapOperation(
//        method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;refreshPositionAndAngles(DDDFF)V",
//            ordinal = 0
//        )
//    )
//    private void wrapOperation_init_(FishingHook fishingBobberEntity, double x, double y, double z, float yaw, float pitch, Operation<Void> original, Player thrower, Level world, int lureLevel, int luckOfTheSeaLevel) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(thrower);
//        if(gravityDirection == Direction.DOWN) {
//            original.call(fishingBobberEntity, x, y, z, yaw, pitch);
//            return;
//        }
//
//        Vec3 pos = thrower.getEyePosition();
//        Vec2 rot = RotationUtil.rotPlayerToWorld(yaw, pitch, gravityDirection);
//        original.call(fishingBobberEntity, pos.x, pos.y, pos.z, rot.x, rot.y);
//    }
//
//    @ModifyVariable(
//        method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
//        at = @At(
//            value = "INVOKE_ASSIGN",
//            target = "Lnet/minecraft/util/math/Vec3d;multiply(DDD)Lnet/minecraft/util/math/Vec3d;",
//            ordinal = 0
//        ),
//        ordinal = 0
//    )
//    private Vec3 modify_init_Vec3d_1(Vec3 vec3d, Player thrower, Level world, int lureLevel, int luckOfTheSeaLevel) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(thrower);
//        if(gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
//    }
    
    @ModifyConstant(method = "Lnet/minecraft/world/entity/projectile/FishingHook;tick()V", constant = @Constant(doubleValue = -0.03))
    private double multiplyGravity(double constant) {
        return constant * GravityChangerAPI.getGravityStrength(this);
    }
}
