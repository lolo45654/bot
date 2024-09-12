package blade.fabric;

import blade.Bot;
import blade.fabric.mixin.ClientboundPlayerInfoUpdatePacketAccessor;
import blade.platform.ServerPlatform;
import blade.utils.fake.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FabricPlatform implements ServerPlatform {
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    public static final List<Bot> BOTS = new ArrayList<>();

    public FabricPlatform() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<Bot> bots = new ArrayList<>(BOTS);
            for (Bot bot : bots) {
                bot.doTick();
            }
        });
    }

    public void removeAll() {
        List<Bot> bots = new ArrayList<>(BOTS);
        for (Bot bot : bots) {
            bot.destroy();
        }
    }

    public void addBot(Bot bot) {
        BOTS.add(bot);
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return EXECUTOR;
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
    public void destroyBot(Bot bot) {
        BOTS.remove(bot);
    }
}
