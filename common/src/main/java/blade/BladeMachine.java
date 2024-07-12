package blade;

import blade.debug.BladeDebug;
import blade.debug.DebugFrame;
import blade.debug.ErrorOccurrence;
import blade.debug.ReportError;
import blade.impl.BladeImpl;
import blade.planner.score.ScorePlanner;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import blade.util.blade.BladeGoal;
import blade.util.blade.ConfigKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BladeMachine {
    protected final Bot bot;
    protected final ScoreState state = new ScoreState();
    protected final ScorePlanner planner = new ScorePlanner();
    protected final BladeDebug report = new BladeDebug();
    private final Map<ConfigKey<?>, Object> config = new HashMap<>();

    protected final List<BladeAction> actions = new ArrayList<>();
    protected BladeGoal goal = null;
    protected boolean enabled = false;
    protected BladeAction previousAction;
    protected DebugFrame frame;

    public BladeMachine(Bot bot) {
        this.bot = bot;
        registerDefault();
    }

    public void tick() {
        if (!enabled) return;
        if (goal == null) return;
        frame = report.newFrame();
        ReportError.wrap(() -> {
            for (BladeAction action : actions) {
                action.setBot(bot);
                action.setState(state);
            }
            goal.setBot(bot);
            goal.tick();
            produceState();
            frame.setState(state.copy());
            BladeAction action = planner.plan(actions, goal, state, frame.getPlanner());
            if (action == null) {
                previousAction = null;
                return;
            }
            if (previousAction != null && !action.equals(previousAction)) {
                ReportError.wrap(() -> previousAction.onRelease(action), frame, ErrorOccurrence.ACTION_RELEASE);
                ReportError.wrap(previousAction::prepare, frame, ErrorOccurrence.ACTION_PREPARE);
            }

            ReportError.wrap(action::onTick, frame, ErrorOccurrence.ACTION_TICK);
            ReportError.wrap(action::postTick, frame, ErrorOccurrence.ACTION_POST_TICK);
            previousAction = action;
        }, frame, ErrorOccurrence.OTHER);
    }

    public void produceState() {
        state.produce(bot);
    }

    public ScorePlanner getPlanner() {
        return planner;
    }

    public void setGoal(BladeGoal goal) {
        setEnabled(true);
        this.goal = goal;
    }

    public void addAction(BladeAction action) {
        actions.add(action);
    }

    public List<BladeAction> getActions() {
        return actions;
    }

    public ScoreState getState() {
        return state;
    }

    public BladeDebug getReport() {
        return report;
    }

    public DebugFrame getLastFrame() {
        return frame;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void causePanic(String detailedReason) {
        bot.destroy();
        DebugFrame frame = report.newFrame();
        frame.addError(ReportError.from(new Exception("Panic: " + detailedReason), ErrorOccurrence.PANIC));
    }

    public void registerDefault() {
        BladeImpl.register(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) {
        return (T) config.computeIfAbsent(key, ConfigKey::getDefaultValue);
    }

    public <T> void set(ConfigKey<T> key, T value) {
        config.put(key, value);
    }

    public BladeGoal getGoal() {
        return goal;
    }

    public void copy(BladeMachine otherBlade) {
        config.clear();
        config.putAll(otherBlade.config);
        actions.clear();
        actions.addAll(otherBlade.actions);
        goal = otherBlade.goal;
        enabled = otherBlade.enabled;
        previousAction = otherBlade.previousAction;
        frame = otherBlade.frame;
        planner.copy(otherBlade.planner);
    }
}
