package me.loloed.bot.api.util.fake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
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
            player.setDeltaMovement(player.serverSideDelta, false);
            player.setDeltaMovement(Vec3.ZERO, true);
        } else if (obj instanceof ClientboundExplodePacket packet) {
            Vec3 add = player.getDeltaMovement(false).add(packet.getKnockbackX(), packet.getKnockbackY(), packet.getKnockbackZ());
            System.out.println("BOT took " + add + " kb.");
            player.setDeltaMovement(add, false);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) {
    }
}
