package gravity_changer.mixin.client;


import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleMixin {
    @Shadow
    @Final
    private Entity target;
    
    // TODO fix item pickup particle
    
//    @ModifyVariable(
//        method = "Lnet/minecraft/client/particle/ItemPickupParticle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/Entity;getX()D",
//            ordinal = 1,
//            shift = At.Shift.BEFORE
//        ),
//        ordinal = 0
//    )
//    private double modify_buildGeometry_double_0(double value) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
//        if (gravityDirection == Direction.DOWN) {
//            return value;
//        }
//
//        return value + RotationUtil.vecPlayerToWorld(0.0D, 0.5D, 0.0D, gravityDirection).x;
//    }
//
//    @ModifyVariable(
//        method = "Lnet/minecraft/client/particle/ItemPickupParticle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/Entity;getX()D",
//            ordinal = 1,
//            shift = At.Shift.BEFORE
//        ),
//        ordinal = 1
//    )
//    private double modify_buildGeometry_double_1(double value) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
//        if (gravityDirection == Direction.DOWN) {
//            return value;
//        }
//
//        return value - 0.5D + RotationUtil.vecPlayerToWorld(0.0D, 0.5D, 0.0D, gravityDirection).y;
//    }
//
//    @ModifyVariable(
//        method = "Lnet/minecraft/client/particle/ItemPickupParticle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/Entity;getX()D",
//            ordinal = 1,
//            shift = At.Shift.BEFORE
//        ),
//        ordinal = 2
//    )
//    private double modify_buildGeometry_double_2(double value) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
//        if (gravityDirection == Direction.DOWN) {
//            return value;
//        }
//
//        return value + RotationUtil.vecPlayerToWorld(0.0D, 0.5D, 0.0D, gravityDirection).z;
//    }
}
