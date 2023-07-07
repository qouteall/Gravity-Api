package gravity_changer;

import gravity_changer.util.GCUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientInit implements ClientModInitializer {
    private static final String ISSUE_LINK = "https://github.com/qouteall/GravityChanger/issues";
    private static boolean displayPreviewWarning = true;
    
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(new ClientTickEvents.StartTick() {
            @Override
            public void onStartTick(Minecraft client) {
                if (client.player == null) {
                    return;
                }
                if (displayPreviewWarning) {
                    displayPreviewWarning = false;
                    client.player.sendSystemMessage(
                        Component.translatable("gravity_changer.preview").append(
                            GCUtil.getLinkText(ISSUE_LINK)
                        )
                    );
                }
            }
        });
    }
}
