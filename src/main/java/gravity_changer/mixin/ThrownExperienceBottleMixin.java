package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin {
    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    private float multiplyGravity(float original) {
        return original * (float) GravityChangerAPI.getGravityStrength(((Entity) (Object) this));
    }
}
