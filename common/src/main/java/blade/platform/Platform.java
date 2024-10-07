package blade.platform;

import blade.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public interface Platform {
    ScheduledExecutorService getExecutor();

    void removeBot(Bot bot);

    /**
     * All bots that are active in the platform, returned type may be immutable.
     */
    List<Bot> getBots();

    default void destroyAll() {
        List<Bot> bots = new ArrayList<>(getBots());
        for (Bot bot : bots) {
            bot.destroy();
        }
    }
}
