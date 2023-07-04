package gravity_changer.api;

import gravity_changer.GravityChangerMod;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

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
}
