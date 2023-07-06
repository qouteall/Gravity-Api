package gravity_changer.mixin.fall_distance;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin_FallDistance {
    
    // seems encountered a bug in MixinExtras?
//    // make sure fall distance is correct on server side of the player
//    @WrapOperation(
//        method = "doCheckFallDamage",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
//        )
//    )
//    private void wrapCheckFallDamage(
//        // target arguments
//        ServerPlayer this_, double y, boolean onGround, BlockState state, BlockPos pos,
//        Operation<Void> original,
//        // outer arguments
//        double dx, double dy, double dz, boolean bl
//    ) {
//        Direction gravity = GravityChangerAPI.getGravityDirection(this_);
//
//        Vec3 localVec = RotationUtil.vecWorldToPlayer(dx, dy, dz, gravity);
//        original.call(((ServerPlayer) this_), localVec.y, onGround, state, pos);
//    }
    
}
