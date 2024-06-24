package me.loloed.bot.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.xpple.clientarguments.arguments.CEntityArgumentType;
import dev.xpple.clientarguments.arguments.CEntitySelector;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.impl.goal.KillTargetGoal;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static me.loloed.bot.client.BotClientMod.LOGGER;
import static me.loloed.bot.client.BotClientMod.PLATFORM;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommands {
    public static final SimpleCommandExceptionType ENTITY_NOT_FOUND = new SimpleCommandExceptionType(() -> "Entity could not be found");
    public static final SimpleCommandExceptionType NOT_LIVING_ENTITY = new SimpleCommandExceptionType(() -> "Target needs to be a LivingEntity");
    public static final SimpleCommandExceptionType SELF_TARGET = new SimpleCommandExceptionType(() -> "Can't target yourself");

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("cbot")
                .then(literal("disable")
                        .executes(source -> {
                            Bot bot = PLATFORM.getBot();
                            if (bot == null) return -1;
                            bot.destroy();
                            LOGGER.info("Destroyed bot");
                            return 0;
                        }))
                .then(literal("enable")
                        .then(literal("kill")
                                .then(argument("target", CEntityArgumentType.entity())
                                        .executes(ctx -> {
                                            FabricClientCommandSource source = ctx.getSource();
                                            CEntitySelector arg = ctx.getArgument("target", CEntitySelector.class);
                                            Entity entity = arg.getEntity(source);
                                            if (entity == null) {
                                                throw ENTITY_NOT_FOUND.create();
                                            }
                                            if (entity == Minecraft.getInstance().player) {
                                                throw SELF_TARGET.create();
                                            }
                                            if (!(entity instanceof LivingEntity livingEntity)) {
                                                throw NOT_LIVING_ENTITY.create();
                                            }
                                            Bot bot = new Bot(Minecraft.getInstance().player, PLATFORM);
                                            BladeMachine blade = bot.getBlade();
                                            blade.setGoal(new KillTargetGoal(() -> {
                                                try {
                                                    Entity entity0 = arg.getEntity(source);
                                                    if (entity0 instanceof LivingEntity livingEntity0) return livingEntity0;
                                                    LOGGER.warn("Target is no longer a living entity.");
                                                } catch (CommandSyntaxException e) {
                                                    LOGGER.warn("Syntax error in tick get target.");
                                                }
                                                bot.destroy();
                                                return null;
                                            }));
                                            PLATFORM.setBot(bot);
                                            source.sendFeedback(Component.literal("Started the bot"));
                                            LOGGER.info("Started bot killing entity {}", livingEntity.getScoreboardName());
                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }
}
