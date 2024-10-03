package blade;

import blade.debug.BladeDebug;
import blade.debug.DebugFrame;
import blade.debug.ReportError;
import blade.debug.planner.ScorePlannerDebug;
import blade.debug.visual.VisualDebug;
import blade.impl.BladeImpl;
import blade.impl.StateKeys;
import blade.planner.score.ScorePlanner;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeAction;
import blade.utils.blade.BladeGoal;
import blade.utils.blade.ConfigKey;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BladeMachine {
    private static final Logger LOGGER = LoggerFactory.getLogger(BladeMachine.class);

    protected final Bot bot;
    protected final ScoreState state = new ScoreState();
    protected final ScorePlanner planner = new ScorePlanner();
    protected final BladeDebug report = new BladeDebug(new ArrayList<>());
    protected final Map<ConfigKey<?>, Object> config = new HashMap<>();
    protected final List<BladeAction> actions = new ArrayList<>();
    protected final AIManager aiManager;

    protected BladeGoal goal = null;
    protected BladeAction previousAction;
    protected DebugFrame frame;
    protected List<VisualDebug> visuals = new ArrayList<>();

    public BladeMachine(Bot bot) {
        this.bot = bot;
        this.aiManager = new AIManager(bot);
        registerDefault();
    }

    public void tick() {
        if (goal == null) return;

        ScoreState stateCopy = null;
        ScorePlannerDebug plannerDebug = null;
        Throwable error = null;
        double reward = 0.0;
        visuals = new ArrayList<>();
        try {
            for (BladeAction action : actions) {
                action.setBot(bot);
                action.setState(state);
            }
            goal.setBot(bot);
            goal.tick();
            produceState();
            stateCopy = state.copy();

            BladeAction action = planner.plan(actions, aiManager, goal, state);
            plannerDebug = planner.getLastDebug();
            if (action == null) {
                previousAction = null;
                return;
            } else if (previousAction != null && !action.equals(previousAction)) {
                previousAction.onRelease(action);
                previousAction.prepare();
            }

            action.onTick();
            action.postTick();
            previousAction = action;
            reward = goal.getReward();
            aiManager.learn(reward, state, plannerDebug.scores());
        } catch (Throwable throwable) {
            LOGGER.warn("Uncaught exception in Blade.", throwable);
            error = throwable;
        } finally {
            frame = new DebugFrame(error == null ? ImmutableList.of() : ImmutableList.of(ReportError.from(error)), stateCopy, plannerDebug, ImmutableList.copyOf(visuals), reward);
            report.addTick(frame);
        }
    }

    public void produceState() {
        state.produce(bot);
    }

    public ScorePlanner getPlanner() {
        return planner;
    }

    public void setGoal(BladeGoal goal) {
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

    public AIManager getAIManager() {
        return aiManager;
    }

    public void registerDefault() {
        StateKeys.register(state);
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

    public void addVisualDebug(VisualDebug visual) {
        visuals.add(visual);
    }

    public void copy(BladeMachine otherBlade) {
        config.clear();
        config.putAll(otherBlade.config);
        actions.clear();
        actions.addAll(otherBlade.actions);
        goal = otherBlade.goal;
        previousAction = otherBlade.previousAction;
        frame = otherBlade.frame;
        planner.copy(otherBlade.planner);
        aiManager.copy(otherBlade.aiManager, actions);
    }
}
