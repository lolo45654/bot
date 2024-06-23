package me.loloed.bot.client;

import me.loloed.bot.api.Bot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotClientMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("blade");
    public static final FabricClientPlatform PLATFORM = new FabricClientPlatform();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Bot bot = PLATFORM.getBot();
            if (bot != null) {
                bot.doTick();
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((ClientCommands::register));
    }
}
