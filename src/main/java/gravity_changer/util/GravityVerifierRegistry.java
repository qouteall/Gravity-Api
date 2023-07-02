package gravity_changer.util;

import gravity_changer.GravityChangerMod;
import gravity_changer.util.packet.GravityPacket;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class GravityVerifierRegistry<T extends GravityPacket> {
    private final HashMap<ResourceLocation, VerifierFunction<T>> map = new HashMap<>();
    
    public void register(ResourceLocation id, VerifierFunction<T> func) {
        if (map.containsKey(id))
            GravityChangerMod.LOGGER.error(new Exception("Verifier function already set for identifier " + id));
        map.put(id, func);
    }
    
    @Nullable
    public VerifierFunction<T> get(ResourceLocation id) {
        return map.get(id);
    }
    
    public interface VerifierFunction<V extends GravityPacket> {
        boolean check(ServerPlayer player, FriendlyByteBuf verifierInfo, V packet);
    }
}
