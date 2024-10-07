package blade.utils.commands;

import blade.Bot;
import blade.platform.ServerPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotCommandSource extends CommandSourceStack {
    private final Bot bot;

    public BotCommandSource(ServerPlatform platform, Bot bot, CommandSourceStack source) {
        super(bot.getVanillaPlayer(), source.getPosition(), source.getRotation(), source.getLevel(), platform.getPermissionLevel(source), source.getTextName(), source.getDisplayName(), source.getServer(), bot.getVanillaPlayer(), source.isSilent(), source.callback(), source.getAnchor(), source.getSigningContext(), source.getChatMessageChainer());
        this.bot = bot;
    }

    @Override
    public @NotNull Entity getEntity() {
        return bot.getVanillaPlayer();
    }

    @Override
    public @NotNull Entity getEntityOrException() {
        return bot.getVanillaPlayer();
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return (ServerPlayer) bot.getVanillaPlayer();
    }

    @Override
    public @NotNull ServerPlayer getPlayerOrException() {
        return (ServerPlayer) bot.getVanillaPlayer();
    }

    @Override
    public @NotNull Vec2 getRotation() {
        return bot.getVanillaPlayer().getRotationVector();
    }

    @Override
    public @NotNull Vec3 getPosition() {
        return bot.getVanillaPlayer().getPosition(1.0f);
    }

    @Override
    public @NotNull ServerLevel getLevel() {
        return getPlayer().serverLevel();
    }
}
