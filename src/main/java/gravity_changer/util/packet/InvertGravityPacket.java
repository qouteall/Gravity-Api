package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.GravityComponent;
import gravity_changer.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;

public class InvertGravityPacket extends GravityPacket{
    public final boolean inverted;
    public final RotationParameters rotationParameters;
    public final boolean initialGravity;

    public InvertGravityPacket(boolean _inverted, RotationParameters _rotationParameters, boolean _initialGravity){
        inverted = _inverted;
        rotationParameters = _rotationParameters;
        initialGravity = _initialGravity;
    }

    public InvertGravityPacket(FriendlyByteBuf buf) {
        this(
            buf.readBoolean(),
            NetworkUtil.readRotationParameters(buf),
            buf.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(inverted);
        NetworkUtil.writeRotationParameters(buf, rotationParameters);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.invertGravity(inverted, rotationParameters, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        return rotationParameters;
    }
}
