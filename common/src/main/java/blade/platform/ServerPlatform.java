package blade.platform;

import blade.Bot;
import blade.utils.fake.FakePlayer;
import io.netty.channel.Channel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;

public interface ServerPlatform extends Platform {
    void declareFakePlayer(FakePlayer player);

    ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry);

    void setSpawnInvulnerableTime(ServerPlayer instance, int time);

    void setChannel(Connection instance, Channel channel);

    void tickUsingItem(LivingEntity instance);

    int getPermissionLevel(CommandSourceStack source);

    void addBot(Bot bot);
}
