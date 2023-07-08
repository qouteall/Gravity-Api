package gravity_changer;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import gravity_changer.util.RotationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The gravity effect.
 * Having a gravity effect can override the base gravity direction and other gravity direction modifiers.
 * (The gravity strength modifiers are not overridden.)
 * For multiple gravity effects, the one with the highest priority is used.
 * For a continuous gravity effect, it should be applied every tick.
 * It's recommended to use it for only things like gravity field that updated every tick.
 */
public record GravityEffect(
    @Nullable Direction direction,
    double strengthMultiplier,
    double priority,
    Vec3 sourcePos,
    ResourceKey<Level> sourceDim
) {

}
