package gravity_changer.mixin.client;

import java.util.Optional;

import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import gravity_changer.RotationAnimation;
import gravity_changer.api.GravityChangerAPI;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mixin(value = Camera.class, priority = 1001)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(double x, double y, double z);
    
    @Shadow
    private Entity entity;
    
    @Shadow
    @Final
    private Quaternionf rotation;
    
    @Shadow
    private float eyeHeightOld;
    
    @Shadow
    private float eyeHeight;
    
    private float storedTickDelta = 0.f;
    
    @Inject(method = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", at = @At("HEAD"))
    private void inject_update(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        storedTickDelta = tickDelta;
    }
    
    @WrapOperation(
        method = "setup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
            ordinal = 0
        )
    )
    private void wrapOperation_update_setPos_0(Camera camera, double x, double y, double z, Operation<Void> original, BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(focusedEntity); ;
        Optional<RotationAnimation> animationOptional = GravityChangerAPI.getGravityAnimation(focusedEntity);
        if (animationOptional.isEmpty()) {
            original.call(this, x, y, z);
            return;
        }
        RotationAnimation animation = animationOptional.get();
        if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
            original.call(this, x, y, z);
            return;
        }
        long timeMs = focusedEntity.level().getGameTime() * 50 + (long) (storedTickDelta * 50);
        Quaternionf gravityRotation = new Quaternionf(animation.getCurrentGravityRotation(gravityDirection, timeMs));
        gravityRotation.conjugate();
        
        double entityX = Mth.lerp((double) tickDelta, focusedEntity.xo, focusedEntity.getX());
        double entityY = Mth.lerp((double) tickDelta, focusedEntity.yo, focusedEntity.getY());
        double entityZ = Mth.lerp((double) tickDelta, focusedEntity.zo, focusedEntity.getZ());
        
        double currentCameraY = Mth.lerp(tickDelta, this.eyeHeightOld, this.eyeHeight);
        
        Vector3f eyeOffset = new Vector3f(0, (float) currentCameraY, 0);
        eyeOffset.rotate(gravityRotation);
        
        original.call(
            this,
            entityX + eyeOffset.x(),
            entityY + eyeOffset.y(),
            entityZ + eyeOffset.z()
        );
    }
    
    @Inject(
        method = "Lnet/minecraft/client/Camera;setRotation(FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void inject_setRotation(CallbackInfo ci) {
        if (this.entity != null) {
            Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.entity);
            Optional<RotationAnimation> animationOptional = GravityChangerAPI.getGravityAnimation(entity);
            if (animationOptional.isEmpty()) return;
            RotationAnimation animation = animationOptional.get();
            if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) return;
            long timeMs = entity.level().getGameTime() * 50 + (long) (storedTickDelta * 50);
            Quaternionf rotation = new Quaternionf(animation.getCurrentGravityRotation(gravityDirection, timeMs));
            rotation.conjugate();
            rotation.mul(this.rotation);
            this.rotation.set(rotation.x(), rotation.y(), rotation.z(), rotation.w());
        }
    }
}
