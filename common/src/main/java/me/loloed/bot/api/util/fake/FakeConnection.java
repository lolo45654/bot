package me.loloed.bot.api.util.fake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;

import java.lang.reflect.Field;

public class FakeConnection extends Connection {
    private static final Field CHANNEL_FIELD;

    static {
        try {
            CHANNEL_FIELD = Connection.class.getDeclaredField("channel");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public FakeConnection() {
        super(PacketFlow.SERVERBOUND);
        try {
            CHANNEL_FIELD.set(this, new EmbeddedChannel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) {
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
    }
}
