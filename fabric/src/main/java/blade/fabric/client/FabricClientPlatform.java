package blade.fabric.client;

import blade.Bot;
import blade.platform.ClientPlatform;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FabricClientPlatform implements ClientPlatform {
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    private Bot bot = null;

    @Override
    public ScheduledExecutorService getExecutor() {
        return EXECUTOR;
    }

    @Override
    public InputConstants.Key getKey(KeyMapping mapping) {
        return KeyBindingHelper.getBoundKeyOf(mapping);
    }

    @Override
    public void destroyBot(Bot bot) {
        this.bot = null;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public void onRespawn() {
        if (bot == null) return;
        Bot newBot = new Bot(Minecraft.getInstance().player, this);
        newBot.copy(bot);
        this.bot = newBot;
    }
}
