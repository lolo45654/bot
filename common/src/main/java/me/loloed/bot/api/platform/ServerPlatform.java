package me.loloed.bot.api.platform;

import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;

public interface ServerPlatform extends Platform {
    void declareFakePlayer(FakePlayer player);

    ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry);

    @Override
    default boolean isClient() {
        return false;
    }
}
