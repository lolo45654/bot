package me.loloed.bot.api.blade;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.planner.score.ScoreGoal;
import me.loloed.bot.api.blade.state.BladeState;

public abstract class BladeGoal implements ScoreGoal {
    protected final String name;
    protected Bot bot;

    public BladeGoal(String name) {
        this.name = name;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return name;
    }
}
