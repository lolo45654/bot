package me.loloed.bot.api.blade.planner.score;

import me.loloed.bot.api.blade.state.BladeState;

public interface ScoreGoal {
    double getScore(BladeState state, BladeState difference);

    String getName();
}
