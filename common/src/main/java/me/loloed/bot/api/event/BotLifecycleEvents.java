package me.loloed.bot.api.event;

import me.loloed.bot.api.Bot;

public class BotLifecycleEvents {
    public static final Event<BotDestroy> BOT_DESTROY = new Event<>(BotDestroy.class, callbacks -> bot -> {
        for (BotDestroy callback : callbacks) {
            callback.onDestroy(bot);
        }
    });

    public static final Event<TickStart> TICK_START = new Event<>(TickStart.class, callbacks -> bot -> {
        for (TickStart callback : callbacks) {
            callback.onTickStart(bot);
        }
    });

    public static final Event<TickEnd> TICK_END = new Event<>(TickEnd.class, callbacks -> bot -> {
        for (TickEnd callback : callbacks) {
            callback.onTickEnd(bot);
        }
    });

    @FunctionalInterface
    public interface BotDestroy {
        void onDestroy(Bot bot);
    }

    @FunctionalInterface
    public interface TickStart {
        void onTickStart(Bot bot);
    }

    @FunctionalInterface
    public interface TickEnd {
        void onTickEnd(Bot bot);
    }
}
