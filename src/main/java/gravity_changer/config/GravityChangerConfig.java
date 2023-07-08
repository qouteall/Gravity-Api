package gravity_changer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(
    name = "gravity_changer"
)
public class GravityChangerConfig implements ConfigData {
//    @ConfigEntry.Gui.Tooltip(count = 2)
//    public static boolean keepWorldLook = false;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static int rotationTime = 500;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static boolean worldVelocity = false;
    
    public static double gravityStrengthMultiplier = 1.0;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static boolean resetGravityOnRespawn = true;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static boolean voidDamageAboveWorld = false;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static boolean voidDamageOnHorizontalFallTooFar = false;
    
    public static boolean autoJumpOnGravityPlateInnerCorner = true;
}
