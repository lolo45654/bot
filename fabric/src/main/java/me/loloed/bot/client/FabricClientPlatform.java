package me.loloed.bot.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.ClientPlatform;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FabricClientPlatform implements ClientPlatform {
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    private Bot bot = null;

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    @Override
    public InputConstants.Key getKey(KeyMapping mapping) {
        return KeyBindingHelper.getBoundKeyOf(mapping);
    }

    @Override
    public void destroyBot(Bot bot) {
        this.bot = null;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
