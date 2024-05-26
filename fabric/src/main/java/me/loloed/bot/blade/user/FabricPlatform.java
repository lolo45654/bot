package me.loloed.bot.blade.user;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.Platform;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FabricPlatform extends Platform {
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

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
    public Executor getExecutor() {
        return EXECUTOR;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public ClientPlatform getClient() {
        return null;
    }

    @Override
    public void declareFakePlayer(FakePlayer player) {
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action> actions, net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry) {
        return null;
    }

    @Override
    public void destroyBot(Bot bot) {
        BOTS.remove(bot);
    }

    @Override
    public void detectEquipmentUpdates(net.minecraft.server.level.ServerPlayer player) {

    }
}
