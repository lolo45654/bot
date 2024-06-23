package me.loloed.bot.api.util.fake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FakeConnection extends Connection {
    public static Field Connection$channel;

    static {
        try {
            Connection$channel = Connection.class.getDeclaredField("channel");
        } catch (NoSuchFieldException e) {
        }
    }

    private final FakePlayer player;

    public FakeConnection(FakePlayer player) {
        super(PacketFlow.SERVERBOUND);
        this.player = player;
        try {
            Connection$channel.set(this, new EmbeddedChannel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
    }

    @Override
    public void send(Packet<?> obj, @Nullable PacketSendListener packetSendListener, boolean bl) {
        if (obj instanceof ClientboundSetEntityMotionPacket) {
            player.uglyAttackFix = true;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) {
    }
}
