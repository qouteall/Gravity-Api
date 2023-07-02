package gravity_changer.mixin;

import gravity_changer.GravityChangerMod;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {
    
    @Inject(
        method = "Lnet/minecraft/server/level/ServerPlayer;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void inject_moveToWorld_sendPacket_1(CallbackInfoReturnable<ServerPlayer> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((ServerPlayer) (Object) this);
        if (gravityDirection != GravityChangerAPI.getDefaultGravityDirection((ServerPlayer) (Object) this) && GravityChangerMod.config.resetGravityOnDimensionChange) {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayer) (Object) this, Direction.DOWN, new RotationParameters().rotationTime(0));
        }
        else {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayer) (Object) this, GravityChangerAPI.getDefaultGravityDirection((ServerPlayer) (Object) this), new RotationParameters().rotationTime(0));
        }
    }
    
    @Inject(
        method = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void inject_teleport_sendPacket_0(CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((ServerPlayer) (Object) this);
        if (gravityDirection != GravityChangerAPI.getDefaultGravityDirection((ServerPlayer) (Object) this) && GravityChangerMod.config.resetGravityOnDimensionChange) {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayer) (Object) this, Direction.DOWN, new RotationParameters().rotationTime(0));
        }
        else {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayer) (Object) this, GravityChangerAPI.getDefaultGravityDirection((ServerPlayer) (Object) this), new RotationParameters().rotationTime(0));
        }
    }
    
    @Inject(
        method = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V",
        at = @At("TAIL")
    )
    private void inject_copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (!GravityChangerMod.config.resetGravityOnRespawn) {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayer) (Object) this, GravityChangerAPI.getDefaultGravityDirection(oldPlayer), new RotationParameters().rotationTime(0));
        }
    }
}
