package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.Entity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceBottleEntity.class)
public class ExperienceBottleEntityMixin {
    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    private float multiplyGravity(float original) {
        return original * (float)GravityChangerAPI.getGravityStrength(((Entity) (Object) this));
    }
}
