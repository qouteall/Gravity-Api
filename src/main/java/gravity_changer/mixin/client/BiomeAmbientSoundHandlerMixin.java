package gravity_changer.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//method_26271 refers to a lambda which is why this class may cause mixin warnings/errors
@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeAmbientSoundHandlerMixin {
    @Redirect(
        method = "method_26271",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getEyeY()D"
        )
    )
    private double redirect_method_26271_getEyeY_0(LocalPlayer clientPlayerEntity) {
        return clientPlayerEntity.getEyePosition().y;
    }
    
    @Redirect(
        method = "method_26271",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"
        )
    )
    private double redirect_method_26271_getX_0(LocalPlayer clientPlayerEntity) {
        return clientPlayerEntity.getEyePosition().x;
    }
    
    @Redirect(
        method = "method_26271",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"
        )
    )
    private double redirect_method_26271_getZ_0(LocalPlayer clientPlayerEntity) {
        return clientPlayerEntity.getEyePosition().z;
    }
}
