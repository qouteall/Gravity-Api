package gravity_changer.util.packet;

import gravity_changer.api.RotationParameters;
import gravity_changer.util.Gravity;
import gravity_changer.util.GravityComponent;
import gravity_changer.util.NetworkUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;

public class OverwriteGravityPacket extends GravityPacket {
    public final ArrayList<Gravity> gravityList;
    public final boolean initialGravity;

    public OverwriteGravityPacket(ArrayList<Gravity> _gravityList, boolean _initialGravity){
        gravityList = _gravityList;
        initialGravity = _initialGravity;
    }

    public OverwriteGravityPacket(FriendlyByteBuf buf) {
        int listSize = buf.readInt();
        gravityList = new ArrayList<>();
        for (int i = 0; i < listSize; i++)
            gravityList.add(NetworkUtil.readGravity(buf));
        initialGravity = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(gravityList.size());
        for (Gravity gravity : gravityList) NetworkUtil.writeGravity(buf, gravity);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.setGravity(gravityList, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        Optional<Gravity> max = gravityList.stream().max(Comparator.comparingInt(Gravity::priority));
        if(max.isEmpty()) return new RotationParameters();
        return max.get().rotationParameters();
    }
}
