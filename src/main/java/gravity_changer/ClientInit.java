package gravity_changer;

import gravity_changer.util.GravityChannel;
import net.fabricmc.api.ClientModInitializer;

public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GravityChannel.initClient();
    }
}
