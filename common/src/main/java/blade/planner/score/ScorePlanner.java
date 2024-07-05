package blade.planner.score;

import blade.debug.planner.ScorePlannerDebug;
import blade.util.blade.BladeGoal;

import java.util.*;

public class ScorePlanner {
    private double temperature = 1.0;
    private Random random = new Random();

    public <Action extends ScoreAction> Action plan(List<Action> actions, BladeGoal goal, ScoreState state, ScorePlannerDebug debug) {
        Objects.requireNonNull(goal);
        debug.setTemperature(temperature);
        Map<Action, Score> scores = new HashMap<>();
        double totalWeight = 0.0;
        for (Action action : actions) {
            if (!action.isSatisfied()) {
                scores.put(action, new Score(0, 0, 0, false));
                continue;
            }
            ScoreState actionState = state.copy();
            action.getResult(actionState);
            double score = Math.max(action.getScore(), 0);
            double scoreWithGoal = score + Math.max(goal.getScore(actionState, state.difference(actionState)), 0);
            double weight = Math.pow(Math.E, scoreWithGoal / temperature);
            scores.put(action, new Score(score, scoreWithGoal, weight, true));
            totalWeight += weight;
        }

        for (Map.Entry<Action, Score> entry : scores.entrySet()) {
            Score score = entry.getValue();
            entry.setValue(new Score(score.score, score.scoreWithGoal, score.weight / totalWeight, score.satisfied));
        }
        debug.setScores((Map<ScoreAction, Score>) scores);

        double rand = random.nextDouble();
        double cumulativeWeight = 0.0;
        for (Map.Entry<Action, Score> entry : scores.entrySet()) {
            if (!entry.getValue().satisfied) continue;
            cumulativeWeight += entry.getValue().weight;
            if (rand <= cumulativeWeight) {
                Action action = entry.getKey();
                debug.setActionTaken(action);
                return action;
            }
        }

        throw new IllegalStateException("Scored planner has reached an impossible state.");
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

    public static record Score(double score, double scoreWithGoal, double weight, boolean satisfied) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "S: %.3f SG: %.3f W: %.3f C: %s", score, scoreWithGoal, weight, satisfied);
        }
    }
}
