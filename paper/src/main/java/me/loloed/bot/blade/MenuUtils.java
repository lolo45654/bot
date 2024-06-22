package me.loloed.bot.blade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;

public class MenuUtils {
    public static ComponentWrapper wrapMiniMessage(String miniMessage) {
        return new AdventureComponentWrapper(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    public static ComponentWrapper empty() {
        return new AdventureComponentWrapper(Component.empty());
    }
}
