package gravity_changer.mixin.client;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//method_26271 refers to a lambda which is why this class may cause mixin warnings/errors
@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeEffectSoundPlayerMixin {
    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEyeY()D",
                    ordinal = 0
            )
    )
    private double redirect_method_26271_getEyeY_0(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getEyeY();
        }

        return clientPlayerEntity.getEyePosition().y;
    }

    @Redirect(
            method = "Lnet/minecraft/client/resources/sounds/BiomeAmbientSoundsHandler;lambda$tick$3(Lnet/minecraft/world/level/biome/AmbientMoodSettings;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 0
            )
    )
    private double redirect_method_26271_getX_0(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getX();
        }

        return clientPlayerEntity.getEyePosition().x;
    }

    @Redirect(
            method = "Lnet/minecraft/client/resources/sounds/BiomeAmbientSoundsHandler;lambda$tick$3(Lnet/minecraft/world/level/biome/AmbientMoodSettings;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double redirect_method_26271_getZ_0(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getZ();
        }

        return clientPlayerEntity.getEyePosition().z;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEyeY()D",
                    ordinal = 1
            )
    )
    private double redirect_method_26271_getEyeY_1(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getEyeY();
        }

        return clientPlayerEntity.getEyePosition().y;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 1
            )
    )
    private double redirect_method_26271_getX_1(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getX();
        }

        return clientPlayerEntity.getEyePosition().x;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 1
            )
    )
    private double redirect_method_26271_getZ_1(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getZ();
        }

        return clientPlayerEntity.getEyePosition().z;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEyeY()D",
                    ordinal = 2
            )
    )
    private double redirect_method_26271_getEyeY_2(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getEyeY();
        }

        return clientPlayerEntity.getEyePosition().y;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D",
                    ordinal = 2
            )
    )
    private double redirect_method_26271_getX_2(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getX();
        }

        return clientPlayerEntity.getEyePosition().x;
    }

    @Redirect(
            method = {"m_hhelwrkg","method_26271"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D",
                    ordinal = 2
            )
    )
    private double redirect_method_26271_getZ_2(LocalPlayer clientPlayerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(clientPlayerEntity);
        if(gravityDirection == Direction.DOWN) {
            return clientPlayerEntity.getZ();
        }

        return clientPlayerEntity.getEyePosition().z;
    }
}
