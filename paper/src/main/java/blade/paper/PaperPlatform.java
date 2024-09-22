package blade.paper;

import blade.Bot;
import blade.platform.ServerPlatform;
import blade.utils.fake.FakePlayer;
import io.netty.channel.Channel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PaperPlatform implements ServerPlatform {
    private final JavaPlugin plugin;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final List<Bot> bots = new ArrayList<>();
    private final Field ServerPlayer$spawnInvulnerableTime;
    private final Method LivingEntity$updatingUsingItem;
    private final Field Connection$channel;

    public PaperPlatform(JavaPlugin plugin) {
        this.plugin = plugin;

        try {
            ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("cS");
            ServerPlayer$spawnInvulnerableTime.setAccessible(true);
            LivingEntity$updatingUsingItem = LivingEntity.class.getDeclaredMethod("J");
            LivingEntity$updatingUsingItem.setAccessible(true);
            Connection$channel = Connection.class.getDeclaredField("n");
            Connection$channel.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                for (Bot bot : bots) {
                    if (bot.getVanillaPlayer() instanceof FakePlayer fakePlayer) {
                        fakePlayer.update(((CraftPlayer) event.getPlayer()).getHandle());
                    }
                }
            }
        }, plugin);
    }

    public void addBot(Bot bot) {
        bots.add(bot);
        bot.getVanillaPlayer().getBukkitEntity().getScheduler().runAtFixedRate(plugin, task -> {
            bot.doTick();
            if (bot.isDestroyed()) {
                bot.destroy();
                task.cancel();
            }
        }, null, 1L, 1L);
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void declareFakePlayer(FakePlayer player) {
        player.isRealPlayer = false;
    }

    @Override
    public ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry) {
        return new ClientboundPlayerInfoUpdatePacket(actions, entry);
    }

    @Override
    public void removeBot(Bot bot) {
        bots.remove(bot);
    }

    @Override
    public List<Bot> getBots() {
        return bots;
    }

    @Override
    public void setSpawnInvulnerableTime(ServerPlayer instance, int time) {
        try {
            ServerPlayer$spawnInvulnerableTime.set(instance, time);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setChannel(Connection instance, Channel channel) {
        try {
            Connection$channel.set(instance, channel);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tickUsingItem(LivingEntity instance) {
        try {
            LivingEntity$updatingUsingItem.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPermissionLevel(CommandSourceStack source) {
        return source.source instanceof ServerPlayer player ? MinecraftServer.getServer().getProfilePermissions(player.gameProfile) : 0;
    }
}
