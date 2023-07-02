package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.GravityComponent;
import net.minecraft.network.PacketByteBuf;

public class DefaultGravityStrengthPacket extends GravityPacket {
    public final double strength;

    public DefaultGravityStrengthPacket(double _strength){
        strength = _strength;
    }

    public DefaultGravityStrengthPacket(PacketByteBuf buf){
        this(buf.readDouble());
    }

    @Override
    public void write(PacketByteBuf buf) {
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