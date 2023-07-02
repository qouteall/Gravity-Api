package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.GravityComponent;
import gravity_changer.util.NetworkUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

public class DefaultGravityPacket extends GravityPacket {
    public final Direction direction;
    public final RotationParameters rotationParameters;
    public final boolean initialGravity;

    public DefaultGravityPacket(Direction _direction, RotationParameters _rotationParameters, boolean _initialGravity){
        direction = _direction;
        rotationParameters = _rotationParameters;
        initialGravity = _initialGravity;
    }

    public DefaultGravityPacket(FriendlyByteBuf buf){
        this(NetworkUtil.readDirection(buf), NetworkUtil.readRotationParameters(buf), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkUtil.writeDirection(buf, direction);
        NetworkUtil.writeRotationParameters(buf, rotationParameters);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.setDefaultGravityDirection(direction, rotationParameters, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        return rotationParameters;
    }
}