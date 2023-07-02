package gravity_changer.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;

@Mixin(ExperienceBottleItem.class)
public abstract class ExperienceBottleItemMixin extends Item {

    public ExperienceBottleItemMixin(Settings settings) {
        super(settings);
    }

//    @WrapOperation(
//            method = "use",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/entity/projectile/thrown/ExperienceBottleEntity;setProperties(Lnet/minecraft/entity/Entity;FFFFF)V"
//            )
//    )
//    public void wrapOperation_use_setProperties(ExperienceBottleEntity target, Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ, Operation<Void> original) {
//        original.call(target, user, pitch, yaw, 0.0F, modifierZ, modifierXYZ);
//    }

}



