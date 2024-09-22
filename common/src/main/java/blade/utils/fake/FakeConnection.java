package blade.utils.fake;

import blade.platform.ServerPlatform;
import blade.utils.fake.FakePlayer.MovementSide;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.jetbrains.annotations.Nullable;

public class FakeConnection extends Connection {
    private final FakePlayer player;

    public FakeConnection(ServerPlatform platform, FakePlayer player) {
        super(PacketFlow.CLIENTBOUND);
        this.player = player;
        platform.setChannel(this, new EmbeddedChannel());
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
    }

    @Override
    public void send(Packet<?> obj, @Nullable PacketSendListener packetSendListener, boolean bl) {
        if (obj instanceof ClientboundSetEntityMotionPacket) {
            player.setDeltaMovement(player.getDeltaMovement(MovementSide.SERVER), MovementSide.CLIENT);
        } else if (obj instanceof ClientboundExplodePacket packet) {
            player.setDeltaMovement(player.getDeltaMovement(MovementSide.CLIENT).add(packet.getKnockbackX(), packet.getKnockbackY(), packet.getKnockbackZ()), MovementSide.CLIENT);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) {
    }
}
