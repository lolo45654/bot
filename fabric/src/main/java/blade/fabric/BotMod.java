package blade.fabric;

import blade.utils.commands.ServerCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("blade");
    public static final FabricPlatform PLATFORM = new FabricPlatform();
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER = server;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PLATFORM.destroyAll();
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PLATFORM.tick();
        });
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> ServerCommands.register(PLATFORM, () -> SERVER, (source, permission) -> source.hasPermission(2), null, dispatcher)));
    }
}
