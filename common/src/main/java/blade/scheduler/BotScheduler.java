package blade.scheduler;

import blade.Bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotScheduler {
    private final Map<Long, List<BotTask>> scheduled = new HashMap<>();
    private final ScheduledExecutorService asyncExecutor;
    private final Bot bot;
    private long tick = 0L;

    public BotScheduler(Bot bot, ScheduledExecutorService asyncExecutor) {
        this.bot = bot;
        this.asyncExecutor = asyncExecutor;
    }

    public void schedule(long tickDelay, BotTask task) {
        scheduled.computeIfAbsent(tick + Math.max(1, tickDelay), point -> new ArrayList<>()).add(task);
    }

    public void scheduleAtRate(long tickRate, BotTask task) {
        final BotTask initialTask = task;
        task = new BotTask() {
            @Override
            public void run(Bot bot) {
                initialTask.run(bot);
                schedule(tickRate, this);
            }
        };
        schedule(tickRate, task);
    }

    public void scheduleAsync(long tickDelay, BotTask task) {
        asyncExecutor.schedule(() -> task.run(bot), tickDelay * 50, TimeUnit.MILLISECONDS);
    }

    public void scheduleAtRateAsync(long tickRate, BotTask task) {
        asyncExecutor.scheduleAtFixedRate(() -> task.run(bot), tickRate * 50, tickRate * 50, TimeUnit.MILLISECONDS);
    }

    public void runAsync(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

    public void tick() {
        tick++;
        List<BotTask> tasks = scheduled.get(tick);
        if (tasks != null) {
            for (BotTask task : tasks) {
                task.run(bot);
            }
        }
    }
}
