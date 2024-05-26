package me.loloed.bot.api.platform;

import com.mojang.blaze3d.platform.InputConstants;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.concurrent.Executor;

public abstract class Platform {
    public abstract Executor getExecutor();

    public abstract boolean isClient();

    public abstract ClientPlatform getClient();

    public abstract void detectEquipmentUpdates(ServerPlayer player);

    public abstract void declareFakePlayer(FakePlayer player);

    public abstract ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<Action> actions, Entry entry);

    public abstract void destroyBot(Bot bot);

    public static abstract class ClientPlatform {
        public abstract InputConstants.Key getKey(KeyMapping mapping);
    }
}
