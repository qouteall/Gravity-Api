package gravity_changer.util;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.Optional;


public class NetworkUtil {
    //PacketMode
    public enum PacketMode{
        EVERYONE,
        EVERYONE_BUT_SELF,
        ONLY_SELF
    }

    //Access gravity component

    public static Optional<GravityComponent> getGravityComponent(Minecraft client, int entityId){
        if(client.level == null) return Optional.empty();
        Entity entity = client.level.getEntity(entityId);
        if(entity == null) return Optional.empty();
        GravityComponent gc = GravityChangerAPI.getGravityComponent(entity);
        if(gc == null) return Optional.empty();
        return Optional.of(gc);
    }

    public static Optional<GravityComponent> getGravityComponent(Entity entity){
        GravityComponent gc = GravityChangerAPI.getGravityComponent(entity);
        if(gc == null) return Optional.empty();
        return Optional.of(gc);
    }

    //Sending packets to players that are tracking an entity

    public static void sendToTracking(Entity entity, ResourceLocation channel, FriendlyByteBuf buf, PacketMode mode){
        //PlayerLookup.tracking(entity) might not return the player if entity is a player, so it has to be done separately
        if(mode != PacketMode.EVERYONE_BUT_SELF)
            if(entity instanceof ServerPlayer player)
                ServerPlayNetworking.send(player, channel, buf);
        if(mode != PacketMode.ONLY_SELF)
            for (ServerPlayer player : PlayerLookup.tracking(entity))
                if(player != entity)
                    ServerPlayNetworking.send(player, channel, buf);
    }

    //Writing to buffer

    public static void writeDirection(FriendlyByteBuf buf, Direction direction){
        buf.writeByte(direction == null ? -1 : direction.get3DDataValue());
    }

    public static void writeRotationParameters(FriendlyByteBuf buf, RotationParameters rotationParameters){
        buf.writeBoolean(rotationParameters.rotateVelocity());
        buf.writeBoolean(rotationParameters.rotateView());
        buf.writeBoolean(rotationParameters.alternateCenter());
        buf.writeInt(rotationParameters.rotationTime());
    }

    public static void writeGravity(FriendlyByteBuf buf, Gravity gravity){
        writeDirection(buf, gravity.direction());
        buf.writeInt(gravity.priority());
        buf.writeDouble(gravity.strength());
        buf.writeInt(gravity.duration());
        buf.writeUtf(gravity.source());
        writeRotationParameters(buf, gravity.rotationParameters());
    }

    //Reading from buffer

    public static RotationParameters readRotationParameters(FriendlyByteBuf buf){
        return new RotationParameters(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    public static Direction readDirection(FriendlyByteBuf buf){
        int rawDirection = buf.readByte();
        return (0 <= rawDirection && rawDirection < Direction.values().length) ? Direction.from3DDataValue(rawDirection) : null;
    }

    public static Gravity readGravity(FriendlyByteBuf buf){
        return new Gravity(
                readDirection(buf),
                buf.readInt(),
                buf.readDouble(),
                buf.readInt(),
                buf.readUtf(),
                readRotationParameters(buf)
        );
    }
}