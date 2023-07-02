package gravity_changer.util;

import gravity_changer.GravityChangerMod;
import gravity_changer.api.RotationParameters;
import gravity_changer.util.GravityChannel.Factory;
import gravity_changer.util.NetworkUtil.PacketMode;
import gravity_changer.util.packet.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

import static gravity_changer.util.NetworkUtil.*;

public class GravityChannel<P extends GravityPacket> {
    public static GravityChannel<OverwriteGravityPacket> OVERWRITE_GRAVITY = new GravityChannel<>(OverwriteGravityPacket::new, GravityChangerMod.id("overwrite_gravity_list"));
    public static GravityChannel<UpdateGravityPacket> UPDATE_GRAVITY = new GravityChannel<>(UpdateGravityPacket::new, GravityChangerMod.id("update_gravity_list"));
    public static GravityChannel<DefaultGravityPacket> DEFAULT_GRAVITY = new GravityChannel<>(DefaultGravityPacket::new, GravityChangerMod.id("default_gravity"));
    public static GravityChannel<DefaultGravityStrengthPacket> DEFAULT_GRAVITY_STRENGTH = new GravityChannel<>(DefaultGravityStrengthPacket::new, GravityChangerMod.id("default_gravity_strength"));
    public static GravityChannel<InvertGravityPacket> INVERT_GRAVITY = new GravityChannel<>(InvertGravityPacket::new, GravityChangerMod.id("inverted"));
    
    private final Factory<P> packetFactory;
    private final ResourceLocation channel;
    private final GravityVerifierRegistry<P> gravityVerifierRegistry;
    
    GravityChannel(Factory<P> _packetFactory, ResourceLocation _channel) {
        packetFactory = _packetFactory;
        channel = _channel;
        gravityVerifierRegistry = new GravityVerifierRegistry<>();
    }
    
    public void sendToClient(Entity entity, P packet, PacketMode mode) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entity.getId());
        packet.write(buf);
        sendToTracking(entity, channel, buf, mode);
    }
    
    public void receiveFromServer(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int entityId = buf.readInt();
        P packet = packetFactory.read(buf);
        client.execute(() -> {
            getGravityComponent(client, entityId).ifPresent(packet::run);
        });
    }
    
    public void sendToServer(P packet, ResourceLocation verifier, FriendlyByteBuf verifierInfoBuf) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        buf.writeResourceLocation(verifier);
        buf.writeByteArray(verifierInfoBuf.array());
        ClientPlayNetworking.send(channel, buf);
    }
    
    public void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        P packet = packetFactory.read(buf);
        ResourceLocation verifier = buf.readResourceLocation();
        FriendlyByteBuf verifierInfoBuf = PacketByteBufs.create();
        verifierInfoBuf.writeBytes(buf.readByteArray());
        server.execute(() -> {
            getGravityComponent(player).ifPresent(gc -> {
                GravityVerifierRegistry.VerifierFunction<P> v = gravityVerifierRegistry.get(verifier);
                if (v != null && v.check(player, verifierInfoBuf, packet)) {
                    packet.run(gc);
                    sendToClient(player, packet, PacketMode.EVERYONE_BUT_SELF);
                }
                else {
                    GravityChangerMod.LOGGER.info("VerifierFunction returned FALSE");
                    sendFullStatePacket(player, PacketMode.ONLY_SELF, packet.getRotationParameters(), false);
                }
            });
        });
    }
    
    public static void sendFullStatePacket(Entity entity, PacketMode mode, RotationParameters rp, boolean initialGravity) {
        getGravityComponent(entity).ifPresent(gc -> {
            OVERWRITE_GRAVITY.sendToClient(entity, new OverwriteGravityPacket(gc.getGravity(), initialGravity), mode);
            DEFAULT_GRAVITY.sendToClient(entity, new DefaultGravityPacket(gc.getDefaultGravityDirection(), rp, initialGravity), mode);
            INVERT_GRAVITY.sendToClient(entity, new InvertGravityPacket(gc.getInvertGravity(), rp, initialGravity), mode);
            DEFAULT_GRAVITY_STRENGTH.sendToClient(entity, new DefaultGravityStrengthPacket(gc.getGravityStrength()), mode);
        });
    }
    
    public GravityVerifierRegistry<P> getVerifierRegistry() {
        return gravityVerifierRegistry;
    }
    
    public void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(channel, this::receiveFromServer);
    }
    
    public void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(channel, this::receiveFromClient);
    }
    
    public static void initClient() {
        DEFAULT_GRAVITY.registerClientReceiver();
        UPDATE_GRAVITY.registerClientReceiver();
        OVERWRITE_GRAVITY.registerClientReceiver();
        INVERT_GRAVITY.registerClientReceiver();
        DEFAULT_GRAVITY_STRENGTH.registerClientReceiver();
    }
    
    public static void initServer() {
        DEFAULT_GRAVITY.registerServerReceiver();
        UPDATE_GRAVITY.registerServerReceiver();
        OVERWRITE_GRAVITY.registerServerReceiver();
        INVERT_GRAVITY.registerServerReceiver();
        DEFAULT_GRAVITY_STRENGTH.registerServerReceiver();
    }
    
    @FunctionalInterface
    interface Factory<T extends GravityPacket> {
        T read(FriendlyByteBuf buf);
    }
}