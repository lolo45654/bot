package me.loloed.bot.api.platform;

import me.loloed.bot.api.Bot;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;

import java.util.EnumSet;
import java.util.concurrent.Executor;

public interface Platform {
    Executor getExecutor();

    boolean isClient();

    void destroyBot(Bot bot);
}
