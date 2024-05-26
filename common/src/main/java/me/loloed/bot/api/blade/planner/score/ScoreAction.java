package me.loloed.bot.api.blade.planner.score;

import me.loloed.bot.api.blade.BladeAction;
import me.loloed.bot.api.blade.state.BladeState;

public abstract class ScoreAction extends BladeAction<ScoreAction> {
    @Override
    public void prepare() {
    }

    public abstract void getResult(BladeState result);

    public abstract double getScore();
}
