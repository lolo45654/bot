package blade.bot;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.impl.goal.KillTargetGoal;
import blade.paper.BotPlugin;
import blade.platform.Platform;
import blade.utils.fake.FakePlayer;
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

import java.util.ArrayList;
import java.util.List;

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
        List<Bot> bots = new ArrayList<>(BotPlugin.PLATFORM.getBots());
        for (Bot bot : bots) {
            if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(spawner.getUniqueId())) {
                bot.destroy();
            }
        }

        FakePlayer fakePlayer = new FakePlayer(BotPlugin.PLATFORM, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
        KitBot bot = new KitBot(fakePlayer, BotPlugin.PLATFORM, ((CraftPlayer) spawner).getHandle());
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
        BotPlugin.PLATFORM.addBot(bot);
        return fakePlayer.getBukkitEntity();
    }

    public static org.bukkit.entity.Player create(Location pos, org.bukkit.entity.Player spawner, LivingEntity target) {
        return create(pos, spawner, target, 0.5f, 0.3);
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
