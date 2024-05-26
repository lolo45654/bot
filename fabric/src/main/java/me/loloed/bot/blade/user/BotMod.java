package me.loloed.bot.blade.user;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("blade");

    @Override
    public void onInitializeClient() {
        /*

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (platform != null && platform.getBot() != null) {
                platform.getBot().tick();
            }
        });
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(literal("cblade")
                    .then(literal("disable")
                            .executes(source -> {
                                FabricBot bot = platform.getBot();
                                if (bot != null) {
                                    bot.destroy();
                                }
                                return Command.SINGLE_SUCCESS;
                            }))
                    .then(literal("enable")
                            .then(literal("test")
                                    .then(argument("target", CEntityArgumentType.entity())
                                            .executes(ctx -> {
                                                FabricClientCommandSource source = ctx.getSource();
                                                Entity entity = CEntityArgumentType.getCEntity(ctx, "target");
                                                if (entity == null) {
                                                    source.sendError(Text.of("Entity isn't within render distance!"));
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                if (entity == MinecraftClient.getInstance().player) {
                                                    source.sendError(Text.of("Can't target yourself!"));
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                FabricEntity fabricEntity = FabricEntity.from(entity);
                                                platform.create();
                                                BladeMachine module = new BladeMachine();
                                                BladeImpl.register(module);
                                                StateEntity stateTarget = StateEntity.fromUUID(entity.getUuid());
                                                module.addGoal(new KillEntityGoal(stateTarget));
                                                module.getState().set(EntityTarget.TARGET, stateTarget);
                                                module.getState().set(stateTarget.getUUID(), entity.getUuid());
                                                platform.getBot().addModule(module);
                                                source.sendFeedback(Text.literal("Starting the bot!"));
                                                return Command.SINGLE_SUCCESS;
                                            })))));
        }));
         */
    }
}
