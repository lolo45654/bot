package blade.scheduler;

import blade.Bot;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotChain {
    public static BotChain of(Bot bot) {
        return new BotChain(bot);
    }

    private final Map<Long, List<Runnable>> scheduled = new HashMap<>();

    private final Bot bot;
    private long tick = 0L;

    protected BotChain(Bot bot) {
        this.bot = bot;
    }

    public BotChain at(long tick, Runnable task) {
        if (tick > this.tick) scheduled.computeIfAbsent(tick, point -> new ArrayList<>()).add(task);
        return this;
    }

    public BotChain in(long tickDelay, Runnable task) {
        return at(tick + Math.max(tickDelay, 1), task);
    }

    public void tick() {
        tick++;
        List<Runnable> tasks = scheduled.getOrDefault(tick, ImmutableList.of());
        for (Runnable task : tasks) {
            task.run();
        }
    }
}
