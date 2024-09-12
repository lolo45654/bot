package blade.fabric.client;

import blade.BladeMachine;
import blade.Bot;
import blade.debug.DebugFrame;
import blade.debug.visual.VisualBox;
import blade.debug.visual.VisualDebug;
import blade.debug.visual.VisualText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotClientMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("blade");
    public static final FabricClientPlatform PLATFORM = new FabricClientPlatform();
    public static FabricClientCommandSource CLIENT_SOURCE;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Bot bot = PLATFORM.getBot();
            if (bot != null) {
                if (client.player == null) {
                    bot.destroy();
                    return;
                }
                bot.doTick();
            }
        });
        WorldRenderEvents.START.register(context -> {
            Bot bot = PLATFORM.getBot();
            if (bot != null) {
                bot.updateRotation();
            }
        });
        WorldRenderEvents.LAST.register(context -> {
            if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
            MultiBufferSource bufferSource = context.consumers();
            PoseStack stack = context.matrixStack();
            Bot bot = PLATFORM.getBot();
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            if (bufferSource == null || stack == null || bot == null || !camera.isInitialized()) return;
            BladeMachine blade = bot.getBlade();
            if (blade.getGoal() == null) return;
            DebugFrame frame = blade.getLastFrame();
            if (frame == null) return;

            for (VisualDebug visual : frame.visuals()) {
                if (visual instanceof VisualBox(AABB box, float red, float green, float blue, float alpha)) {
                    DebugRenderer.renderFilledBox(stack, bufferSource, box.move(camera.getPosition().reverse()), red, green, blue, alpha);
                } else if (visual instanceof VisualText(Vec3 pos, String text)) {
                    DebugRenderer.renderFloatingText(stack, bufferSource, text, pos.x, pos.y, pos.z, -1);
                }
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((ClientCommands::register));
    }
}
