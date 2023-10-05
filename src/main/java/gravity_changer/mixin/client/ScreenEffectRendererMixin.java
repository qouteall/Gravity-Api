package gravity_changer.mixin.client;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(
        method = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void inject_getInWallBlockState(Player player, CallbackInfoReturnable<BlockState> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.cancel();
        
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        
        Vec3 eyePos = player.getEyePosition();
        Vector3f multipliers = RotationUtil.vecPlayerToWorld(player.getBbWidth() * 0.8F, 0.1F, player.getBbWidth() * 0.8F, gravityDirection);
        for (int i = 0; i < 8; ++i) {
            double d = eyePos.x + (double) (((float) ((i >> 0) % 2) - 0.5F) * multipliers.x());
            double e = eyePos.y + (double) (((float) ((i >> 1) % 2) - 0.5F) * multipliers.y());
            double f = eyePos.z + (double) (((float) ((i >> 2) % 2) - 0.5F) * multipliers.z());
            mutable.set(d, e, f);
            BlockState blockState = player.level().getBlockState(mutable);
            if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(player.level(), mutable)) {
                cir.setReturnValue(blockState);
            }
        }
        
        cir.setReturnValue(null);
    }
}
