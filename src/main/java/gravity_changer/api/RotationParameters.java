package gravity_changer.api;

import gravity_changer.GravityChangerMod;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.nbt.CompoundTag;

public record RotationParameters(
    boolean rotateVelocity,
    boolean rotateView,
    boolean alternateCenter,
    int rotationTimeMS
) {
    public static RotationParameters getDefault() {
        return new RotationParameters(
            GravityChangerMod.config.worldVelocity,
            !GravityChangerMod.config.keepWorldLook,
            false,
            GravityChangerMod.config.rotationTime
        );
    }
    
    public RotationParameters withRotationTimeMs(int rotationTimeMS) {
        return new RotationParameters(
            rotateVelocity,
            rotateView,
            alternateCenter,
            rotationTimeMS
        );
    }
    
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("RotateVelocity", rotateVelocity);
        tag.putBoolean("RotateView", rotateView);
        tag.putBoolean("AlternateCenter", alternateCenter);
        tag.putInt("RotationTimeMS", rotationTimeMS);
        return tag;
    }
    
    public static RotationParameters fromTag(CompoundTag tag) {
        return new RotationParameters(
            tag.getBoolean("RotateVelocity"),
            tag.getBoolean("RotateView"),
            tag.getBoolean("AlternateCenter"),
            tag.getInt("RotationTimeMS")
        );
    }
}
