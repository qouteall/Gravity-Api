package gravity_changer.api;

import gravity_changer.GravityChangerMod;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import gravity_changer.config.GravityChangerConfig;
import net.minecraft.nbt.CompoundTag;

// TODO refactor
public record RotationParameters(
    boolean rotateVelocity,
    boolean rotateView,
    int rotationTimeMS
) {
    public static RotationParameters defaultParam = new RotationParameters(
        true, true, 500
    );
    
    public static void updateDefault() {
        defaultParam = new RotationParameters(
            !GravityChangerConfig.worldVelocity,
            true,
            GravityChangerConfig.rotationTime
        );
    }
    
    public static RotationParameters getDefault() {
        return defaultParam;
    }
    
    public RotationParameters withRotationTimeMs(int rotationTimeMS) {
        return new RotationParameters(
            rotateVelocity,
            rotateView,
            rotationTimeMS
        );
    }
    
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("RotateVelocity", rotateVelocity);
        tag.putBoolean("RotateView", rotateView);
        tag.putInt("RotationTimeMS", rotationTimeMS);
        return tag;
    }
    
    public static RotationParameters fromTag(CompoundTag tag) {
        return new RotationParameters(
            tag.getBoolean("RotateVelocity"),
            tag.getBoolean("RotateView"),
            tag.getInt("RotationTimeMS")
        );
    }
}
