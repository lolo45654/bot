package blade.planner.score;

import blade.Bot;
import blade.debug.planner.ScoreDebug;
import blade.planner.Planner;
import blade.state.BladeState;
import blade.util.blade.BladeGoal;
import blade.util.blade.BladePlannedAction;

import java.util.*;

public class ScorePlanner implements Planner<ScoreDebug, ScoreAction> {
    private final Set<ScoreAction> actions = new HashSet<>();
    private double temperature = 1.0;
    private Random random = new Random();

    @Override
    public void refreshAll(Bot bot, BladeState state) {
        for (ScoreAction action : actions) {
            action.setBot(bot);
            action.setState(state);
        }
    }

    @Override
    public ScoreDebug createDebug() {
        ScoreDebug debug = new ScoreDebug();
        for (ScoreAction action : actions) {
            action.setParentDebugPlanner(debug);
        }
        return debug;
    }

    @Override
    public BladePlannedAction<ScoreAction> planInternal(BladeGoal goal, BladeState state, ScoreDebug debug) {
        Objects.requireNonNull(goal);
        goal.tick();
        debug.setTemperature(temperature);
        Map<ScoreAction, Score> scores = new HashMap<>();
        double totalWeight = 0.0;
        for (ScoreAction action : actions) {
            BladeState actionState = state.copy();
            action.getResult(actionState);
            double score = action.getScore();
            double scoreWithGoal = score + goal.getScore(actionState, state.difference(actionState));
            double weight = Math.pow(Math.E, scoreWithGoal / temperature);
            scores.put(action, new Score(score, scoreWithGoal, weight));
            totalWeight += weight;
        }

        for (Map.Entry<ScoreAction, Score> entry : scores.entrySet()) {
            Score score = entry.getValue();
            entry.setValue(new Score(score.score, score.scoreWithGoal, score.weight / totalWeight));
        }
        debug.setScores(scores);

        double rand = random.nextDouble();
        double cumulativeWeight = 0.0;
        for (Map.Entry<ScoreAction, Score> entry : scores.entrySet()) {
            cumulativeWeight += entry.getValue().weight;
            if (rand <= cumulativeWeight) {
                ScoreAction action = entry.getKey();
                debug.setActionTaken(action);
                return new BladePlannedAction<>(action);
            }
        }

        throw new IllegalStateException("Scored planner has reached an impossible state.");
    }

    public void addAction(ScoreAction action) {
        actions.add(action);
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public static record Score(double score, double scoreWithGoal, double weight) {
    }
}
