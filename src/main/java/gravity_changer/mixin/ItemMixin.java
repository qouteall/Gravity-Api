package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Item.class, priority = 1001)
public class ItemMixin {
    @WrapOperation(
        method="getPlayerPOVHitResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
            ordinal = 0
        )
    )
    private static float wrapOperation_raycast_getYaw(Player player, Operation<Float> original){
        Direction direction = GravityChangerAPI.getGravityDirection(player);
        if(direction == Direction.DOWN) return original.call(player);
        return RotationUtil.rotPlayerToWorld(original.call(player), player.getXRot(), direction).x;
    }
    
    @WrapOperation(
        method="getPlayerPOVHitResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getXRot()F",
            ordinal = 0
        )
    )
    private static float wrapOperation_raycast_getPitch(Player player, Operation<Float> original){
        Direction direction = GravityChangerAPI.getGravityDirection(player);
        if(direction == Direction.DOWN) return original.call(player);
        return RotationUtil.rotPlayerToWorld(player.getYRot(), original.call(player), direction).y;
    }
}
