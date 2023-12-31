package gravity_changer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ClientPacketListener.class, priority = 1001)
public abstract class ClientPacketListenerMixin {
    @Shadow
    @Final
    private Map<UUID, PlayerInfo> playerInfoMap;
    
    @Redirect(
        method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleGameEvent(Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D",
            ordinal = 0
        )
    )
    private double redirect_onGameStateChange_getEyeY_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getEyeY();
        }
        
        return playerEntity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleGameEvent(Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getX()D",
            ordinal = 0
        )
    )
    private double redirect_onGameStateChange_getX_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getX();
        }
        
        return playerEntity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleGameEvent(Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_onGameStateChange_getZ_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getZ();
        }
        
        return playerEntity.getEyePosition().z;
    }
    
    @WrapOperation(
        method = "handleExplosion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 wrapOperation_onExplosion_add_0(
        Vec3 vec3d, double x, double y, double z, Operation<Vec3> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(
            Minecraft.getInstance().player
        );
        if (gravityDirection == Direction.DOWN) {
            return original.call(vec3d, x, y, z);
        }
        
        Vec3 player = RotationUtil.vecWorldToPlayer(x, y, z, gravityDirection);
        return original.call(vec3d, player.x, player.y, player.z);
    }
}
