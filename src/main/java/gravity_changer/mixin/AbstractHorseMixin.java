package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractHorse.class)
public class AbstractHorseMixin {
    @ModifyVariable(method = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;calculateFallDamage(FF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float diminishFallDamage(float value) {
        return value * (float) Math.sqrt(GravityChangerAPI.getGravityStrength(((Entity) (Object) this)));
    }
}
