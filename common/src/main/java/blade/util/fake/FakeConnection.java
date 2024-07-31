package blade.util.fake;

import blade.util.fake.FakePlayer.MovementSide;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
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
        super(PacketFlow.CLIENTBOUND);
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
        System.out.println("packet: " + obj.toString());
        if (obj instanceof ClientboundSetEntityMotionPacket) {
            player.setDeltaMovement(player.getDeltaMovement(MovementSide.SERVER), MovementSide.CLIENT);
        } else if (obj instanceof ClientboundExplodePacket packet) {
            player.setDeltaMovement(player.getDeltaMovement(MovementSide.CLIENT).add(packet.getKnockbackX(), packet.getKnockbackY(), packet.getKnockbackZ()), MovementSide.CLIENT);
        } else if (obj instanceof ClientboundEntityEventPacket packet) {
            System.out.println("SHHHHHHHH");
            if (packet.getEventId() != 31) return;
            System.out.println("31");
            if (!(packet.getEntity(player.level()) instanceof FishingHook hook)) return;
            System.out.println("received relevant packet");
            Entity hookedIn = hook.getHookedIn();
            if (hookedIn == null || !hookedIn.getUUID().equals(player.getUUID())) return;
            System.out.println("okay its ACTUALLY relevant!");
            // simulate #pullEntity
            Entity owner = hook.getOwner();
            if (owner == null) return;
            System.out.println("weeeeeeee");
            Vec3 vec3 = new Vec3(owner.getX() - hook.getX(), owner.getY() - hook.getY(), owner.getZ() - hook.getZ()).scale(0.1);
            player.setDeltaMovement(player.getDeltaMovement(MovementSide.CLIENT).add(vec3), MovementSide.CLIENT);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) {
    }
}
