package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.GravityComponent;
import net.minecraft.network.FriendlyByteBuf;

public class DefaultGravityStrengthPacket extends GravityPacket {
    public final double strength;
    
    public DefaultGravityStrengthPacket(double _strength) {
        strength = _strength;
    }
    
    public DefaultGravityStrengthPacket(FriendlyByteBuf buf) {
        this(buf.readDouble());
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(strength);
    }
    
    @Override
    public void run(GravityComponent gc) {
        gc.setDefaultGravityStrength(strength);
    }
    
    @Override
    public RotationParameters getRotationParameters() {
        return null;
    }
}