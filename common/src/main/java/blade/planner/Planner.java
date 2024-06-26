package blade.planner;

import blade.Bot;
import blade.debug.DebugPlanner;
import blade.state.BladeState;
import blade.util.blade.BladeAction;
import blade.util.blade.BladeGoal;
import blade.util.blade.BladePlannedAction;

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
