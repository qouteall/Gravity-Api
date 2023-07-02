package gravity_changer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(
    name = "gravity_changer"
)
public class GravityChangerConfig implements ConfigData {
    public static boolean keepWorldLook = false;
    public static int rotationTime = 500;
    
    public static boolean server;
    public static boolean worldVelocity = false;
    
    public static double worldDefaultGravityStrength = 1;
    public static boolean resetGravityOnDimensionChange = true;
    public static boolean resetGravityOnRespawn = true;
    public static boolean voidDamageAboveWorld = false;
}
