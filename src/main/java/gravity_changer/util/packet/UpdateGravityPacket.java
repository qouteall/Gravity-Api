package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.Gravity;
import gravity_changer.util.GravityComponent;
import gravity_changer.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateGravityPacket extends GravityPacket{
    public final Gravity gravity;
    public final boolean initialGravity;

    public UpdateGravityPacket(Gravity _gravity, boolean _initialGravity){
        gravity =  _gravity;
        initialGravity = _initialGravity;
    }

    public UpdateGravityPacket(FriendlyByteBuf buf) {
        this(
            NetworkUtil.readGravity(buf),
            buf.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkUtil.writeGravity(buf, gravity);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.addGravity(gravity, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        return gravity.rotationParameters();
    }
}
