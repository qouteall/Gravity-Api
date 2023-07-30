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
    public int rotationTime = 500;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean worldVelocity = false;
    
    public double gravityStrengthMultiplier = 1.0;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean resetGravityOnRespawn = true;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean voidDamageAboveWorld = true;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean voidDamageOnHorizontalFallTooFar = true;
    
    public boolean autoJumpOnGravityPlateInnerCorner = true;
    public boolean adjustPositionAfterChangingGravity = true;
}
