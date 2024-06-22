package me.loloed.bot.api.util.fake;

import com.mojang.authlib.GameProfile;
import me.loloed.bot.api.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.EnumSet;

public class FakePlayer extends ServerPlayer {
    public static Field ServerPlayer$spawnInvulnerableTime;
    static {
        try {
            ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            ServerPlayer$spawnInvulnerableTime.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    private final ClientboundPlayerInfoUpdatePacket.Entry fakePlayerEntry;
    private final Platform platform;

    public Vec3 shieldDelta = Vec3.ZERO;
    /**
     * When attacked, a player receives a ClientboundSetEntityMotionPacket for themselves and the server sets
     * the server side velocity to zero (or the velocity before the attack). For the shield, it sets the delta movement
     * on the server, without telling the client. If you stun (shield break and attack at the same time, double click)
     * then the velocity of the shield is saved and sent due to the attack.
     */
    public boolean uglyAttackFix = false;

    public FakePlayer(Platform platform, MinecraftServer server, Vec3 pos, float yaw, float pitch, ServerLevel world, GameProfile profile) {
        super(server, world, profile, ClientInformation.createDefault());
        this.platform = platform;
        // Please save yourself the trouble and don't use setPosRaw. The bounding box is wrong with that.
        setPos(pos.x, pos.y, pos.z);
        setRot(yaw, pitch);
        platform.declareFakePlayer(this);
        fakePlayerEntry = new ClientboundPlayerInfoUpdatePacket.Entry(getUUID(), getGameProfile(), false, 0,
                GameType.SURVIVAL, getDisplayName(), null);
        connection = new ServerGamePacketListenerImpl(server, new FakeConnection(this), this, CommonListenerCookie.createInitial(profile));
        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer player : playerList.getPlayers()) {
            update(player);
        }

        // Bukkit.getPluginManager().registerEvents(this, PaperPlatform.PLUGIN);
        world.addNewPlayer(this);
        invulnerableTime = 0;
        try {
            ServerPlayer$spawnInvulnerableTime.set(this, 0);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.doTick();
        platform.detectEquipmentUpdates(this);
        uglyAttackFix = false;

        BlockPos blockBelow = this.getBlockPosBelowThatAffectsMyMovement();
        float f4 = level().getBlockState(blockBelow).getBlock().getFriction();

        double f = onGround() ? f4 * 0.91F : 0.91F;
        double shieldDeltaX = shieldDelta.x * f;
        double shieldDeltaY = shieldDelta.y * f;
        double shieldDeltaZ = shieldDelta.z * f;
        if (Math.abs(shieldDeltaX) < 0.003) {
            shieldDeltaX = 0.0;
        }
        if (Math.abs(shieldDeltaY) < 0.003) {
            shieldDeltaY = 0.0;
        }
        if (Math.abs(shieldDeltaZ) < 0.003) {
            shieldDeltaZ = 0.0;
        }
        shieldDelta = new Vec3(shieldDeltaX, shieldDeltaY, shieldDeltaZ);
    }

    /**
     * Just some default stuff.
     * 1. disconnect, not sure if this is needed.
     */
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        disconnect();
    }

    @Override
    public @NotNull String getScoreboardName() {
        return "BOT:" + getUUID();
    }

    /**
     * allows for server side ai
     */
    @Override
    public boolean isControlledByLocalInstance() {
        return true;
    }

    /**
     * allows for server side ai
     */
    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    public void update(ServerPlayer player) {
        player.connection.send(platform.buildPlayerInfoPacket(EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED), fakePlayerEntry));
    }

    @Override
    protected void blockUsingShield(LivingEntity livingEntity) {
        Vec3 a = getDeltaMovement();
        super.blockUsingShield(livingEntity);
        Vec3 b = getDeltaMovement();
        setDeltaMovement(a);
        if (getCooldowns().isOnCooldown(Items.SHIELD)) {
            shieldDelta = b.subtract(a);
        }
    }

    @Override
    public void setDeltaMovement(Vec3 vec3) {
        if (uglyAttackFix) {
            uglyAttackFix = false;
            return;
        }
        super.setDeltaMovement(vec3);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (Math.abs(shieldDelta.x) > 0.003 || Math.abs(shieldDelta.y) > 0.003 || Math.abs(shieldDelta.z) > 0.003) {
            setDeltaMovement(shieldDelta);
            shieldDelta = Vec3.ZERO;
        }
        return super.hurt(damageSource, f);
    }

    /**
     * performance
     */
    @Override
    public void awardStat(Stat<?> stat, int i) {
    }

    @Override
    public @NotNull String getIpAddress() {
        return "127.0.0.1";
    }
}
