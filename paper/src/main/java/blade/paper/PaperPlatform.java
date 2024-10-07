package blade.paper;

import blade.Bot;
import blade.platform.ServerPlatform;
import blade.util.ClientSimulator;
import blade.util.fake.FakeConnection;
import blade.util.fake.FakePlayer;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PaperPlatform implements ServerPlatform {
    public static JavaPlugin PLUGIN;
    public static final List<Bot> BOTS = new ArrayList<>();
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    public void register(JavaPlugin plugin) {
        PLUGIN = plugin;

        try {
            FakePlayer.ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("cS");
            FakePlayer.ServerPlayer$spawnInvulnerableTime.setAccessible(true);
            ClientSimulator.LivingEntity$updatingUsingItem = LivingEntity.class.getDeclaredMethod("J");
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
            public void onEntityAddToWorldEvent(EntityAddToWorldEvent event) {
                ChunkMap chunkMap = ((CraftWorld) event.getWorld()).getHandle().getChunkSource().chunkMap;
                System.out.println("entity add to world event: entity.seenBy.size() = " + chunkMap.entityMap.get(event.getEntity().getEntityId()).seenBy.size());

            }
        }, plugin);
    }

    public void addBot(Bot bot) {
        BOTS.add(bot);
        bot.getVanillaPlayer().getBukkitEntity().getScheduler().runAtFixedRate(PLUGIN, task -> {
            bot.doTick();

            ChunkMap chunkMap = ((ServerLevel) bot.getVanillaPlayer().level()).getChunkSource().chunkMap;
            for (Entity nearbyEntity : bot.getVanillaPlayer().getBukkitLivingEntity().getNearbyEntities(32, 32, 32)) {
                if (!(nearbyEntity instanceof FishHook)) continue;
                ChunkMap.TrackedEntity trackedEntity = chunkMap.entityMap.get(nearbyEntity.getEntityId());
                System.out.println("fishingHook.seenBy.contains(bot) = " + trackedEntity.seenBy.contains(((ServerPlayer) bot.getVanillaPlayer()).connection));
            }
            System.out.println("level.players().contains(bot) = " + (((ServerLevel) bot.getVanillaPlayer().level()).players().contains(bot.getVanillaPlayer())));
            System.out.println("entityMap.contains(bot) = " + (chunkMap.entityMap.containsKey(bot.getVanillaPlayer().getId())));
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
    public ScheduledExecutorService getExecutor() {
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
