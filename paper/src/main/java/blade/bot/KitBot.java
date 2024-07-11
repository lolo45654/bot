package blade.bot;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.impl.goal.KillTargetGoal;
import blade.paper.BotPlugin;
import blade.paper.PaperPlatform;
import blade.platform.Platform;
import blade.util.fake.FakePlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
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
    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner, LivingEntity target, float difficulty, double temperature) {
        for (int i = 0; i < PaperPlatform.BOTS.size(); i++) {
            Bot bot = PaperPlatform.BOTS.get(i);
            if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(spawner.getUniqueId())) {
                bot.destroy();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(BotPlugin.platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
        KitBot bot = new KitBot(fakePlayer, BotPlugin.platform, ((CraftPlayer) spawner).getHandle());
        BladeMachine blade = bot.getBlade();
        blade.set(ConfigKeys.DIFFICULTY, difficulty);
        blade.getPlanner().setTemperature(temperature);
        blade.setGoal(new KillTargetGoal(() -> {
            if (target.isDead()) {
                bot.destroy();
                return null;
            }
            return ((CraftLivingEntity) target).getHandle();
        }));
        BotPlugin.platform.addBot(bot);
        return fakePlayer.getBukkitEntity();
    }

    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner, LivingEntity target) {
        return create(pos, spawner, target, 0.5f, 0.3);
    }

    @Deprecated(forRemoval = true)
    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner) {
        for (Bot bot : PaperPlatform.BOTS) {
            if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(spawner.getUniqueId())) {
                bot.destroy();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(BotPlugin.platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "KitBot"));
        KitBot bot = new KitBot(fakePlayer, BotPlugin.platform, ((CraftPlayer) spawner).getHandle());
        BotPlugin.platform.addBot(bot);
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

        player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(settings.reach);
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
