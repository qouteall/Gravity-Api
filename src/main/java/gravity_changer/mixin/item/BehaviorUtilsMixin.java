package gravity_changer.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BehaviorUtils.class)
public class BehaviorUtilsMixin {
    @WrapOperation(
        method = "throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)V",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
        )
    )
    private static ItemEntity onInitItemEntity(
        Level level, double posX, double posY, double posZ, ItemStack itemStack,
        Operation<ItemEntity> operation,
        @Local float yOffset, @Local LivingEntity entity
    ) {
        Vec3 eyeOffset = GravityChangerAPI.getEyeOffset(entity);
        Vec3 offset = eyeOffset.normalize().scale(yOffset);
        Vec3 itemPos = entity.position().add(eyeOffset).subtract(offset);
        ItemEntity itemEntity = operation.call(
            level, itemPos.x(), itemPos.y(), itemPos.z(), itemStack
        );
        GravityChangerAPI.setBaseGravityDirection(
            itemEntity,
            GravityChangerAPI.getGravityDirection(entity)
        );
        return itemEntity;
    }
    
    @WrapOperation(
        method = "throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private static void onSetDeltaMovement(
        ItemEntity itemEntity, Vec3 deltaMovement,
        Operation<Void> operation,
        @Local LivingEntity entity
    ) {
        GravityChangerAPI.setWorldVelocity(entity, deltaMovement);
    }
    
}
