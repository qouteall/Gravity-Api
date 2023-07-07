package gravity_changer.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GCUtil {
    public static MutableComponent getLinkText(String link) {
        return Component.literal(link).withStyle(
            style -> style.withClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL, link
            )).withUnderlined(true)
        );
    }
}
