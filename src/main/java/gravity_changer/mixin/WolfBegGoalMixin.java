package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BegGoal.class)
public abstract class WolfBegGoalMixin {
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/goal/BegGoal;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getEyeY_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getEyeY();
        }
        
        return playerEntity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/goal/BegGoal;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getX()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getX_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getX();
        }
        
        return playerEntity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/goal/BegGoal;tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_tick_getZ_0(Player playerEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return playerEntity.getZ();
        }
        
        return playerEntity.getEyePosition().z;
    }
}
