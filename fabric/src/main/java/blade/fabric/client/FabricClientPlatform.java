package blade.fabric.client;

import blade.Bot;
import blade.platform.ClientPlatform;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FabricClientPlatform implements ClientPlatform {
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    private Bot bot = null;

    @Override
    public Executor getExecutor() {
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
}
