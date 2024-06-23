package me.loloed.bot.api.blade.planner;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeGoal;
import me.loloed.bot.api.blade.BladePlannedAction;
import me.loloed.bot.api.blade.debug.DebugPlanner;
import me.loloed.bot.api.blade.BladeAction;
import me.loloed.bot.api.blade.state.BladeState;

public interface Planner<Debug extends DebugPlanner, Action extends BladeAction<Action>> {
    void refreshAll(Bot bot, BladeState state);

    Debug createDebug();

    default BladePlannedAction<Action> plan(BladeGoal goal, BladeState state, DebugPlanner parent) {
        Debug debug = createDebug();
        parent.setChildren(debug);
        return planInternal(goal, state, debug);
    }

    BladePlannedAction<Action> planInternal(BladeGoal goal, BladeState state, Debug debug);
}
