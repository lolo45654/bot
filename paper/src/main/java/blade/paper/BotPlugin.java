package blade.paper;

import blade.bot.ServerBotSettings;
import blade.utils.commands.ServerCommands;
import com.mojang.brigadier.CommandDispatcher;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class BotPlugin extends JavaPlugin {
    public static final TextColor PRIMARY = TextColor.color(0xFFCBD5);
    public static PaperPlatform PLATFORM;

    @Override
    public void onLoad() {
        super.onLoad();
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        new Metrics(this, 22946);

        PLATFORM = new PaperPlatform(this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            ServerCommands.register(PLATFORM,
                    MinecraftServer::getServer,
                    (source, permission) -> source.source.getBukkitSender(source).hasPermission(permission),
                    (viewer, bot) -> BotSettingGui.show(viewer, PLATFORM, bot == null ? new ServerBotSettings() : bot.getSettings(), bot),
                    (CommandDispatcher<CommandSourceStack>) (Object) event.registrar().getDispatcher());
        });
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        PLATFORM.destroyAll();
    }
}
