package blade.utils.commands;

import blade.BladeMachine;
import blade.Bot;
import blade.bot.IServerBot;
import blade.bot.ServerBot;
import blade.bot.ServerBotSettings;
import blade.impl.goal.KillTargetGoal;
import blade.platform.ServerPlatform;
import blade.utils.fake.FakePlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entity;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.vec3;

public class ServerCommands {
    private static final int PRIMARY = 0xFFCBD5;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType UNHANDLED_EXCEPTION_ERROR = new SimpleCommandExceptionType(Component.literal("An exception occurred while executing this command."));

    public static void register(ServerPlatform platform, Server server, @NotNull Permission permission, @Nullable Gui gui, @NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("bot")
                .requires(ctx -> permission.hasPermission(ctx, "bot.use"))
                .then(literal("totem")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.totem"))
                        .executes(handleException(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            List<Bot> bots = new ArrayList<>(platform.getBots());
                            for (Bot bot : bots) {
                                if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(player.getUUID())) {
                                    bot.destroy();
                                    player.sendSystemMessage(Component.literal("Removed your simple bot.").withColor(PRIMARY));
                                    return 0;
                                }
                            }

                            Vec3 position = ctx.getSource().getPosition();
                            Vec2 rotation = ctx.getSource().getRotation();
                            ServerLevel world = ctx.getSource().getLevel();
                            FakePlayer fakePlayer = new FakePlayer(platform, server.getServer(), position, rotation.x, rotation.y, world, IServerBot.getProfile());
                            ServerBot bot = new ServerBot(fakePlayer, platform, player, ServerBotSettings.TOTEM.clone());
                            platform.addBot(bot);
                            player.sendSystemMessage(Component.literal("Spawned a totem bot.").withColor(PRIMARY));
                            return 0;
                        })))
                .then(literal("shield")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.shield"))
                        .executes(handleException(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            List<Bot> bots = new ArrayList<>(platform.getBots());
                            for (Bot bot : bots) {
                                if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(player.getUUID())) {
                                    bot.destroy();
                                    player.sendSystemMessage(Component.literal("Removed your simple bot.").withColor(PRIMARY));
                                    return 0;
                                }
                            }

                            Vec3 position = ctx.getSource().getPosition();
                            Vec2 rotation = ctx.getSource().getRotation();
                            ServerLevel world = ctx.getSource().getLevel();
                            FakePlayer fakePlayer = new FakePlayer(platform, server.getServer(), position, rotation.x, rotation.y, world, IServerBot.getProfile());
                            ServerBot bot = new ServerBot(fakePlayer, platform, player, ServerBotSettings.SHIELD.clone());
                            platform.addBot(bot);
                            player.sendSystemMessage(Component.literal("Spawned a shield bot.").withColor(PRIMARY));
                            return 0;
                        })))
                .then(literal("spawn")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.spawn"))
                        .then(createPossibleBots(platform, argument("where", vec3()), ctx -> {
                            Vec3 pos = Vec3Argument.getVec3(ctx, "where");
                            return new FakePlayer(platform, server.getServer(), pos, 0, 0, ctx.getSource().getLevel(), IServerBot.getProfile());
                        })))
                .then(literal("control")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.control"))
                        .then(createPossibleBots(platform, argument("who", player()), ctx -> EntityArgument.getPlayer(ctx, "who"))))
                .then(literal("removeall")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.removeall"))
                        .executes(handleException(ctx -> {
                            platform.destroyAll();
                            ctx.getSource().sendSuccess(() -> Component.literal("Removed all bots"), true);
                            return 0;
                        })))
                .then(literal("count")
                        .requires(ctx -> permission.hasPermission(ctx, "bot.count"))
                        .executes(handleException(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(platform.getBots().size() + " bot(s) exist."), false);
                            return 0;
                        })))
                .executes(handleException(ctx -> {
                    if (gui == null) {
                        ctx.getSource().sendFailure(Component.literal("Sorry, this platform doesn't have a gui."));
                        return -1;
                    }

                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                    for (Bot b : platform.getBots()) {
                        if (b instanceof IServerBot bot && bot.getSpawner().getUUID().equals(sender.getUUID())) {
                            gui.show(sender, bot);
                            return 1;
                        }
                    }

                    gui.show(sender, null);
                    return 1;
                })));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> createPossibleBots(ServerPlatform platform, ArgumentBuilder<CommandSourceStack, ?> tree, CommandFunction<CommandSourceStack, ServerPlayer> playerGetter) {
        CommandFunction<CommandSourceStack, Bot> botGetter = ctx -> {
            Bot bot = new Bot(playerGetter.get(ctx), platform);
            platform.addBot(bot);
            return bot;
        };
        CommandFunction<CommandSourceStack, Bot> kill = ctx -> {
            EntitySelector targetSelector = ctx.getArgument("target", EntitySelector.class);

            Entity target = targetSelector.findSingleEntity(ctx.getSource());
            if (!(target instanceof LivingEntity)) return null;
            Bot bot = botGetter.get(ctx);
            BotCommandSource botSource = new BotCommandSource(platform, bot, ctx.getSource());
            BladeMachine blade = bot.getBlade();
            blade.setGoal(new KillTargetGoal(() -> {
                try {
                    Entity entity0 = targetSelector.findSingleEntity(botSource);
                    if (entity0 instanceof LivingEntity livingEntity0) return livingEntity0;
                } catch (CommandSyntaxException ignored) {
                }
                return null;
            }));
            return bot;
        };

        return tree
                .then(literal("kill")
                        .then(argument("target", entity())
                                .then(literal("copy")
                                        .executes(handleException(ctx -> {
                                            Bot bot = kill.get(ctx);
                                            if (bot == null) return -1;
                                            Inventory fromInv = ctx.getSource().getPlayerOrException().getInventory();
                                            Inventory toInv = bot.getVanillaPlayer().getInventory();
                                            for (int i = 0; i < fromInv.getContainerSize(); i++) {
                                                toInv.setItem(i, fromInv.getItem(i).copy());
                                            }
                                            ctx.getSource().sendSuccess(() -> Component.literal("Spawned a Bot"), false);
                                            return 0;
                                        })))
                                .executes(handleException(ctx -> {
                                    Bot bot = kill.get(ctx);
                                    if (bot == null) return -1;
                                    ctx.getSource().sendSuccess(() -> Component.literal("Spawned a Bot"), false);
                                    return 0;
                                }))))
                .then(literal("test")
                        .executes(handleException(ctx -> {
                            Bot bot = botGetter.get(ctx);
                            bot.setMoveRight(true);
                            return 0;
                        })));
    }

    private static Command<CommandSourceStack> handleException(Command<CommandSourceStack> command) {
        return ctx -> {
            try {
                return command.run(ctx);
            } catch (Throwable throwable) {
                if (throwable instanceof CommandSyntaxException) throw throwable;
                LOGGER.error("Running command failed", throwable);
                throw UNHANDLED_EXCEPTION_ERROR.create();
            }
        };
    }

    public interface Gui {
        void show(@NotNull ServerPlayer viewer, @Nullable IServerBot bot);
    }

    public interface Permission {
        boolean hasPermission(CommandSourceStack source, String permission);
    }

    public interface Server {
        MinecraftServer getServer();
    }
}
