package blade.platform;

import blade.Bot;

import java.util.concurrent.ScheduledExecutorService;

public interface Platform {
    ScheduledExecutorService getExecutor();

    boolean isClient();

    void destroyBot(Bot bot);
}
