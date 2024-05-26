package me.loloed.bot.api.event;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.inventory.Slot;

public class InventoryEvents {
    public static final Event<MoveItem> MOVE_ITEM = new Event<>(MoveItem.class, callbacks -> (bot, from, to) -> {
        for (MoveItem callback : callbacks) {
            callback.onMoveItem(bot, from, to);
        }
    });

    @FunctionalInterface
    public interface MoveItem {
        void onMoveItem(Bot bot, Slot from, Slot to);
    }
}
