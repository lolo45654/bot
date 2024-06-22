package me.loloed.bot.blade;

import com.mojang.authlib.GameProfile;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.impl.goal.KillTargetGoal;
import me.loloed.bot.api.impl.IServerBot;
import me.loloed.bot.api.impl.ServerBot;
import me.loloed.bot.api.impl.ServerBotSettings;
import me.loloed.bot.api.platform.Platform;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class KitBot extends Bot implements IServerBot {
    /**
     * This is used by the companion plugin "PersonalKits" for my practice server.
     * It will be called using reflection. Will be removing any other server bots.
     *
     * @param pos     Position of the new bot.
     * @param spawner Player that is spawning the bot.
     * @param target  Entity to start killing.
     * @return the newly spawned bot as a bukkit player
     */
    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner, LivingEntity target) {
        for (Bot bot : PaperPlatform.BOTS) {
            if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(spawner.getUniqueId())) {
                bot.destroy();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(BladePlugin.platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "KitBot"));
        KitBot bot = new KitBot(fakePlayer, BladePlugin.platform, ((CraftPlayer) spawner).getHandle());
        BladeMachine blade = bot.getBlade();
        blade.set(ConfigKeys.TARGET, ((CraftLivingEntity) target).getHandle());
        blade.setGoal(new KillTargetGoal());
        BladePlugin.platform.addBot(bot);
        return fakePlayer.getBukkitEntity();
    }

    @Deprecated(forRemoval = true)
    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner) {
        for (Bot bot : PaperPlatform.BOTS) {
            if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(spawner.getUniqueId())) {
                bot.destroy();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(BladePlugin.platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "KitBot"));
        KitBot bot = new KitBot(fakePlayer, BladePlugin.platform, ((CraftPlayer) spawner).getHandle());
        BladePlugin.platform.addBot(bot);
        return fakePlayer.getBukkitEntity();
    }

    private final ServerPlayer spawner;
    private final ServerBotSettings settings;

    public KitBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner) {
        super(vanillaPlayer, platform);
        this.spawner = spawner;
        this.settings = new ServerBotSettings();
    }

    @Override
    protected void tick() {
        super.tick();
        ServerPlayer player = getVanillaPlayer();
        if (player.touchingUnloadedChunk() || player.distanceToSqr(spawner) > 120*120 || !player.server.getPlayerList().getPlayers().contains(spawner)) {
            destroy();
            return;
        }

        clientSimulator.setEntityReach(settings.reach);
    }

    @Override
    public ServerPlayer getSpawner() {
        return spawner;
    }

    @Override
    public ServerBotSettings getSettings() {
        return settings;
    }

    @Override
    public ServerPlayer getVanillaPlayer() {
        return (ServerPlayer) super.getVanillaPlayer();
    }
}
