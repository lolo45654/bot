package blade.platform;

import blade.Bot;

import java.util.concurrent.Executor;

public interface Platform {
    Executor getExecutor();

    boolean isClient();

    void destroyBot(Bot bot);
}
