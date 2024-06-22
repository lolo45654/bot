package me.loloed.bot.blade;

import com.mojang.authlib.GameProfile;
import me.loloed.bot.api.Bot;
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
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;

import java.util.UUID;

public class KitBot extends ServerBot {
    /**
     * This is used by the companion plugin "PersonalKits" for my practice server.
     * It will be called using reflection. Will be removing any other server bots.
     *
     * @param pos     Position of the new bot.
     * @param spawner Player that is spawning the bot.
     * @return the newly spawned bot as a bukkit player
     */
    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner) {
        for (Bot bot : PaperPlatform.BOTS) {
            if (bot instanceof ServerBot serverBot && serverBot.getSpawner().getUUID() == spawner.getUniqueId()) {
                bot.destroy();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(BladePlugin.platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "KitBot"));
        KitBot bot = new KitBot(fakePlayer, BladePlugin.platform, ((CraftPlayer) spawner).getHandle());
        BladePlugin.platform.addBot(bot);
        return fakePlayer.getBukkitEntity();
    }

    public KitBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner) {
        super(vanillaPlayer, platform, spawner, new ServerBotSettings());
    }

    @Override
    protected void tickShield(ServerPlayer player, Inventory inventory) {
    }

    @Override
    protected void tickHealing(ServerPlayer player) {
    }

    @Override
    protected void applyArmor(Inventory inventory) {
    }
}
