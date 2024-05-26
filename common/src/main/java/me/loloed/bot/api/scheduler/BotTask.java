package me.loloed.bot.api.scheduler;

import me.loloed.bot.api.Bot;

public interface BotTask {
    void run(Bot bot);
}
