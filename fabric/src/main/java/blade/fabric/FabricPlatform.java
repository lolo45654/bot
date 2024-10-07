package blade.fabric;

import blade.Bot;
import blade.fabric.mixin.*;
import blade.platform.ServerPlatform;
import blade.utils.fake.FakePlayer;
import io.netty.channel.Channel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FabricPlatform implements ServerPlatform {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final List<Bot> bots = new ArrayList<>();

    public FabricPlatform() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<Bot> bots = new ArrayList<>(this.bots);
            for (Bot bot : bots) {
                bot.doTick();
            }
        });
    }

    @Override
    public void addBot(Bot bot) {
        bots.add(bot);
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void declareFakePlayer(FakePlayer player) {
    }

    @Override
    public ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, List.of());
        ((ClientboundPlayerInfoUpdatePacketAccessor) packet).setEntries(List.of(entry));
        return packet;
    }

    @Override
    public void setSpawnInvulnerableTime(ServerPlayer instance, int time) {
        ((ServerPlayerAccessor) instance).setSpawnInvulnerableTime(time);
    }

    @Override
    public void setChannel(Connection instance, Channel channel) {
        ((ConnectionAccessor) instance).setChannel(channel);
    }

    @Override
    public void tickUsingItem(LivingEntity instance) {
        ((LivingEntityAccessor) instance).updatingUsingItem();
    }

    @Override
    public void removeBot(Bot bot) {
        bots.remove(bot);
    }

    @Override
    public List<Bot> getBots() {
        return bots;
    }

    public void tick() {
        for (Bot bot : new ArrayList<>(bots)) {
            bot.doTick();
            if (bot.isDestroyed()) {
                bot.destroy();
            }
        }
    }

    @Override
    public int getPermissionLevel(CommandSourceStack source) {
        return ((CommandSourceStackAccessor) source).getPermissionLevel();
    }
}
