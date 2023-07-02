package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderMan.class)
public abstract class EndermanEntityMixin {
    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D",
                    ordinal = 0
            )
    )
    private double redirect_isPlayerStaring_getEyeY_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if(gravityDirection == Direction.DOWN) {
            return playerEntity.getEyeY();
        }

        return playerEntity.getEyePosition().y;
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getX()D",
                    ordinal = 0
            )
    )
    private double redirect_isPlayerStaring_getX_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if(gravityDirection == Direction.DOWN) {
            return playerEntity.getX();
        }

        return playerEntity.getEyePosition().x;
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getZ()D",
                    ordinal = 0
            )
    )
    private double redirect_isPlayerStaring_getZ_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if(gravityDirection == Direction.DOWN) {
            return playerEntity.getZ();
        }

        return playerEntity.getEyePosition().z;
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;teleportTowards(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getEyeY()D",
                    ordinal = 0
            )
    )
    private double redirect_teleportTo_getEyeY_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN) {
            return entity.getEyeY();
        }

        return entity.getEyePosition().y;
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;teleportTowards(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 0
            )
    )
    private double redirect_teleportTo_getX_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN) {
            return entity.getX();
        }

        return entity.getEyePosition().x;
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/monster/EnderMan;teleportTowards(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = 0
            )
    )
    private double redirect_teleportTo_getZ_0(Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }

        return entity.getEyePosition().z;
    }
}
