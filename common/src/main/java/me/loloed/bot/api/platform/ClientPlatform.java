package me.loloed.bot.api.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public interface ClientPlatform extends Platform {
    InputConstants.Key getKey(KeyMapping mapping);

    @Override
    default boolean isClient() {
        return true;
    }
}
