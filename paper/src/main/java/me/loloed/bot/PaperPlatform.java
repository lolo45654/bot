package me.loloed.bot;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.ServerPlatform;
import me.loloed.bot.api.util.ClientSimulator;
import me.loloed.bot.api.util.fake.FakeConnection;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executor;

public class PaperPlatform implements ServerPlatform {
    public static JavaPlugin PLUGIN;
    public static final List<Bot> BOTS = new ArrayList<>();
    public static final Executor EXECUTOR = new Executor() {
        @Override
        public void execute(@NotNull Runnable command) {
            Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, command);
        }
    };

    public void register(JavaPlugin plugin) {
        PLUGIN = plugin;

        try {
            FakePlayer.ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("cR");
            FakePlayer.ServerPlayer$spawnInvulnerableTime.setAccessible(true);
            ClientSimulator.LivingEntity$updatingUsingItem = LivingEntity.class.getDeclaredMethod("H");
            ClientSimulator.LivingEntity$updatingUsingItem.setAccessible(true);
            FakeConnection.Connection$channel = Connection.class.getDeclaredField("n");
            FakeConnection.Connection$channel.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                for (Bot bot : BOTS) {
                    if (bot.getVanillaPlayer() instanceof FakePlayer fakePlayer) {
                        fakePlayer.update(((CraftPlayer) event.getPlayer()).getHandle());
                    }
                }
            }

            @EventHandler
            public void onExplosion(EntityKnockbackEvent event) {
                if (event.getCause() == EntityKnockbackEvent.Cause.EXPLOSION) {
                    System.out.println("Player " + event.getEntity().getName() + " took " + event.getKnockback() + " knockback.");
                }
            }
        }, plugin);
    }

    public void addBot(Bot bot) {
        BOTS.add(bot);
        bot.getVanillaPlayer().getBukkitEntity().getScheduler().runAtFixedRate(PLUGIN, task -> {
            bot.doTick();
            if (bot.isDestroyed()) {
                bot.destroy();
                task.cancel();
            }
        }, null, 1L, 1L);
    }

    public void removeAll() {
        ArrayList<Bot> bots = new ArrayList<>(BOTS);
        for (Bot bot : bots) {
            bot.destroy();
        }
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
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
    public void destroyBot(Bot bot) {
        BOTS.remove(bot);
    }
}
