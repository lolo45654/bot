package me.loloed.bot.api.util.fake;

import com.mojang.authlib.GameProfile;
import me.loloed.bot.api.platform.Platform;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.EnumSet;

public class FakePlayer extends ServerPlayer {
    public static final Field ServerPlayer$spawnInvulnerableTime;
    static {
        try {
            ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            ServerPlayer$spawnInvulnerableTime.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final ClientboundPlayerInfoUpdatePacket.Entry fakePlayerEntry;
    private final Platform platform;

    public FakePlayer(Platform platform, MinecraftServer server, Vec3 pos, float yaw, float pitch, ServerLevel world, GameProfile profile) {
        super(server, world, profile, ClientInformation.createDefault());
        this.platform = platform;
        // Please save yourself the trouble and don't use setPosRaw. The bounding box is wrong with that.
        setPos(pos.x, pos.y, pos.z);
        setRot(yaw, pitch);
        platform.declareFakePlayer(this);
        fakePlayerEntry = new ClientboundPlayerInfoUpdatePacket.Entry(getUUID(), getGameProfile(), false, 0,
                GameType.SURVIVAL, getDisplayName(), null);
        connection = new ServerGamePacketListenerImpl(server, new FakeConnection(), this, CommonListenerCookie.createInitial(profile));
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
        platform.detectEquipmentUpdates(this);
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
}
