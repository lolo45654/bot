package me.loloed.bot.api.blade.planner;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladePlannedAction;
import me.loloed.bot.api.blade.debug.DebugPlanner;
import me.loloed.bot.api.blade.BladeAction;
import me.loloed.bot.api.blade.state.BladeState;

public interface Planner<Debug extends DebugPlanner, Action extends BladeAction<Action>> {
    void refreshAll(Bot bot, BladeState state);

    Debug createDebug();

    default BladePlannedAction<Action> plan(BladeState state, DebugPlanner parent) {
        Debug debug = createDebug();
        parent.setChildren(debug);
        return planInternal(state, debug);
    }

    BladePlannedAction<Action> planInternal(BladeState state, Debug debug);
}
