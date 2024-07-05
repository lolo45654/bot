package blade.planner.score;

import blade.Bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreState {
    public static final Set<StateKey> KEYS = new HashSet<>();

    private final Map<StateKey, Double> values = new HashMap<>();

    public ScoreState() {
        updateValues();
    }

    public void updateValues() {
        for (StateKey key : KEYS) {
            if (values.containsKey(key)) continue;
            values.put(key, 0.0);
        }
    }

    public double getValue(StateKey key) {
        KEYS.add(key);
        return values.computeIfAbsent(key, o -> 0.0);
    }

    public void setValue(StateKey key, double value) {
        KEYS.add(key);
        values.put(key, value);
    }

    public void reset() {
        values.clear();
    }

    public ScoreState copy() {
        ScoreState copy = new ScoreState();
        copy.values.putAll(values);
        return copy;
    }

    public ScoreState difference(ScoreState other) {
        ScoreState diff = new ScoreState();
        for (StateKey key : KEYS) {
            diff.values.put(key, getValue(key) - other.getValue(key));
        }
        return diff;
    }

    public void produce(Bot bot) {
        for (StateKey key : KEYS) {
            Double produced = key.produce(bot);
            if (produced == null) continue;
            values.put(key, produced);
        }
    }
}
